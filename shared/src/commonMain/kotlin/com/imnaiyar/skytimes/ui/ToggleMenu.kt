package com.imnaiyar.skytimes.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.RoundedCorner
import com.imnaiyar.skytimes.theme.DefaultThemeColor
import com.imnaiyar.skytimes.theme.appTypography
import com.materialkolor.Contrast
import com.materialkolor.rememberDynamicColorScheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.chevron_right

@Composable
fun SlidingIconToggle(
    icons: List<DrawableResource>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemSize: Dp = 48.dp,
    roundedCornerCard: Shape = RoundedCorner,
    roundedCornerIndicator: Shape = RoundedCorner,
    usehaptics: Boolean = false,
    indicatorColor: Color = MaterialTheme.colorScheme.primary
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .height(itemSize + 12.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = roundedCornerCard
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = roundedCornerCard
            )
            .padding(6.dp)
    ) {

        val indicatorOffset by animateDpAsState(
            targetValue = itemSize * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "indicatorOffset"
        )


        val indicatorElevation by animateDpAsState(
            targetValue = 20.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "indicatorElevation"
        )

        Surface(
            modifier = Modifier
                .offset(
                    x = indicatorOffset
                )
                .size(itemSize),
            shape = roundedCornerIndicator,
            color = indicatorColor,
            shadowElevation = indicatorElevation,
            tonalElevation = 3.dp
        ) {}

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            icons.forEachIndexed { index, icon ->

                val selected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .size(itemSize)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectedChange(index)
                            if (usehaptics) haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = if (selected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


/**
 * Example usage with three icons.
 */
@Composable
@Preview
fun SlidingIconToggleExample() {
    var selected by remember { mutableStateOf(0) }

    val color = rememberDynamicColorScheme(
        Color(DefaultThemeColor.toInt()),
        true,
        contrastLevel = Contrast.Default.value
    )

    MaterialTheme(
        colorScheme = color,
        typography = appTypography()
    ) {
        SlidingIconToggle(
            icons = listOf(
                Res.drawable.chevron_right,
                Res.drawable.chevron_right,
                Res.drawable.chevron_right
            ),
            selectedIndex = selected,
            onSelectedChange = { selected = it }
        )
    }

}