package com.imnaiyar.skytimes.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/** Bounds are in TutorialHost root coordinates, which makes them safe for its overlay. */
@Stable
class TutorialTargetRegistry {
    private val targets = mutableStateMapOf<Any, Rect>()

    operator fun get(id: Any): Rect? = targets[id]

    internal fun update(id: Any, bounds: Rect) {
        targets[id] = bounds
    }

    internal fun remove(id: Any) {
        targets.remove(id)
    }
}

/** Exposed for custom [TutorialOverlay] placement, though TutorialHost supplies it automatically. */
val LocalTutorialTargetRegistry = compositionLocalOf<TutorialTargetRegistry?> { null }

/**
 * Registers the bounds of [content] while it is composed. It is safe to use in a
 * screen without a TutorialHost: it simply behaves like a Box in that case.
 */
@Composable
fun TutorialTarget(
    id: Any,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val registry = LocalTutorialTargetRegistry.current
    DisposableEffect(id, registry, enabled) {
        if (!enabled) registry?.remove(id)
        onDispose { registry?.remove(id) }
    }
    Box(
        modifier = modifier.then(
            if (enabled) {
                Modifier.onGloballyPositioned { coordinates ->
                    registry?.update(id, coordinates.boundsInRoot())
                }
            } else {
                Modifier
            }
        )
    ) {
        content()
    }
}
