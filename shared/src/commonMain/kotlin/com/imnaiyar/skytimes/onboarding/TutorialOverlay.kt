package com.imnaiyar.skytimes.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.max

private const val OverlayAnimationMillis = 260
private val TooltipGap = 12.dp
private val ScreenMargin = 16.dp
private val TooltipMaxWidth = 320.dp

/**
 * Draws the coach-mark UI for the manager's current step. No overlay is rendered
 * until the matching TutorialTarget is composed, so screen transitions are safe.
 */
@Composable
fun <S : TutorialStep> TutorialOverlay(
    manager: TutorialManager<S>,
    registry: TutorialTargetRegistry? = LocalTutorialTargetRegistry.current,
    modifier: Modifier = Modifier
) {
    val state by manager.state.collectAsState()
    val step = state.currentStep
    val definition = step?.let(manager::definitionFor)
    val targetBounds = step?.let { registry?.get(it.targetId) }
    val isVisible = definition != null && targetBounds != null

    // Bounds are read from Compose state; a target moving or resizing recomposes this overlay.
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(OverlayAnimationMillis)),
        exit = fadeOut(tween(180)),
        modifier = modifier.fillMaxSize()
    ) {
        if (definition != null && targetBounds != null) {
            SpotlightContent(
                definition = definition,
                targetBounds = targetBounds,
                onGotIt = manager::next,
                onSkip = manager::skip,
                onPrevious = manager::previous,
                canGoPrevious = state.canGoPrevious
            )
        }
    }
}

@Composable
private fun SpotlightContent(
    definition: TutorialDefinition<out TutorialStep>,
    targetBounds: Rect,
    onGotIt: () -> Unit,
    onSkip: () -> Unit,
    onPrevious: () -> Unit,
    canGoPrevious: Boolean
) {
    var hostSize by remember { mutableStateOf(IntSize.Zero) }
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val paddingPx = with(density) { definition.spotlightPadding.toPx() }
    val radiusPx = with(density) { definition.spotlightCornerRadius.toPx() }
    val spotlight = targetBounds.expand(paddingPx, hostSize)
    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f)

    val animatedLeft by animateFloatAsState(
        spotlight.left,
        tween(OverlayAnimationMillis, easing = FastOutSlowInEasing)
    )
    val animatedTop by animateFloatAsState(
        spotlight.top,
        tween(OverlayAnimationMillis, easing = FastOutSlowInEasing)
    )
    val animatedRight by animateFloatAsState(
        spotlight.right,
        tween(OverlayAnimationMillis, easing = FastOutSlowInEasing)
    )
    val animatedBottom by animateFloatAsState(
        spotlight.bottom,
        tween(OverlayAnimationMillis, easing = FastOutSlowInEasing)
    )
    val animatedSpotlight = Rect(animatedLeft, animatedTop, animatedRight, animatedBottom)
    val reveal by animateFloatAsState(
        targetValue = if (hostSize == IntSize.Zero) 0f else 1f,
        animationSpec = tween(OverlayAnimationMillis, easing = FastOutSlowInEasing),
        label = "spotlightReveal"
    )
    val drawnSpotlight = animatedSpotlight.scaleFromCenter(reveal)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { hostSize = it }
    ) {
        // This canvas receives every pointer event, including events inside the clear hole.
        // The card below is a later sibling and remains the only interactive surface.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {})
                }
                .drawWithCache {
                    onDrawBehind {
                        drawRect(scrimColor)
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = Offset(drawnSpotlight.left, drawnSpotlight.top),
                            size = Size(drawnSpotlight.width, drawnSpotlight.height),
                            cornerRadius = CornerRadius(radiusPx, radiusPx),
                            blendMode = BlendMode.Clear
                        )
                    }
                }
        ) { }

        val position = tooltipPosition(
            spotlight = animatedSpotlight,
            hostSize = hostSize,
            tooltipSize = tooltipSize,
            preferred = definition.preferredPlacement,
            density = density
        )
        TutorialTooltip(
            definition = definition,
            onGotIt = onGotIt,
            onSkip = onSkip,
            onPrevious = onPrevious,
            canGoPrevious = canGoPrevious,
            modifier = Modifier
                .onSizeChanged { tooltipSize = it }
                .offset { position }
        )
    }
}

@Composable
private fun TutorialTooltip(
    definition: TutorialDefinition<out TutorialStep>,
    onGotIt: () -> Unit,
    onSkip: () -> Unit,
    onPrevious: () -> Unit,
    canGoPrevious: Boolean,
    modifier: Modifier = Modifier
) {
    val textBtnColor = MaterialTheme.colorScheme.onPrimary
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(220)) + scaleIn(tween(260), initialScale = 0.92f),
        modifier = modifier
    ) {
        ElevatedCard(
            modifier = Modifier.widthIn(max = TooltipMaxWidth).animateContentSize(),
            colors = CardDefaults.cardColors(
                MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                definition.title?.let {
                    Text(it, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                }
                Text(definition.description, style = MaterialTheme.typography.labelMedium)
                definition.gestureHint?.let {
                    Spacer(Modifier.height(12.dp))
                    GestureHint(it)
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canGoPrevious) {
                        TextButton(onClick = onPrevious) { Text("Back", color = textBtnColor) }
                    }
                    TextButton(onClick = onSkip) { Text("Skip", color = textBtnColor) }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onGotIt) {
                        Text(
                            "Got it",
                            color = textBtnColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GestureHint(hint: TutorialGestureHint) {
    when (hint) {
        is TutorialGestureHint.Swipe -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SwipeAnimation(hint.direction)
                hint.label?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(it, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun SwipeAnimation(direction: SwipeDirection) {
    val transition = rememberInfiniteTransition(label = "tutorialSwipe")
    val progress by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "tutorialSwipeProgress"
    )
    val color = MaterialTheme.colorScheme.onPrimary
    Canvas(Modifier.size(48.dp, 36.dp)) {
        val horizontal = direction == SwipeDirection.Left || direction == SwipeDirection.Right
        val start = if (horizontal) Offset(
            size.width * 0.18f,
            size.height / 2f
        ) else Offset(size.width / 2f, size.height * 0.18f)
        val end = if (horizontal) Offset(
            size.width * 0.82f,
            size.height / 2f
        ) else Offset(size.width / 2f, size.height * 0.82f)
        val forward = direction == SwipeDirection.Right || direction == SwipeDirection.Down
        val from = if (forward) start else end
        val to = if (forward) end else start
        val point = Offset(
            x = from.x + (to.x - from.x) * progress,
            y = from.y + (to.y - from.y) * progress
        )
        drawLine(color.copy(alpha = 0.45f), from, to, strokeWidth = 2.dp.toPx())
        drawCircle(color, radius = 5.dp.toPx(), center = point)
        val arrow = 6.dp.toPx()
        val back = if (horizontal) {
            if (forward) -1f else 1f
        } else 0f
        if (horizontal) {
            drawLine(color, to, Offset(to.x + back * arrow, to.y - arrow), 2.dp.toPx())
            drawLine(color, to, Offset(to.x + back * arrow, to.y + arrow), 2.dp.toPx())
        } else {
            val backY = if (forward) -arrow else arrow
            drawLine(color, to, Offset(to.x - arrow, to.y + backY), 2.dp.toPx())
            drawLine(color, to, Offset(to.x + arrow, to.y + backY), 2.dp.toPx())
            if (direction == SwipeDirection.Vertical) {
                drawLine(color, from, Offset(from.x - arrow, from.y - arrow), 2.dp.toPx())
                drawLine(color, from, Offset(from.x + arrow, from.y - arrow), 2.dp.toPx())
            }
        }
    }
}

private fun Rect.expand(padding: Float, hostSize: IntSize): Rect {
    if (hostSize == IntSize.Zero) {
        return Rect(left - padding, top - padding, right + padding, bottom + padding)
    }
    val maxWidth = hostSize.width.toFloat()
    val maxHeight = hostSize.height.toFloat()
    return Rect(
        left = (left - padding).coerceAtLeast(0f),
        top = (top - padding).coerceAtLeast(0f),
        right = (right + padding).coerceAtMost(maxWidth),
        bottom = (bottom + padding).coerceAtMost(maxHeight)
    )
}

private fun Rect.scaleFromCenter(scale: Float): Rect {
    val halfWidth = width * scale / 2f
    val halfHeight = height * scale / 2f
    return Rect(
        center.x - halfWidth,
        center.y - halfHeight,
        center.x + halfWidth,
        center.y + halfHeight
    )
}

private fun tooltipPosition(
    spotlight: Rect,
    hostSize: IntSize,
    tooltipSize: IntSize,
    preferred: TooltipPlacement,
    density: Density
): IntOffset {
    if (hostSize == IntSize.Zero) return IntOffset.Zero
    val margin = with(density) { ScreenMargin.roundToPx() }.toFloat()
    val gap = with(density) { TooltipGap.roundToPx() }.toFloat()
    val tooltipWidth = tooltipSize.width.toFloat()
    val tooltipHeight = tooltipSize.height.toFloat()
    val screenWidth = hostSize.width.toFloat()
    val screenHeight = hostSize.height.toFloat()
    val available = mapOf(
        TooltipPlacement.Above to spotlight.top - gap - margin,
        TooltipPlacement.Below to screenHeight - spotlight.bottom - gap - margin,
        TooltipPlacement.Left to spotlight.left - gap - margin,
        TooltipPlacement.Right to screenWidth - spotlight.right - gap - margin
    )
    val required = mapOf(
        TooltipPlacement.Above to tooltipHeight,
        TooltipPlacement.Below to tooltipHeight,
        TooltipPlacement.Left to tooltipWidth,
        TooltipPlacement.Right to tooltipWidth
    )
    val placement = if ((available[preferred] ?: 0f) >= (required[preferred] ?: 0f)) {
        preferred
    } else {
        available.maxBy { it.value }.key
    }
    val x = when (placement) {
        TooltipPlacement.Above, TooltipPlacement.Below -> spotlight.center.x - tooltipWidth / 2f
        TooltipPlacement.Left -> spotlight.left - gap - tooltipWidth
        TooltipPlacement.Right -> spotlight.right + gap
    }.coerceIn(margin, max(margin, screenWidth - tooltipWidth - margin))
    val y = when (placement) {
        TooltipPlacement.Above -> spotlight.top - gap - tooltipHeight
        TooltipPlacement.Below -> spotlight.bottom + gap
        TooltipPlacement.Left, TooltipPlacement.Right -> spotlight.center.y - tooltipHeight / 2f
    }.coerceIn(margin, max(margin, screenHeight - tooltipHeight - margin))
    return IntOffset(x.toInt(), y.toInt())
}
