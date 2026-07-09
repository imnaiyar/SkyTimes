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
import androidx.compose.material3.Text
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
fun _SlidingToggle(
    count: Int,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemWidth: Dp = 48.dp,
    itemHeight: Dp = 48.dp,
    roundedCornerCard: Shape = RoundedCorner,
    roundedCornerIndicator: Shape = RoundedCorner,
    useHaptics: Boolean = false,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable (selected: Boolean, index: Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .height(itemHeight + 12.dp)
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
            targetValue = itemWidth * selectedIndex,
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
                .offset(x = indicatorOffset)
                .size(width = itemWidth, height = itemHeight),
            shape = roundedCornerIndicator,
            color = indicatorColor,
            shadowElevation = indicatorElevation,
            tonalElevation = 3.dp
        ) {}

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(count) { index ->
                val selected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .size(width = itemWidth, height = itemHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectedChange(index)
                            if (useHaptics) {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    content(selected, index)
                }
            }
        }
    }
}


// icon overload
@Composable
fun SlidingToggle(
    icons: List<DrawableResource>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemSize: Dp = 48.dp,
    roundedCornerCard: Shape = RoundedCorner,
    roundedCornerIndicator: Shape = RoundedCorner,
    useHaptics: Boolean = false,
    indicatorColor: Color = MaterialTheme.colorScheme.primary
) {
    _SlidingToggle(
        count = icons.size,
        selectedIndex = selectedIndex,
        onSelectedChange = onSelectedChange,
        modifier = modifier,
        itemWidth = itemSize,
        itemHeight = itemSize,
        roundedCornerCard = roundedCornerCard,
        roundedCornerIndicator = roundedCornerIndicator,
        useHaptics = useHaptics,
        indicatorColor = indicatorColor
    ) { selected, index ->
        Icon(
            painter = painterResource(icons[index]),
            contentDescription = null,
            tint = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// text overload
@Composable
fun SlidingToggle(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemWidth: Dp = 72.dp,
    itemHeight: Dp = 48.dp,
    roundedCornerCard: Shape = RoundedCorner,
    roundedCornerIndicator: Shape = RoundedCorner,
    useHaptics: Boolean = false,
    indicatorColor: Color = MaterialTheme.colorScheme.primary
) {
    _SlidingToggle(
        count = items.size,
        selectedIndex = selectedIndex,
        onSelectedChange = onSelectedChange,
        modifier = modifier,
        itemWidth = itemWidth,
        itemHeight = itemHeight,
        roundedCornerCard = roundedCornerCard,
        roundedCornerIndicator = roundedCornerIndicator,
        useHaptics = useHaptics,
        indicatorColor = indicatorColor
    ) { selected, index ->
        Text(
            text = items[index],
            style = MaterialTheme.typography.labelLarge,
            color = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        SlidingToggle(
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