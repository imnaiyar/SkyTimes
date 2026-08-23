package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.navigation.AppTutorialStep
import com.imnaiyar.skytimes.core.onboarding.TutorialTarget
import com.imnaiyar.skytimes.core.ui.animated.rotatingBorder
import com.imnaiyar.skytimes.core.common.TimeFormatter
import com.imnaiyar.skytimes.core.domain.EventTimeUtils
import com.imnaiyar.skytimes.core.domain.Times
import com.imnaiyar.skytimes.core.ui.contextClickable
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyGridState
import com.imnaiyar.skytimes.feature.home.generated.resources.Res
import com.imnaiyar.skytimes.feature.home.generated.resources.drag_indicator
import kotlin.time.Instant

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
    onClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onPinToggle: () -> Unit,
    onReminderClick: () -> Unit,
    isLast: Boolean
) {
    ReorderableItem(reorderableLazyGridState, key = row.eventData.key) { isDragging ->
        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

        val now = nowState.value
        val eventDetails = remember(row.eventData, now) {
            EventTimeUtils.getEventDetails(row.eventData, now, includeAllOccurrences = false)
        }
        // Still treat as inactive while reordering — that's handled by the && below.
        val isActive = eventDetails.status is Times.Active && !reorderMode

        val rowScale by animateFloatAsState(
            targetValue = if (isMenuOpen) 1.06f else 1f,
            animationSpec = tween(durationMillis = 500),
        )
        val rowAlpha by animateFloatAsState(
            targetValue = if (isDimmed) 0.35f else 1f,
            animationSpec = tween(durationMillis = 300),
        )

        /** top item will always be a category label, so its corner is handled in [HomeTopBar] */
        val bottomShape = if (isLast) GRID_ITEM_TOP_PADDING else Grid_ITEM_PADDING
        val currentShape = RoundedCornerShape(
            bottomStart = bottomShape,
            bottomEnd = bottomShape,
            topStart = Grid_ITEM_PADDING,
            topEnd = Grid_ITEM_PADDING
        )


        Box {
            TutorialTarget(
                id = AppTutorialStep.HomeEventContextMenu.targetId,
                enabled = isTutorialTarget
            ) {
                Surface(
                    shadowElevation = elevation,
                    shape = currentShape,
                    color = GRID_ITEM_BG_COLOR,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = rowScale
                            scaleY = rowScale
                            alpha = rowAlpha
                        }
                        .rotatingBorder(
                            enabled = isActive,
                            shape = currentShape,
                        )
                        .then(
                            // Long-press context menu is disabled while reordering so it
                            // doesn't fight with the drag gesture.
                            if (reorderMode) {
                                Modifier
                            } else {
                                Modifier.contextClickable(
                                    onClick = onClick,
                                    onLongPress = onLongClick,
                                    onRightClick = onLongClick
                                )
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
                            eventDetails = eventDetails,
                            reorderMode = reorderMode,
                            isActive = isActive,
                            timeFormatter = timeFormatter,
                            now = nowState.value
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
                contentDescription = "Drag to reorder",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}