package com.imnaiyar.skytimes.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey

@Serializable
data object MainRoute : AppRoute

@Serializable
data object VaultRoute : AppRoute

@Serializable
data object ThemeSettingsRoute : AppRoute
