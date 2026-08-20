package com.imnaiyar.skytimes.core.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import com.imnaiyar.skytimes.core.onboarding.TutorialManager

/** The app-wide tutorial manager, provided by the app root. */
val LocalTutorialManager = staticCompositionLocalOf<TutorialManager<AppTutorialStep>> {
    error("No TutorialManager provided")
}
