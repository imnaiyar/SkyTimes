package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imnaiyar.skytimes.core.ui.Card
import com.imnaiyar.skytimes.core.ui.theme.titleTiny
import com.imnaiyar.skytimes.feature.home.generated.resources.Res
import com.imnaiyar.skytimes.feature.home.generated.resources.drag_indicator
import com.imnaiyar.skytimes.feature.home.generated.resources.list_arrow
import org.jetbrains.compose.resources.painterResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyStaggeredGridState

@Composable
internal fun LazyStaggeredGridItemScope.EventCategoryCard(
    section: IRow.Section,
    reorderableLazyGridState: ReorderableLazyStaggeredGridState,
    dimmed: Boolean,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    eventContent: @Composable (IRow.Event) -> Unit,
) {
    val card: @Composable (showDragHandle: Boolean, dragHandle: @Composable () -> Unit) -> Unit =
        { showDragHandle, dragHandle ->
            Card(
                modifier = Modifier.fillMaxWidth()
                    .graphicsLayer { alpha = if (dimmed) .35f else 1f }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (showDragHandle) dragHandle()

                        Text(
                            text = "${section.title} (${section.eventRows.size})",
                            style = MaterialTheme.typography.titleTiny(1.sp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    IconButton(onClick = { onExpandedChange(!isExpanded) }) {
                        Icon(
                            painter = painterResource(Res.drawable.list_arrow),
                            contentDescription = if (isExpanded) "Collapse ${section.title}" else "Expand ${section.title}",
                            modifier = Modifier.graphicsLayer {
                                rotationZ = if (isExpanded) 90f else 0f
                            },
                        )
                    }
                }
                AnimatedVisibility(visible = isExpanded) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        for (event in section.eventRows) eventContent(event)
                    }
                }
            }
        }

    if (section.isPinnedSection) {
        card(false) {}
    } else {
        ReorderableItem(reorderableLazyGridState, key = section.key) {
            card(true) { CategoryDragHandle() }
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.CategoryDragHandle() {
    IconButton(modifier = Modifier.draggableHandle(), onClick = {}) {
        Icon(
            painterResource(Res.drawable.drag_indicator),
            "Drag to reorder",
            Modifier.size(18.dp)
        )
    }
}
