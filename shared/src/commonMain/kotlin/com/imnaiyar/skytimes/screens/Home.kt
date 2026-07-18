package com.imnaiyar.skytimes.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.constants.events
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.di.LocalSettingsViewModel
import com.imnaiyar.skytimes.onboarding.AppTutorialStep
import com.imnaiyar.skytimes.onboarding.TutorialTarget
import com.imnaiyar.skytimes.reminders.ui.rememberReminderFlow
import com.imnaiyar.skytimes.theme.labelTiny
import com.imnaiyar.skytimes.ui.AnimatedTimer
import com.imnaiyar.skytimes.ui.ClockDirection
import com.imnaiyar.skytimes.ui.Grid
import com.imnaiyar.skytimes.ui.GridType
import com.imnaiyar.skytimes.ui.Tooltip
import com.imnaiyar.skytimes.utils.EventDetails
import com.imnaiyar.skytimes.utils.EventTimeUtils
import com.imnaiyar.skytimes.utils.TimeFormatter
import com.imnaiyar.skytimes.utils.Times
import com.imnaiyar.skytimes.utils.indexOfKey
import com.imnaiyar.skytimes.utils.rememberTimeFormatter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyGridState
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.close
import skytimes.shared.generated.resources.drag_indicator
import skytimes.shared.generated.resources.list_arrow
import skytimes.shared.generated.resources.notifications
import skytimes.shared.generated.resources.pin
import kotlin.time.Instant


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

private val GRID_ITEM_BG_COLOR
    @Composable
    get() = MaterialTheme.colorScheme.surfaceContainer

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
    val appContainer = LocalAppContainer.current
    val viewModel = LocalSettingsViewModel.current
    val settingsState = viewModel.settings.collectAsState()
    val reminderState = appContainer.reminderRepository.reminders.collectAsState()
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


/** Long-press context menu for a single event row: pin/unpin and set a reminder. */
@Composable
internal fun ContextMenu(
    isOpen: Boolean,
    isPinned: Boolean,
    onDismiss: () -> Unit,
    onPinClick: () -> Unit,
    onReminderClick: () -> Unit,
) {

    val pinRotation by animateFloatAsState(
        targetValue = if (isPinned) 30f else 0f,
        label = "PinRotation",
    )

    DropdownMenu(
        modifier = Modifier.width(180.dp),
        expanded = isOpen,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    if (isPinned) "Unpin" else "Pin",
                    modifier = Modifier.animateContentSize()
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.pin),
                    contentDescription = null,
                    modifier = Modifier.rotate(pinRotation),
                )
            },
            onClick = onPinClick,
            colors = if (isPinned) {
                MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.primary,
                    trailingIconColor = MaterialTheme.colorScheme.primary,
                )
            } else {
                MenuDefaults.itemColors()
            },
        )
        HorizontalDivider(modifier = Modifier.padding(5.dp))
        DropdownMenuItem(text = { Text("Reminder") }, onClick = onReminderClick)
    }
}

@Composable
internal fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp),
    )
}


/** Event name with an optional pin badge and notification bell. */
@Composable
internal fun EventNameLabel(
    eventDetails: EventDetails,
    isPinned: Boolean = false,
    notified: Notified,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    isActive: Boolean = false,
) {
    val iconColor =
        if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = eventDetails.event.name, style = style, color = color)

        AnimatedVisibility(visible = isPinned) {
            Tooltip("This event is pinned to the top") {
                Icon(
                    painter = painterResource(Res.drawable.pin),
                    contentDescription = "Pinned",
                    modifier = Modifier.rotate(30f).size(18.dp),
                    tint = iconColor,
                )
            }
        }

        AnimatedVisibility(visible = notified != Notified.No) {
            Tooltip(text = "Reminders enabled for this event" + if (notified == Notified.YesButGlobalDisabled) " (notifications are globally disabled)" else "") {
                Icon(
                    painter = painterResource(Res.drawable.notifications),
                    contentDescription = "Reminder set",
                    modifier = Modifier.size(12.dp),
                    tint = if (notified == Notified.Yes) iconColor else iconColor.copy(alpha = 0.5f),
                )
            }
        }
    }
}


/** The row's content: event name/status on the left, countdown on the right. */
@Composable
internal fun EventRow(
    row: IRow.Event,
    reorderMode: Boolean,
    timeFormatter: TimeFormatter,
    nowState: State<Instant>,
) {
    val now = nowState.value
    val eventDetails = remember(row.eventData, now) {
        EventTimeUtils.getEventDetails(row.eventData, now, includeAllOccurrences = false)
    }

    // Even in reorder mode we only ever want the plain event name, no status styling,
    // so treat the row as inactive while the user is reordering.
    val isActive = eventDetails.status is Times.Active && !reorderMode
    val motion = if (isActive) rememberActiveRowMotion() else RowMotion.None

    val remainingMillis = if (isActive) {
        eventDetails.status.remaining.inWholeMilliseconds
    } else {
        eventDetails.nextOccurrence.toEpochMilliseconds() - now.toEpochMilliseconds()
    }
    val countdown = timeFormatter.formatMillis(remainingMillis, false)
    val nextAt = if (isActive) eventDetails.status.endTime else eventDetails.nextOccurrence

    val nameColor = if (isActive) MaterialTheme.colorScheme.onSecondary else Color.Unspecified
    val nextAtColor = if (isActive) {
        MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = motion.translationY
                scaleX = motion.scale
                scaleY = motion.scale
            }
            .background(
                color = if (isActive) MaterialTheme.colorScheme.secondary else Color.Unspecified,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(all = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (isActive) {
            Column(horizontalAlignment = Alignment.Start) {
                EventNameLabel(
                    eventDetails = eventDetails,
                    isPinned = row.isPinned,
                    notified = row.notified,
                    color = nameColor,
                    isActive = true,
                )
                Text(
                    text = "Active (Next at ${timeFormatter.format(eventDetails.nextOccurrence)})",
                    style = MaterialTheme.typography.labelTiny,
                    color = nameColor,
                )
            }
        } else {
            EventNameLabel(
                eventDetails = eventDetails,
                isPinned = row.isPinned,
                notified = row.notified,
                style = MaterialTheme.typography.labelLarge,
            )
        }

        AnimatedVisibility(
            visible = !reorderMode,
            enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut(),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (isActive) {
                    Text(
                        text = "Ends in",
                        color = nameColor,
                        style = MaterialTheme.typography.labelTiny
                    )
                }
                AnimatedTimer(
                    time = countdown,
                    size = MaterialTheme.typography.labelLarge,
                    color = nameColor,
                    modifier = Modifier.padding(start = 8.dp),
                    direction = ClockDirection.DOWN,
                )
                Text(
                    text = "At ${timeFormatter.format(nextAt)}",
                    color = nextAtColor,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

/** Gentle up-and-down "breathing" motion applied to a row while its event is active. */
private data class RowMotion(val translationY: Float, val scale: Float) {
    companion object {
        val None = RowMotion(translationY = 0f, scale = 1f)
    }
}

@Composable
private fun rememberActiveRowMotion(): RowMotion {
    val transition = rememberInfiniteTransition(label = "ActiveEventMotion")
    val y by transition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translationY",
    )
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale",
    )
    return RowMotion(translationY = y, scale = scale)
}


@Composable
internal fun LazyGridItemScope.EventGridItem(
    row: IRow.Event,
    reorderMode: Boolean,
    reorderableLazyGridState: ReorderableLazyGridState,
    isMenuOpen: Boolean,
    isDimmed: Boolean,
    isTutorialTarget: Boolean,
    timeFormatter: TimeFormatter,
    nowState: State<Instant>,
    onLongClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onPinToggle: () -> Unit,
    onReminderClick: () -> Unit,
    isLast: Boolean
) {
    ReorderableItem(reorderableLazyGridState, key = row.eventData.key) { isDragging ->
        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)


        val rowScale by animateFloatAsState(
            targetValue = if (isMenuOpen) 1.06f else 1f,
            animationSpec = tween(durationMillis = 500),
        )
        val rowAlpha by animateFloatAsState(
            targetValue = if (isDimmed) 0.35f else 1f,
            animationSpec = tween(durationMillis = 300),
        )

        /** top item will always be a category label, so its corner is handled in [HomeTopBar] */
        val bottomShape = if (isLast) 16.dp else 0.dp

        Box {
            TutorialTarget(
                id = AppTutorialStep.HomeEventContextMenu.targetId,
                enabled = isTutorialTarget
            ) {
                Surface(
                    shadowElevation = elevation,
                    shape = RoundedCornerShape(bottomStart = bottomShape, bottomEnd = bottomShape),
                    color = GRID_ITEM_BG_COLOR,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = rowScale
                            scaleY = rowScale
                            alpha = rowAlpha
                        }
                        .then(
                            // Long-press context menu is disabled while reordering so it
                            // doesn't fight with the drag gesture.
                            if (reorderMode) {
                                Modifier
                            } else {
                                Modifier.combinedClickable(onClick = {}, onLongClick = onLongClick)
                            },
                        ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(
                            4.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReorderIcon(visible = reorderMode)
                        EventRow(
                            row = row,
                            reorderMode = reorderMode,
                            timeFormatter = timeFormatter,
                            nowState = nowState
                        )
                    }
                }
            }

            ContextMenu(
                isOpen = isMenuOpen,
                isPinned = row.isPinned,
                onDismiss = onDismissMenu,
                onPinClick = onPinToggle,
                onReminderClick = onReminderClick,
            )
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.ReorderIcon(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally() + fadeIn(),
        exit = shrinkHorizontally() + fadeOut(),
    ) {
        IconButton(modifier = Modifier.draggableHandle(), onClick = {}) {
            Icon(
                painter = painterResource(Res.drawable.drag_indicator),
                contentDescription = "Drag to reorder"
            )
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    setFabVisible: (Boolean) -> Unit,
    fabPad: PaddingValues,
    tutorialTargetsEnabled: Boolean,
) {
    val state = rememberHomeScreenState()
    val appContainer = LocalAppContainer.current
    val reminderFlow = rememberReminderFlow(appContainer)
    val nowState = appContainer.clockRepository.observeEveryMinute()
    val timeFormatter = rememberTimeFormatter()

    val lazyGridState = rememberLazyGridState()
    val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
        state.onMove(from.key, to.key)
    }

    LaunchedEffect(state.reorderMode) {
        setFabVisible(!state.reorderMode)
    }

    val rows by state.rows
    val firstEventKey by state.firstEventKey

    Box(modifier = modifier.fillMaxSize()) {
        Grid(
            type = GridType.GRID,
            state = lazyGridState,
            contentPadding = fabPad,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(
                items = rows,
                key = { _, row ->
                    when (row) {
                        is IRow.Header -> "header_${row.title}"
                        is IRow.Event -> row.eventData.key
                    }
                },
                span = { _, row ->
                    if (row is IRow.Header) GridItemSpan(maxLineSpan) else GridItemSpan(
                        1
                    )
                },
            ) { index, row ->
                when (row) {
                    is IRow.Header -> HomeTopBar(
                        reorderMode = state.reorderMode,
                        dimmed = state.selectedEventKey != null,
                        tutorialTargetsEnabled = tutorialTargetsEnabled,
                        index = index,
                        headerTitle = row.title,
                        onToggleReorderMode = state::toggleReorderMode,
                    )

                    is IRow.Event -> EventGridItem(
                        row = row,
                        reorderMode = state.reorderMode,
                        reorderableLazyGridState = reorderableLazyGridState,
                        isMenuOpen = state.selectedEventKey == row.eventData.key,
                        isDimmed = state.selectedEventKey != null && state.selectedEventKey != row.eventData.key,
                        isTutorialTarget = tutorialTargetsEnabled && row.eventData.key == firstEventKey,
                        timeFormatter = timeFormatter,
                        nowState = nowState,
                        onLongClick = { state.openContextMenu(row.eventData.key) },
                        onDismissMenu = state::closeContextMenu,
                        onPinToggle = { state.togglePin(row.eventData.key) },
                        onReminderClick = { reminderFlow.requestReminderEditor(row.eventData) },
                        isLast = index == rows.lastIndex
                    )
                }

            }
        }

        // Reminder-related dialogs (e.g. exact-alarm permission prompts).
        reminderFlow.RenderDialogs()
    }
}

@Composable
private fun HomeTopBar(
    reorderMode: Boolean,
    dimmed: Boolean,
    tutorialTargetsEnabled: Boolean,
    headerTitle: String,
    index: Int = 0,
    onToggleReorderMode: () -> Unit,
) {
    val alpha by animateFloatAsState(
        targetValue = if (dimmed) 0.35f else 1f,
        animationSpec = tween(durationMillis = 300),
    )
    val topShape = if (index == 0) 16.dp else 0.dp

    Row(
        modifier = Modifier.fillMaxWidth().background(
            GRID_ITEM_BG_COLOR,
            RoundedCornerShape(topStart = topShape, topEnd = topShape)
        ).padding(4.dp).graphicsLayer { this.alpha = alpha },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionHeader(headerTitle)

        if (index == 0) {
            TutorialTarget(
                id = AppTutorialStep.HomeReorder.targetId,
                enabled = tutorialTargetsEnabled
            ) {
                IconButton(onClick = onToggleReorderMode) {
                    Icon(
                        painter = painterResource(if (reorderMode) Res.drawable.close else Res.drawable.list_arrow),
                        contentDescription = "Reorder Mode Button",
                    )
                }
            }
        }
    }
}