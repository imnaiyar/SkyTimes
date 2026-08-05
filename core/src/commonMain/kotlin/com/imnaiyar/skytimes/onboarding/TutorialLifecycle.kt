package com.imnaiyar.skytimes.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay

/**
 * Starts onboarding after the host has had time to compose and settle. It is safe
 * to keep in the app root: completion is derived from configured step keys, so a
 * newly added step automatically becomes eligible on a later launch. A target may
 * compose after this delay; TutorialOverlay will wait.
 */
@Composable
fun <S : TutorialStep> TutorialLifecycle(
    manager: TutorialManager<S>,
    initialFlowId: String? = null,
    startDelayMillis: Long = DefaultTutorialStartDelayMillis
) {
    require(startDelayMillis >= 0) { "startDelayMillis cannot be negative." }
    val state by manager.state.collectAsState()

    LaunchedEffect(state.isLoaded, state.isTutorialCompleted) {
        if (state.isLoaded && !state.isTutorialCompleted) {
            delay(timeMillis = startDelayMillis)
            val latest = manager.state.value
            if (!latest.isRunning && !latest.isTutorialCompleted) {
                if (initialFlowId == null) manager.start() else manager.start(initialFlowId)
            }
        }
    }
}

const val DefaultTutorialStartDelayMillis = 500L
