package com.imnaiyar.skytimes.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.constants.events
import com.imnaiyar.skytimes.ui.AnimatedTimer
import com.imnaiyar.skytimes.ui.ClockDirection
import com.imnaiyar.skytimes.utils.EventTimeUtils
import com.imnaiyar.skytimes.utils.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.drag_indicator
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    var eventDataList = remember {
        val byKey = events.associateBy { it.key }
        EventKey.entries.mapNotNull { byKey[it] }
    }

    val timeUtils = remember { TimeUtils() }
    val hapticFeedback = LocalHapticFeedback.current

    val lazyListState = rememberLazyListState()

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        eventDataList = eventDataList.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }

        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
    ) {
        items(eventDataList, key = { it.key }) { eventData ->
           ReorderableItem(reorderableLazyListState, key = eventData.key) { isDragging ->
               val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

               Surface(shadowElevation = elevation) {
                   Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                       IconButton(
                           modifier = Modifier.draggableHandle(
                               onDragStarted = {
                                   hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                               },
                               onDragStopped = {
                                   hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                               },
                           ),
                           onClick = {}
                       ) {
                           Icon(
                               painterResource(Res.drawable.drag_indicator),
                               contentDescription = "Drag Handle"
                           )
                       }
                       EventRow(eventData, timeUtils)
                   }
               }
           }
        }
    }
}

@Composable
private fun EventRow(
    eventData: EventData,
    timeUtils: TimeUtils
) {

    var eventDetails by remember {
        mutableStateOf(EventTimeUtils.getEventDetails(eventData))
    }

    var now by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            val current = Clock.System.now()
            now = current
            delay(timeMillis = 1000 - (current.toEpochMilliseconds() % 1000))
        }
    }

    if (now >= eventDetails.nextOccurrence) {
        eventDetails = EventTimeUtils.getEventDetails(eventData)
    }

    val difference = eventDetails.nextOccurrence.toEpochMilliseconds() - now.toEpochMilliseconds()
    val formatted = timeUtils.formatMillis(difference)

    val atTimeText = remember(eventDetails) {
            eventDetails.nextOccurrence
                .toLocalDateTime(TimeZone.currentSystemDefault()).time
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(eventDetails.event.name, style = MaterialTheme.typography.titleMedium)
        Column(horizontalAlignment = Alignment.End) {
            AnimatedTimer(
                time = formatted,
                size = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp),
                direction = ClockDirection.DOWN,
            )
            Text(
                text = "At: ${timeUtils.formatTime(atTimeText)}",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

