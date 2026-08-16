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
import com.imnaiyar.skytimes.core.common.indexOfKey
import com.imnaiyar.skytimes.core.domain.EventData
import com.imnaiyar.skytimes.core.domain.EventKey
import com.imnaiyar.skytimes.core.domain.events
import com.imnaiyar.skytimes.feature.reminders.LocalReminderRepository
import com.imnaiyar.skytimes.feature.settings.LocalSettingsViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop


internal sealed interface IRow {
    data class Header(val title: String) : IRow
    data class Event(val eventData: EventData, val isPinned: Boolean, val notified: Notified) : IRow
}

/** Whether an event has reminders configured, and whether they're actually able to fire. */
internal enum class Notified {
    Yes,
    No,
    YesButGlobalDisabled,
}

/** All events keyed for O(1) lookup while building rows. */
private val eventsByKey: Map<EventKey, EventData> = events.associateBy { it.key }

private const val PINNED_HEADER_TITLE = "Pinned"
private const val OTHERS_HEADER_TITLE = "Others"

/** How long to wait after the last edit before persisting order/pin changes. */
private const val COMMIT_DEBOUNCE_MS = 300L

internal val GRID_ITEM_BG_COLOR
    @Composable
    get() = MaterialTheme.colorScheme.surfaceContainer

internal val GRID_ITEM_TOP_PADDING = 16.dp
internal val Grid_ITEM_PADDING = 4.dp

/**
 * Owns every piece of interactive state for the Home screen: event order, pinned
 * events, reorder mode, and which row's context menu (if any) is open.
 */
@Stable
internal class HomeScreenState(
    initialOrder: List<EventKey>,
    initialPinned: List<EventKey>,
    private val notificationsEnabled: State<Boolean>,
    private val notifiedStatusFor: (key: EventKey, notificationsEnabled: Boolean) -> Notified,
    private val hapticFeedback: HapticFeedback,
) {
    val orderedKeys = initialOrder.toMutableStateList()
    val pinnedKeys = initialPinned.toMutableStateList()

    var reorderMode by mutableStateOf(false)
        private set

    /** Key of the event whose context menu is currently open, if any. */
    var selectedEventKey by mutableStateOf<EventKey?>(null)
        private set

    val rows: State<List<IRow>> = derivedStateOf {
        val pinnedSet = pinnedKeys.toSet()
        val enabled = notificationsEnabled.value

        buildList {
            val pinned = orderedKeys.filter { it in pinnedSet }.mapNotNull(eventsByKey::get)
            val others = orderedKeys.filterNot { it in pinnedSet }.mapNotNull(eventsByKey::get)

            if (pinned.isNotEmpty()) {
                add(IRow.Header(PINNED_HEADER_TITLE))
                pinned.forEach {
                    add(
                        IRow.Event(
                            it,
                            isPinned = true,
                            notifiedStatusFor(it.key, enabled)
                        )
                    )
                }
            }
            if (others.isNotEmpty()) {
                add(IRow.Header(OTHERS_HEADER_TITLE))
                others.forEach {
                    add(
                        IRow.Event(
                            it,
                            isPinned = false,
                            notifiedStatusFor(it.key, enabled)
                        )
                    )
                }
            }
        }
    }

    val firstEventKey: State<EventKey?> = derivedStateOf {
        rows.value.filterIsInstance<IRow.Event>().firstOrNull()?.eventData?.key
    }

    fun toggleReorderMode() {
        reorderMode = !reorderMode

        // if for some rare reason, both context menu and reorder is active, disable context menu
        if (reorderMode) selectedEventKey = null
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

    /** Invoked by the reorderable grid while the user drags a row to a new spot. */
    fun onMove(fromKey: Any?, toKey: Any?) {
        val from = fromKey as? EventKey ?: return
        val to = toKey as? EventKey ?: return
        if (from == to) return

        // Dragging a row across the Pinned/Others boundary re-pins it to match.
        val fromPinned = from in pinnedKeys
        val toPinned = to in pinnedKeys
        if (fromPinned != toPinned) {
            if (toPinned) pinnedKeys.add(from) else pinnedKeys.remove(from)
        }

        val fromIndex = orderedKeys.indexOfKey(from)
        val toIndex = orderedKeys.indexOfKey(to)
        if (fromIndex != -1 && toIndex != -1) {
            orderedKeys.add(toIndex, orderedKeys.removeAt(fromIndex))
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
            initialOrder = initial.eventOrder,
            initialPinned = initial.pinnedEvents,
            notificationsEnabled = notificationsEnabled,
            notifiedStatusFor = notifiedStatusFor,
            hapticFeedback = hapticFeedback,
        )
    }

    // Debounce writes back to the view model so a fast drag or a burst of pin toggles
    // produces one persisted update instead of one write per intermediate step.
    LaunchedEffect(state, viewModel) {
        snapshotFlow { state.orderedKeys.toList() to state.pinnedKeys.toList() }
            .drop(1) // first emission is just the seeded initial state, nothing changed yet
            .debounce(timeoutMillis = COMMIT_DEBOUNCE_MS)
            .collect { (order, pinned) ->
                viewModel.setEventOrder(order)
                viewModel.setPinnedEvents(pinned)
            }
    }

    return state
}