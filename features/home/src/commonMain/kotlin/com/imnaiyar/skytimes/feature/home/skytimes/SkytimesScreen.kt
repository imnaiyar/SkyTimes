package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.data.LocalClockRepository
import com.imnaiyar.skytimes.core.domain.EventTimeUtils
import com.imnaiyar.skytimes.core.ui.Grid
import com.imnaiyar.skytimes.core.ui.rememberTimeFormatter
import com.imnaiyar.skytimes.feature.reminders.rememberReminderFlow
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkytimesScreen(
    modifier: Modifier = Modifier,
    setFabVisible: (Boolean) -> Unit,
    fabPad: PaddingValues,
    tutorialTargetsEnabled: Boolean,
) {
    val state = rememberHomeScreenState()
    val reminderFlow = rememberReminderFlow()
    val nowState = LocalClockRepository.current.now.collectAsState()

    val timeFormatter = rememberTimeFormatter()

    val lazyGridState = rememberLazyStaggeredGridState()
    val reorderableLazyGridState =
        rememberReorderableLazyStaggeredGridState(lazyGridState) { from, to ->
            state.onMove(from.key, to.key)
        }

    val sheetState = rememberModalBottomSheetState()
    val expandedSections = remember { mutableStateMapOf<Any, Boolean>() }

    val rows by state.rows
    val firstEventKey by state.firstEventKey

    Box(modifier = modifier.fillMaxSize()) {
        Grid(
            state = lazyGridState,
            contentPadding = fabPad
        ) {
            item(
                key = "skytimes-column-header",
                span = StaggeredGridItemSpan.FullLine,
            ) {
                SkyClockColumnHeader()
            }

            items(
                count = rows.size,
                key = { rows[it].key },
            ) { index ->
                val section = rows[index]
                EventCategoryCard(
                    section = section,
                    reorderableLazyGridState = reorderableLazyGridState,
                    isExpanded = expandedSections[section.key] ?: true,
                    onExpandedChange = { expandedSections[section.key] = it },
                ) { row ->
                    EventGridItem(
                        row = row,
                        isTutorialTarget = tutorialTargetsEnabled && row.eventData.key == firstEventKey,
                        timeFormatter = timeFormatter,
                        nowState = nowState,
                        onClick = { state.showEventDetails(row.eventData) },
                        onPinToggle = { state.togglePin(row.eventData.key) },
                        onReminderToggle = { reminderFlow.requestReminderEditor(row.eventData) }
                    )
                }

            }
        }

        // Reminder-related dialogs (e.g. exact-alarm permission prompts).
        reminderFlow.RenderDialogs()

        state.eventDetailsToShow?.let { eventData ->
            val eventDetails = remember(eventData, nowState.value) {
                EventTimeUtils.getEventDetails(
                    eventData,
                    nowState.value,
                    includeAllOccurrences = true
                )
            }
            EventDetailsBottomSheet(
                eventDetails = eventDetails,
                sheetState = sheetState,
                now = nowState.value,
                onDismiss = state::dismissEventDetails
            )
        }
    }
}

@Composable
private fun SkyClockColumnHeader() {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    EventColumnLayout(
        modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp),
        eventName = {
            Text(
                text = "Event name",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
            )
        },
        nextTime = {
            Text(
                text = "Next time",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
            )
        },
        remaining = {
            Text(
                text = "Remaining",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
            )
        },
        toggles = {},
    )
}
