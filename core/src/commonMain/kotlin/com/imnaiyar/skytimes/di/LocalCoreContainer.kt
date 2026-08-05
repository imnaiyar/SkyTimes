package com.imnaiyar.skytimes.di

import androidx.compose.runtime.staticCompositionLocalOf
import com.imnaiyar.skytimes.onboarding.AppTutorialStep
import com.imnaiyar.skytimes.onboarding.TutorialManager

/** Provided by the app's [CoreContainer] (which is the AppContainer). */
val LocalCoreContainer =
    staticCompositionLocalOf<CoreContainer> {
        error("No CoreContainer provided")
    }

val LocalTutorialManager =
    staticCompositionLocalOf<TutorialManager<AppTutorialStep>> {
        error("No TutorialManager provided")
    }
