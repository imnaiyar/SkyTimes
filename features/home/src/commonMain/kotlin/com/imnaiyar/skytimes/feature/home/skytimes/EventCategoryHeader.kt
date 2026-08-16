package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.navigation.AppTutorialStep
import com.imnaiyar.skytimes.core.onboarding.TutorialTarget
import org.jetbrains.compose.resources.painterResource
import com.imnaiyar.skytimes.feature.home.generated.resources.Res
import com.imnaiyar.skytimes.feature.home.generated.resources.close
import com.imnaiyar.skytimes.feature.home.generated.resources.list_arrow

/**
 * Header for categorized events, for example, "Pinned", "Others", etc
 */
@Composable
fun EventCategoryHeader(
    reorderMode: Boolean,
    dimmed: Boolean,
    tutorialTargetsEnabled: Boolean,
    headerTitle: String,
    index: Int = 0,
    onToggleReorderMode: () -> Unit,
) {
    val alpha by animateFloatAsState(
        targetValue = if (dimmed) 0.35f else 1f,
        animationSpec = tween(durationMillis = 300),
    )
    val topShape = if (index == 0) GRID_ITEM_TOP_PADDING else Grid_ITEM_PADDING

    Row(
        modifier = Modifier.fillMaxWidth().background(
            GRID_ITEM_BG_COLOR,
            RoundedCornerShape(
                topStart = topShape,
                topEnd = topShape,
                bottomEnd = Grid_ITEM_PADDING,
                bottomStart = Grid_ITEM_PADDING
            )
        ).padding(4.dp).graphicsLayer { this.alpha = alpha },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionHeader(headerTitle)

        if (index == 0) {
            TutorialTarget(
                id = AppTutorialStep.HomeReorder.targetId,
                enabled = tutorialTargetsEnabled
            ) {
                IconButton(onClick = onToggleReorderMode) {
                    Icon(
                        painter = painterResource(if (reorderMode) Res.drawable.close else Res.drawable.list_arrow),
                        contentDescription = "Reorder Mode Button",
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp),
    )
}