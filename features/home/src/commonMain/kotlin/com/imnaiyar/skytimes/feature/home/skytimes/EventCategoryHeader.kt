package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imnaiyar.skytimes.core.navigation.AppTutorialStep
import com.imnaiyar.skytimes.core.onboarding.TutorialTarget
import com.imnaiyar.skytimes.core.ui.Card
import com.imnaiyar.skytimes.core.ui.generated.resources.chevron_right
import com.imnaiyar.skytimes.core.ui.theme.titleTiny
import com.imnaiyar.skytimes.feature.home.generated.resources.Res
import com.imnaiyar.skytimes.feature.home.generated.resources.drag_indicator
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyStaggeredGridState
import com.imnaiyar.skytimes.core.ui.generated.resources.Res as CoreRes

@Composable
internal fun LazyStaggeredGridItemScope.EventCategoryCard(
    section: IRow.Section,
    reorderableLazyGridState: ReorderableLazyStaggeredGridState,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isTutorialTarget: Boolean,
    eventContent: @Composable (IRow.Event) -> Unit,
) {
    val rotate by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "CategoryRotation",
    )

    val rows = if (section.isPinnedSection) section.eventRows
    else section.eventRows.filter { !it.isPinned };

    val card: @Composable (showDragHandle: Boolean, startPad: Dp?, dragHandle: @Composable () -> Unit) -> Unit =
        { showDragHandle, startPad, dragHandle ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .defaultMinSize(minHeight = 50.dp)
                        .clickable(onClick = { onExpandedChange(!isExpanded) })
                        .padding(start = startPad ?: 4.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showDragHandle) dragHandle()

                        Text(
                            text = "${section.title} (${rows.size})",
                            style = MaterialTheme.typography.titleTiny(1.sp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Icon(
                        painter = painterResource(CoreRes.drawable.chevron_right),
                        contentDescription = if (isExpanded) "Collapse ${section.title}" else "Expand ${section.title}",
                        modifier = Modifier.rotate(rotate),
                    )

                }
                AnimatedVisibility(visible = isExpanded) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        for (event in rows) eventContent(event)
                    }
                }
            }
        }

    if (section.isPinnedSection) {
        card(false, 15.dp) {}
    } else {
        ReorderableItem(reorderableLazyGridState, key = section.key) {
            card(true, null) { CategoryDragHandle(isTutorialTarget) }
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.CategoryDragHandle(isTarget: Boolean) {
    TutorialTarget(AppTutorialStep.EventCategoryDragHandle.targetId, enabled = isTarget) {
        IconButton(
            modifier = Modifier.draggableHandle(dragGestureDetector = DragGestureDetector.LongPress),
            onClick = {}) {
            Icon(
                painterResource(Res.drawable.drag_indicator),
                "Drag to reorder",
                Modifier.size(18.dp)
            )
        }
    }
}
