package com.imnaiyar.skytimes.ui.animated


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * List of colors that can be blended to indicate active state
 */
val ActiveColorBlend
    @Composable
    get() = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer,
    )

@Composable
fun Modifier.rotatingBorder(
    enabled: Boolean = true,
    colors: List<Color> = ActiveColorBlend.map { it.copy(0.5f) },
    strokeWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    speed: Int = 3000,
): Modifier {
    if (!enabled) return this
    val brush = Brush.linearGradient(colors)
    val angle = rememberInfiniteTransition(label = "angle")
        .animateFloat(
            360f,
            0f,
            animationSpec = infiniteRepeatable(
                tween(speed, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "angle_change"
        )

    return this
        .clip(shape)
        .padding(strokeWidth)
        .drawWithContent {
            rotate(angle.value) {
                drawCircle(brush, size.width)
            }
            drawContent()
        }
}
