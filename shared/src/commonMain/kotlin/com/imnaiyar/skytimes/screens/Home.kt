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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.constants.RoundedCorner
import com.imnaiyar.skytimes.constants.events
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.di.LocalSettingsViewModel
import com.imnaiyar.skytimes.theme.labelTiny
import com.imnaiyar.skytimes.ui.AnimatedTimer
import com.imnaiyar.skytimes.ui.ClockDirection
import com.imnaiyar.skytimes.ui.Grid
import com.imnaiyar.skytimes.ui.GridType
import com.imnaiyar.skytimes.utils.EventDetails
import com.imnaiyar.skytimes.utils.EventTimeUtils
import com.imnaiyar.skytimes.utils.TimeFormatter
import com.imnaiyar.skytimes.utils.Times
import com.imnaiyar.skytimes.utils.indexOfKey
import com.imnaiyar.skytimes.utils.rememberTimeFormatter
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyGridState
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.close
import skytimes.shared.generated.resources.drag_indicator
import skytimes.shared.generated.resources.list_arrow
import skytimes.shared.generated.resources.pin
import kotlin.time.Instant

sealed interface IRow {
    data class Header(val title: String) : IRow
    data class Event(val eventData: EventData, val isPinned: Boolean) : IRow
}


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    setFabVisible: (Boolean) -> Unit,
    fabPad: PaddingValues
) {
    val viewModel = LocalSettingsViewModel.current
    val settings by viewModel.settings.collectAsState()


    val orderedKey = remember { settings.eventOrder.toMutableStateList() }
    val pinnedEvents = remember { settings.pinnedEvents.toMutableStateList() }

    val byKey = remember { events.associateBy { it.key } }

    var reorderMode by remember { mutableStateOf(false) }
    var pendingCommit by remember { mutableStateOf(false) }
    var contextMenuKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(reorderMode) {
        setFabVisible(!reorderMode)
        if (reorderMode) contextMenuKey = null
    }

    LaunchedEffect(orderedKey.toList(), pinnedEvents.toList()) {
        if (pendingCommit) {
            delay(timeMillis = 300)
            viewModel.setEventOrder(orderedKey.toList())
            viewModel.setPinnedEvents(pinnedEvents.toList())
            pendingCommit = false
        }
    }

    val nowState = LocalAppContainer.current.clockRepository.observeEveryMinute()
    val hapticFeedback = LocalHapticFeedback.current
    val lazyGridState = rememberLazyGridState()

    val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
        val fromKey = from.key
        val toKey = to.key
        if (fromKey == toKey) return@rememberReorderableLazyGridState

        val fromPinned = fromKey in pinnedEvents
        val toPinned = toKey in pinnedEvents

        if (fromPinned != toPinned) {
            val eventKey = fromKey as EventKey
            if (toPinned) pinnedEvents.add(eventKey) else pinnedEvents.remove(eventKey)
        }

        orderedKey.apply {
            val fromIndex = indexOfKey(fromKey)
            val toIndex = indexOfKey(toKey)
            if (fromIndex != -1 && toIndex != -1) {
                add(toIndex, removeAt(fromIndex))
            }
        }

        pendingCommit = true
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    // merged rows so dragging and dropping between two categories is smooth
    val rows = remember(orderedKey.toList(), pinnedEvents.toList()) {
        val pinned = orderedKey.filter { it in pinnedEvents }.mapNotNull(byKey::get)
        val others = orderedKey.filterNot { it in pinnedEvents }.mapNotNull(byKey::get)
        buildList {
            if (pinned.isNotEmpty()) {
                add(IRow.Header("Pinned"))
                pinned.forEach { add(IRow.Event(it, isPinned = true)) }
            }
            if (others.isNotEmpty()) {
                add(IRow.Header("Others"))
                others.forEach { add(IRow.Event(it, isPinned = false)) }
            }
        }
    }

    Grid(modifier, type = GridType.GRID, state = lazyGridState, contentPadding = fabPad) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            val topRowAlpha by animateFloatAsState(
                targetValue = if (contextMenuKey != null) 0.35f else 1f,
                animationSpec = tween(durationMillis = 300)
            )
            Row(
                modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = topRowAlpha },
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { reorderMode = !reorderMode }) {
                    Icon(
                        painterResource(if (!reorderMode) Res.drawable.list_arrow else Res.drawable.close),
                        contentDescription = "Reorder Mode Button"
                    )
                }
            }
        }

        items(
            rows,
            key = { row ->
                when (row) {
                    is IRow.Header -> "header_${row.title}"
                    is IRow.Event -> row.eventData.key
                }
            },
            span = { row -> if (row is IRow.Header) GridItemSpan(maxLineSpan) else GridItemSpan(1) }
        ) { row ->
            when (row) {
                is IRow.Header -> SectionHeader(title = row.title)
                is IRow.Event -> EventGridItem(
                    eventData = row.eventData,
                    isPinned = row.isPinned,
                    reorderMode = reorderMode,
                    reorderableLazyGridState = reorderableLazyGridState,
                    contextMenuKey = contextMenuKey,
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        contextMenuKey = row.eventData.key.name
                    },
                    onDismissMenu = { contextMenuKey = null },
                    onPinToggle = {
                        if (row.isPinned) {
                            pinnedEvents.remove(row.eventData.key)
                        } else {
                            pinnedEvents.add(row.eventData.key)
                        }
                        pendingCommit = true
                    },
                    timeUtils = rememberTimeFormatter(),
                    nowState = nowState,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
private fun LazyGridItemScope.EventGridItem(
    eventData: EventData,
    isPinned: Boolean,
    reorderMode: Boolean,
    reorderableLazyGridState: ReorderableLazyGridState,
    contextMenuKey: String?,
    onLongClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onPinToggle: () -> Unit,
    timeUtils: TimeFormatter,
    nowState: State<Instant>,
) {
    ReorderableItem(
        reorderableLazyGridState,
        key = eventData.key
    ) { isDragging ->
        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

        val isMenuOpen = contextMenuKey == eventData.key.name
        val isDimmed = contextMenuKey != null && !isMenuOpen

        // slow scale-up while this row's context menu is open
        val rowScale by animateFloatAsState(
            targetValue = if (isMenuOpen) 1.06f else 1f,
            animationSpec = tween(durationMillis = 500)
        )
        // dim every other row while a context menu is open
        val rowAlpha by animateFloatAsState(
            targetValue = if (isDimmed) 0.35f else 1f,
            animationSpec = tween(durationMillis = 300)
        )

        Box {
            Surface(
                shadowElevation = elevation,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = rowScale
                        scaleY = rowScale
                        alpha = rowAlpha
                    }
                    .then(
                        // long-press context menu is disabled while reordering
                        if (!reorderMode) {
                            Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = onLongClick
                            )
                        } else Modifier
                    ),
                shape = RoundedCorner,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReorderIcon(reorderMode)
                    EventRow(
                        eventData = eventData,
                        isPinned = isPinned,
                        timeUtils = timeUtils,
                        nowState = nowState,
                        reorderMode = reorderMode,
                    )
                }
            }
            ContextMenu(isMenuOpen, isPinned, onDissmiss = onDismissMenu, onPinToggle)
        }
    }
}


@Composable
private fun EventRow(
    eventData: EventData,
    timeUtils: TimeFormatter,
    nowState: State<Instant>,
    isPinned: Boolean,
    reorderMode: Boolean
) {
    val now = nowState.value

    val eventDetails = remember(eventData, now) {
        EventTimeUtils.getEventDetails(eventData, now, includeAllOccurrences = false)
    }

    // reorder mode too because ideally here we only want to display event name
    // so no event status related customization  should be done
    val isActive = eventDetails.status is Times.Active && !reorderMode
    val difference = eventDetails.nextOccurrence.toEpochMilliseconds() - now.toEpochMilliseconds()
    val formatted = timeUtils.formatMillis(
        if (isActive)
            eventDetails.status.remaining.inWholeMilliseconds
        else
            difference,
        false
    )


    val y: Float
    val scale: Float
    if (isActive) {
        val transition = rememberInfiniteTransition()

        val animatedY by transition.animateFloat(
            initialValue = 0f,
            targetValue = -3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )

        val animatedScale by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.015f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )

        y = animatedY
        scale = animatedScale
    } else {
        y = 0f
        scale = 1f
    }

    val nextAT = if (isActive) eventDetails.status.endTime else eventDetails.nextOccurrence

    val eventNameStyle =
        if (isActive) MaterialTheme.colorScheme.onSecondary else Color.Unspecified
    val nextOcStyle =
        if (isActive) MaterialTheme.colorScheme.onSecondary.copy(0.5f) else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth().graphicsLayer {
            if (isActive) {
                translationY = y
                scaleX = scale
                scaleY = scale
            }
        }
            .background(
                color = if (isActive) MaterialTheme.colorScheme.secondary else Color.Unspecified,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(all = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isActive) {
            Column(horizontalAlignment = Alignment.Start) {
                EventNameLabel(
                    eventDetails,
                    isPinned,
                    eventNameStyle,
                    isActive = true
                )
                Text(
                    "Active (Next at ${
                        timeUtils.format(eventDetails.nextOccurrence)
                    })", style = MaterialTheme.typography.labelTiny,
                    color = eventNameStyle
                )
            }

        } else
            EventNameLabel(eventDetails, isPinned, style = MaterialTheme.typography.titleMedium)

        AnimatedVisibility(
            visible = !reorderMode,
            enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (isActive) Text(
                    "Ends in",
                    color = eventNameStyle,
                    style = MaterialTheme.typography.labelTiny
                )
                AnimatedTimer(
                    time = formatted,
                    size = MaterialTheme.typography.titleSmall,
                    color = eventNameStyle,
                    modifier = Modifier.padding(start = 8.dp),
                    direction = ClockDirection.DOWN,
                )
                Text(
                    text = "At ${timeUtils.format(nextAT)}",
                    color = nextOcStyle,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}


@Composable
private fun ReorderableCollectionItemScope.ReorderIcon(
    visible: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally() + fadeIn(),
        exit = shrinkHorizontally() + fadeOut()
    ) {
        IconButton(
            modifier = Modifier.draggableHandle(),
            onClick = {}
        ) {
            Icon(
                painter = painterResource(Res.drawable.drag_indicator),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ContextMenu(
    isOpen: Boolean,
    isPinned: Boolean,
    onDissmiss: () -> Unit,
    onPinClick: () -> Unit
) {
    // have pin slightly tilted to right then animate it straight on pinned
    val pinRotation by animateFloatAsState(
        targetValue = if (isPinned) 30f else 0f,
        label = "PinRotation"
    )

    DropdownMenu(
        modifier = Modifier.width(180.dp),
        expanded = isOpen,
        onDismissRequest = onDissmiss
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
                    painterResource(Res.drawable.pin), null,
                    modifier = Modifier.rotate(pinRotation)
                )
            },
            onClick = onPinClick,
            colors = if (isPinned)
                MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.primary,
                    trailingIconColor = MaterialTheme.colorScheme.primary
                )
            else MenuDefaults.itemColors()
        )
        HorizontalDivider(modifier = Modifier.padding(5.dp))
        DropdownMenuItem(
            text = { Text("Notification") },
            onClick = { }
        )
    }
}

@Composable
private fun EventNameLabel(
    eventDetails: EventDetails,
    isPinned: Boolean = false,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    isActive: Boolean = false,
) {
    Row {
        Text(
            eventDetails.event.name,
            style = style,
            color = color,
        )
        AnimatedVisibility(isPinned) {
            Icon(
                painterResource(Res.drawable.pin),
                contentDescription = null,
                modifier = Modifier.rotate(30f).size(18.dp),
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary else
                    MaterialTheme.colorScheme.primary
            )
        }
    }

}