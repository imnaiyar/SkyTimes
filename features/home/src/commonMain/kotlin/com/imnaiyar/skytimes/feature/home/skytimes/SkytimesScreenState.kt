package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.domain.EventCategory
import com.imnaiyar.skytimes.core.domain.EventData
import com.imnaiyar.skytimes.core.domain.EventKey
import com.imnaiyar.skytimes.core.domain.events
import com.imnaiyar.skytimes.feature.reminders.LocalReminderRepository
import com.imnaiyar.skytimes.feature.settings.LocalSettingsViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop


internal sealed interface IRow {
    data class Section(
        val key: Any,
        val title: String,
        val isPinnedSection: Boolean,
        val eventRows: List<Event>,
    ) : IRow

    data class Event(val eventData: EventData, val isPinned: Boolean, val notified: Notified) : IRow
}

/** Whether an event has reminders configured, and whether they're actually able to fire. */
internal enum class Notified {
    Yes,
    No,
    YesButGlobalDisabled,
}

/** All events keyed for O(1) lookup while building rows. */
private const val PINNED_HEADER_TITLE = "Pinned"

/** How long to wait after the last edit before persisting order/pin changes. */
private const val COMMIT_DEBOUNCE_MS = 300L

internal val GRID_ITEM_BG_COLOR
    @Composable
    get() = MaterialTheme.colorScheme.background.copy(0.8f)

internal val Grid_ITEM_PADDING = 4.dp

/**
 * Owns every piece of interactive state for the Home screen: category order, pinned
 * events,  and which row's context menu (if any) is open.
 */
@Stable
internal class HomeScreenState(
    initialCategoryOrder: List<EventCategory>,
    initialPinned: List<EventKey>,
    private val notificationsEnabled: State<Boolean>,
    private val notifiedStatusFor: (key: EventKey, notificationsEnabled: Boolean) -> Notified,
    private val hapticFeedback: HapticFeedback,
) {
    val orderedCategories = initialCategoryOrder.toMutableStateList()
    val pinnedKeys = initialPinned.toMutableStateList()

    /** Key of the event whose context menu is currently open, if any. */
    var selectedEventKey by mutableStateOf<EventKey?>(null)
        private set

    var eventDetailsToShow by mutableStateOf<EventData?>(null)
        private set

    val rows: State<List<IRow.Section>> = derivedStateOf {
        val pinnedSet = pinnedKeys.toSet()
        val enabled = notificationsEnabled.value

        buildList {
            val pinned = events.filter { it.key in pinnedSet }
            if (pinned.isNotEmpty()) addSection(
                PINNED_HEADER_TITLE,
                "pinned",
                pinned,
                true,
                enabled
            )

            orderedCategories.forEach { category ->
                val categoryEvents = events.filter { it.category == category }
                if (categoryEvents.isNotEmpty()) {
                    addSection(category.name, category, categoryEvents, false, enabled)
                }
            }
        }
    }

    private fun MutableList<IRow.Section>.addSection(
        title: String,
        key: Any,
        sectionEvents: List<EventData>,
        isPinnedSection: Boolean,
        notificationsEnabled: Boolean,
    ) {
        add(
            IRow.Section(
                key = key,
                title = title,
                isPinnedSection = isPinnedSection,
                eventRows = sectionEvents.map {
                    IRow.Event(
                        it,
                        isPinned = it.key in pinnedKeys,
                        notifiedStatusFor(it.key, notificationsEnabled)
                    )
                }
            )
        )
    }

    val firstEventKey: State<EventKey?> = derivedStateOf {
        rows.value.filterIsInstance<IRow.Section>().firstOrNull()
            ?.eventRows?.firstOrNull()?.eventData?.key
    }

    fun togglePin(key: EventKey) {
        if (!pinnedKeys.remove(key)) pinnedKeys.add(key)
    }

    fun openContextMenu(key: EventKey) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        selectedEventKey = key
    }

    fun closeContextMenu() {
        selectedEventKey = null
    }

    fun showEventDetails(eventData: EventData) {
        eventDetailsToShow = eventData
    }

    fun dismissEventDetails() {
        eventDetailsToShow = null
    }

    /** Invoked by the reorderable grid while the user drags a category card. */
    fun onMove(fromKey: Any?, toKey: Any?) {
        val from = fromKey as? EventCategory ?: return
        val to = toKey as? EventCategory ?: return
        if (from == to) return

        val fromIndex = orderedCategories.indexOf(from)
        val toIndex = orderedCategories.indexOf(to)
        if (fromIndex != -1 && toIndex != -1) {
            orderedCategories.add(toIndex, orderedCategories.removeAt(fromIndex))
        }

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }
}

@OptIn(FlowPreview::class)
@Composable
internal fun rememberHomeScreenState(): HomeScreenState {
    val viewModel = LocalSettingsViewModel.current
    val settingsState = viewModel.settings.collectAsState()
    val reminderState = LocalReminderRepository.current.reminders.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current

    val notificationsEnabled = remember(settingsState) {
        derivedStateOf { settingsState.value.notificationsEnabled }
    }

    val notifiedStatusFor = remember(reminderState.value) {
        { key: EventKey, enabled: Boolean ->
            val reminders = reminderState.value.filter { it.eventId == key }
            when {
                reminders.isEmpty() -> Notified.No
                reminders.none { it.enabled } -> Notified.No
                enabled -> Notified.Yes
                else -> Notified.YesButGlobalDisabled
            }
        }
    }

    val state = remember(viewModel) {
        val initial = settingsState.value
        HomeScreenState(
            initialCategoryOrder = initial.categoryOrder,
            initialPinned = initial.pinnedEvents,
            notificationsEnabled = notificationsEnabled,
            notifiedStatusFor = notifiedStatusFor,
            hapticFeedback = hapticFeedback,
        )
    }

    // Debounce writes back to the view model so a fast drag or a burst of pin toggles
    // produces one persisted update instead of one write per intermediate step.
    LaunchedEffect(state, viewModel) {
        snapshotFlow { state.orderedCategories.toList() to state.pinnedKeys.toList() }
            .drop(1) // first emission is just the seeded initial state, nothing changed yet
            .debounce(timeoutMillis = COMMIT_DEBOUNCE_MS)
            .collect { (categoryOrder, pinned) ->
                viewModel.setCategoryOrder(categoryOrder)
                viewModel.setPinnedEvents(pinned)
            }
    }

    return state
}
