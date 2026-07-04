package com.imnaiyar.skytimes.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.constants.events
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.di.LocalSettingsViewModel
import com.imnaiyar.skytimes.theme.DarkColor
import com.imnaiyar.skytimes.theme.labelTiny
import com.imnaiyar.skytimes.ui.AnimatedTimer
import com.imnaiyar.skytimes.ui.ClockDirection
import com.imnaiyar.skytimes.ui.Grid
import com.imnaiyar.skytimes.ui.GridType
import com.imnaiyar.skytimes.utils.EventTimeUtils
import com.imnaiyar.skytimes.utils.TimeUtils
import com.imnaiyar.skytimes.utils.TimeValue
import com.imnaiyar.skytimes.utils.Times
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.close
import skytimes.shared.generated.resources.drag_indicator
import skytimes.shared.generated.resources.list_arrow
import kotlin.time.Instant

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    setFabVisible: (Boolean) -> Unit,
    fabPad: PaddingValues
) {

    val viewModel = LocalSettingsViewModel.current
    val settings by viewModel.settings.collectAsState()
    val orderedKey = remember(settings.eventOrder) {
        settings.eventOrder.toMutableStateList()
    }
    val orderSnapshot = orderedKey.toList()

    val byKey = remember {
        events.associateBy { it.key }
    }

    val eventDataList = remember(orderSnapshot) {
        orderSnapshot.mapNotNull(byKey::get)
    }

    var reorderMode by remember { mutableStateOf(false) }
    var orderChanged by remember { mutableStateOf(false) }

    LaunchedEffect(reorderMode) {
        setFabVisible(!reorderMode)
    }

    LaunchedEffect(orderSnapshot) {
        if (orderChanged) {
            delay(300)
            viewModel.setEventOrder(orderSnapshot)
            orderChanged = false
        }
    }

    val timeUtils = remember { TimeUtils() }
    val nowState =
        LocalAppContainer.current.clockRepository.observeEveryMinute()

    val hapticFeedback = LocalHapticFeedback.current

    val lazyGridState = rememberLazyGridState()

    val reorderableLazyGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
        orderedKey.apply {
            add(to.index - 1, removeAt(from.index - 1))
        }

        orderChanged = true
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    Grid(modifier, type = GridType.GRID, state = lazyGridState, contentPadding = fabPad) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = { reorderMode = !reorderMode }) {
                    Icon(
                        painterResource(if (!reorderMode) Res.drawable.list_arrow else Res.drawable.close),
                        contentDescription = "Reorder Mode Button"
                    )
                }
            }
        }
        items(eventDataList, key = { it.key }) { eventData ->
            ReorderableItem(reorderableLazyGridState, key = eventData.key) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                Surface(shadowElevation = elevation) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        AnimatedVisibility(
                            visible = reorderMode,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut()
                        ) {
                            IconButton(
                                modifier = Modifier.draggableHandle(),
                                onClick = {}
                            ) {
                                Icon(
                                    painterResource(Res.drawable.drag_indicator),
                                    contentDescription = null
                                )
                            }
                        }
                        EventRow(
                            eventData = eventData,
                            timeUtils = timeUtils,
                            nowState = nowState,
                            reorderMode = reorderMode,
                            clockAnimation = settings.clockAnimation,
                            use24HourClock = settings.use24HourClock
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun EventRow(
    eventData: EventData,
    timeUtils: TimeUtils,
    nowState: State<Instant>,
    clockAnimation: Boolean,
    use24HourClock: Boolean,
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
        if (isActive) DarkColor.onSecondary else Color.Unspecified
    val nextOcStyle =
        if (isActive) DarkColor.onSecondary.copy(0.5f) else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth().graphicsLayer {
            if (isActive) {
                translationY = y
                scaleX = scale
                scaleY = scale
            }
        }
            .background(
                color = if (isActive) DarkColor.secondary else Color.Unspecified,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(all = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isActive) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    eventDetails.event.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = eventNameStyle,
                )

                Text(
                    "Active (Next at ${
                        timeUtils.formatTime(
                            TimeValue.instant(eventDetails.nextOccurrence),
                            use24HourClock
                        )
                    })", style = MaterialTheme.typography.labelTiny,
                    color = eventNameStyle
                )
            }

        } else Text(
            eventDetails.event.name,
            style = MaterialTheme.typography.titleMedium,
        )
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
                    withAnimation = clockAnimation
                )
                Text(
                    text = "At ${timeUtils.formatTime(TimeValue.instant(nextAT), use24HourClock)}",
                    color = nextOcStyle,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}