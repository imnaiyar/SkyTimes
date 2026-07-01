package com.imnaiyar.skytimes.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import com.imnaiyar.skytimes.LocalViewModel
import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.constants.events
import com.imnaiyar.skytimes.ui.ActionBar
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
import skytimes.shared.generated.resources.close
import skytimes.shared.generated.resources.drag_indicator
import skytimes.shared.generated.resources.list_arrow
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun HomeScreen(modifier: Modifier = Modifier, setFabVisible: (Boolean) -> Unit) {

    val viewModel = LocalViewModel.current
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
            add(to.index - 1 , removeAt(from.index - 1))
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
                       contentDescription = "Reorder Mode Button")
               }
           }
           items(eventDataList, key = { it.key }) { eventData ->
               ReorderableItem(reorderableLazyListState, key = eventData.key) { isDragging ->
                   val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                   Surface(shadowElevation = elevation) {
                       Row(
                           modifier = Modifier
                               .fillMaxWidth()
                               .animateContentSize()
                               .padding(16.dp).height(35.dp),
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(eventDetails.event.name, style = MaterialTheme.typography.titleMedium)
        AnimatedVisibility(
            visible = !reorderMode,
            enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut()
        ) {
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
}

