package com.imnaiyar.skytimes.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.imnaiyar.skytimes.constants.RoundedCorner
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.abs

/**
 * Vertically-swipeable stacked card view.
 *
 * Swipe UP   -> advances to the NEXT day
 * Swipe DOWN -> goes back to the PREVIOUS day
 *
 * You supply [content], which is re-invoked for whichever LocalDate is
 * currently being shown (front card or peeking background card), so you
 * plug in your own per-day layout/data fetch there.
 *
 * Usage:
 *   StackedDateCards(initialDate = today) { date ->
 *       MyDayContent(date)
 *   }
 */
@Composable
fun StackedDateCards(
    initialDate: LocalDate,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 420.dp,
    content: @Composable (date: LocalDate) -> Unit
) {
    var currentDate by remember { mutableStateOf(initialDate) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current
    val dragThresholdPx = with(density) { 120.dp.toPx() }
    val flingDistancePx = with(density) { 900.dp.toPx() }

    // -1f..1f : negative while dragging up (toward next day), positive while dragging down
    val dragProgress = (offsetY.value / dragThresholdPx).coerceIn(-1f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight + 40.dp), // room for the peeking cards behind
        contentAlignment = Alignment.Center
    ) {
        // Deepest peeking card (2 days out in whichever direction is being dragged)
        BackgroundCard(
            date = if (dragProgress < 0) currentDate.plus(2, DateTimeUnit.DAY)
            else currentDate.minus(1, DateTimeUnit.DAY),
            depth = 1,
            progress = abs(dragProgress),
            cardHeight = cardHeight,
            content = content
        )

        // Nearer peeking card (1 day out)
        BackgroundCard(
            date = if (dragProgress < 0) currentDate.plus(1, DateTimeUnit.DAY)
            else currentDate.minus(1, DateTimeUnit.DAY),
            depth = 1,
            progress = abs(dragProgress),
            cardHeight = cardHeight,
            content = content
        )

        // Front card - follows the finger
        Card(
            shape = RoundedCorner,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(cardHeight)
                .zIndex(10f)
                .graphicsLayer {
                    translationY = offsetY.value
                    val shrink = abs(dragProgress) * 0.06f
                    scaleX = 1f - shrink
                    scaleY = 1f - shrink
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetY.value <= -dragThresholdPx -> {
                                        // swiped UP -> next day
                                        offsetY.animateTo(-flingDistancePx, tween(220))
                                        currentDate = currentDate.plus(1, DateTimeUnit.DAY)
                                        offsetY.snapTo(0f)
                                    }

                                    offsetY.value >= dragThresholdPx -> {
                                        // swiped DOWN -> previous day
                                        offsetY.animateTo(flingDistancePx, tween(220))
                                        currentDate = currentDate.minus(1, DateTimeUnit.DAY)
                                        offsetY.snapTo(0f)
                                    }

                                    else -> {
                                        // didn't cross the threshold - snap back
                                        offsetY.animateTo(0f, tween(250))
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetY.animateTo(0f, tween(250)) }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch { offsetY.snapTo(offsetY.value + dragAmount) }
                        }
                    )
                }
        ) {
            Box(Modifier.fillMaxSize().padding(16.dp)) {
                content(currentDate)
            }
        }
    }
}

@Composable
private fun BackgroundCard(
    date: LocalDate,
    depth: Int, // 1 = just behind the front card, 2 = further behind
    progress: Float, // 0f..1f, how far the front card has currently been dragged
    cardHeight: Dp,
    content: @Composable (date: LocalDate) -> Unit
) {
    // Resting stack position: offset down and scaled down a bit per depth level.
    val baseOffset = depth * 16f
    val baseScale = 1f - depth * 0.05f

    // As the front card gets dragged away, animate this card toward the
    // "front" position (offset 0, full scale) so it visually fills in behind it.
    val revealedOffset = baseOffset - (baseOffset * progress)
    val revealedScale = baseScale + (1f - baseScale) * progress * (if (depth == 1) 1f else 0.4f)

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = (4 - depth).dp),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(cardHeight)
            .zIndex((10 - depth).toFloat())
            .graphicsLayer {
                translationY = revealedOffset
                scaleX = revealedScale
                scaleY = revealedScale
                alpha = 0.5f + 0.5f * progress
            }
    ) {
        Box(Modifier.fillMaxSize().padding(16.dp)) {
            content(date)
        }
    }
}