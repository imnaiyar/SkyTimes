package com.imnaiyar.skytimes.di

import androidx.compose.runtime.staticCompositionLocalOf
import com.imnaiyar.skytimes.views.SettingsViewModel

val LocalAppContainer =
    staticCompositionLocalOf<AppContainer> {
        error("No AppContainer provided")
    }

val LocalSettingsViewModel =
    staticCompositionLocalOf<SettingsViewModel> {
        error("No SettingsViewModel provided")
    }
