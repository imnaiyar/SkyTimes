package com.imnaiyar.skytimes.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey

@Serializable
data object MainRoute : AppRoute

@Serializable
data object ThemeSettingsRoute : AppRoute


/** Prevents repeated taps from pushing the same destination more than once. */
fun NavBackStack<NavKey>.navigateTo(route: NavKey) {
    if (lastOrNull() != route) add(route)
}