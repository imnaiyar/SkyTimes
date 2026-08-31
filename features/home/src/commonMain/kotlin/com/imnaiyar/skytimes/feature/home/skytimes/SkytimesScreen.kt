package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
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
import com.imnaiyar.skytimes.core.ui.MinFlowRowWidth
import com.imnaiyar.skytimes.core.ui.rememberTimeFormatter
import com.imnaiyar.skytimes.feature.reminders.ReminderFlowController
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkytimesScreen(
    modifier: Modifier = Modifier,
    fabPad: PaddingValues,
    tutorialTargetsEnabled: Boolean,
    reminderFlow: ReminderFlowController,
) {
    val state = rememberHomeScreenState()
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
    val firstEventCategory by state.firstEventCategory
    val firstEventKey by state.firstEventKey

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val spacing = Arrangement.spacedBy(16.dp)
        // manually calculate column, bcz event column header gets misaligned when there's more than one column in the grid layout
        // so calculate column and replicate column header for each column
        val columnCount =
            ((maxWidth + spacing.spacing) / (MinFlowRowWidth.dp + spacing.spacing)).toInt()
                .coerceAtLeast(1)
        Grid(
            state = lazyGridState,
            columns = StaggeredGridCells.Fixed(columnCount),
            horizontalArrangement = spacing,
            contentPadding = fabPad
        ) {
            items(
                count = columnCount,
                key = { "skytimes-column-header_$it" },
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
                    isTutorialTarget = tutorialTargetsEnabled && section.key == firstEventCategory,
                    onExpandedChange = { expandedSections[section.key] = it },
                ) { row ->
                    EventGridItem(
                        row = row,
                        timeFormatter = timeFormatter,
                        nowState = nowState,
                        isTutorialTarget = tutorialTargetsEnabled && row.eventData.key == firstEventKey,
                        onClick = { state.showEventDetails(row.eventData) },
                        onPinToggle = { state.togglePin(row.eventData.key) },
                        onReminderToggle = { reminderFlow.requestReminderEditor(row.eventData) }
                    )
                }

            }
        }

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
