package com.imnaiyar.skytimes.di

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppContainer =
    staticCompositionLocalOf<AppContainer> {
        error("No AppContainer provided")
    }
