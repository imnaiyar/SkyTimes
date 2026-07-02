package com.imnaiyar.skytimes.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.imnaiyar.skytimes.di.LocalSettingsViewModel
import com.imnaiyar.skytimes.theme.labelTiny
import com.imnaiyar.skytimes.ui.AnimatedTimer
import com.imnaiyar.skytimes.ui.ClockDirection
import com.imnaiyar.skytimes.utils.EventTimeUtils
import com.imnaiyar.skytimes.utils.TimeUtils
import com.imnaiyar.skytimes.utils.TimeValue
import com.imnaiyar.skytimes.utils.Times
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.close
import skytimes.shared.generated.resources.drag_indicator
import skytimes.shared.generated.resources.list_arrow
import kotlin.time.Clock

@Composable
fun HomeScreen(modifier: Modifier = Modifier, setFabVisible: (Boolean) -> Unit) {

    val viewModel = LocalSettingsViewModel.current
    val settings by viewModel.settings.collectAsState()
    val orderedKey = remember(settings.eventOrder) {
        settings.eventOrder.toMutableStateList()
    }

    val byKey = remember {
        events.associateBy { it.key }
    }

    val eventDataList = orderedKey.mapNotNull(byKey::get)

    val reorderMode = remember { mutableStateOf(false) }

    LaunchedEffect(reorderMode.value) {
        setFabVisible(!reorderMode.value)
    }

    val timeUtils = remember { TimeUtils() }
    val hapticFeedback = LocalHapticFeedback.current

    val lazyListState = rememberLazyListState()

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        orderedKey.apply {
            add(to.index - 1, removeAt(from.index - 1))
        }

        viewModel.setEventOrder(orderedKey)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }


    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        horizontalAlignment = Alignment.End
    ) {
        item() {
            IconButton(
                onClick = { reorderMode.value = !reorderMode.value }) {
                Icon(
                    painterResource(if (!reorderMode.value) Res.drawable.list_arrow else Res.drawable.close),
                    contentDescription = "Reorder Mode Button"
                )
            }
        }
        items(eventDataList, key = { it.key }) { eventData ->
            ReorderableItem(reorderableLazyListState, key = eventData.key) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                val transition = rememberInfiniteTransition()

                val blur by transition.animateFloat(
                    initialValue = 8f,
                    targetValue = 20f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Surface(shadowElevation = elevation) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        AnimatedVisibility(
                            visible = reorderMode.value,
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
                        EventRow(eventData, timeUtils, reorderMode.value)
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
    reorderMode: Boolean
) {


    var now by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            val current = Clock.System.now()
            now = current
            delay(timeMillis = 1000 - (current.toEpochMilliseconds() % 1000))
        }
    }

    val eventDetails = EventTimeUtils.getEventDetails(eventData)

    val nextOccursOn = remember { eventDetails.nextOccurrence }

    // reorder mode too because ideally here we only want to display event name
    // so no event status related customization  should be done
    val isActive = eventDetails.status is Times.Active && !reorderMode
    val difference = eventDetails.nextOccurrence.toEpochMilliseconds() - now.toEpochMilliseconds()
    val formatted = timeUtils.formatMillis(
        if (isActive)
            eventDetails.status.remaining.inWholeMilliseconds
        else
            difference
    )


    val transition = rememberInfiniteTransition()

    val y by transition.animateFloat(
        initialValue = 0f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val nextAT = if (isActive) eventDetails.status.endTime else eventDetails.nextOccurrence

    val eventNameStyle =
        if (isActive) MaterialTheme.colorScheme.onError else Color.Unspecified
    val nextOcStyle =
        if (isActive) MaterialTheme.colorScheme.onError.copy(0.8f) else MaterialTheme.colorScheme.tertiary

    Row(
        modifier = Modifier.fillMaxWidth().graphicsLayer {
            if (isActive) {
                translationY = y
                scaleX = scale
                scaleY = scale
            } else 0f
        }
            .animateContentSize()
            .background(
                color = if (isActive) MaterialTheme.colorScheme.error else Color.Unspecified,
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
                        timeUtils.formatTime(TimeValue.instant(nextOccursOn))
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
                    "End in",
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
                    text = "At: ${timeUtils.formatTime(TimeValue.instant(nextAT))}",
                    color = nextOcStyle,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

