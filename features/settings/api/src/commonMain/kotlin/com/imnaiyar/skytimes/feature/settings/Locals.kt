package com.imnaiyar.skytimes.feature.settings

import androidx.compose.runtime.staticCompositionLocalOf

/** The settings view model, provided by the app root. */
val LocalSettingsViewModel = staticCompositionLocalOf<SettingsViewModel> {
    error("No SettingsViewModel provided")
}

/** The theme controller, provided by the app root. */
val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("No ThemeController provided")
}
