package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.data.LocalClockRepository
import com.imnaiyar.skytimes.core.ui.rememberTimeFormatter
import com.imnaiyar.skytimes.feature.reminders.rememberReminderFlow
import com.imnaiyar.skytimes.core.ui.Grid
import com.imnaiyar.skytimes.core.ui.GridType
import sh.calvin.reorderable.rememberReorderableLazyGridState

@Composable
fun SkytimesScreen(
    modifier: Modifier = Modifier,
    setFabVisible: (Boolean) -> Unit,
    fabPad: PaddingValues,
    tutorialTargetsEnabled: Boolean,
) {
    val state = rememberHomeScreenState()
    val reminderFlow = rememberReminderFlow()
    val nowState = LocalClockRepository.current.observeEveryMinute()
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
                    is IRow.Header -> EventCategoryHeader(
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