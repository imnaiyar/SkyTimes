package com.imnaiyar.skytimes.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * The root for one or more screens that can contain tutorial targets. The overlay
 * is placed last, so it draws over and intercepts all normal screen interaction.
 */
@Composable
fun <S : TutorialStep> TutorialHost(
    manager: TutorialManager<S>,
    modifier: Modifier = Modifier,
    autoStart: Boolean = true,
    initialFlowId: String? = null,
    startDelayMillis: Long = DefaultTutorialStartDelayMillis,
    content: @Composable () -> Unit
) {
    val registry = remember { TutorialTargetRegistry() }
    CompositionLocalProvider(LocalTutorialTargetRegistry provides registry) {
        if (autoStart) {
            TutorialLifecycle(
                manager = manager,
                initialFlowId = initialFlowId,
                startDelayMillis = startDelayMillis
            )
        }
        Box(modifier = modifier.fillMaxSize()) {
            content()
            TutorialOverlay(manager = manager, registry = registry)
        }
    }
}
