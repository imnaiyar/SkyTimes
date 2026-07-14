package com.imnaiyar.skytimes.nav

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.imnaiyar.skytimes.screens.MainScreen
import com.imnaiyar.skytimes.screens.ThemePage
import com.imnaiyar.skytimes.vault_archive.MainArchive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Explicit polymorphic registration keeps navigation state restorable on every
 * supported Compose Multiplatform target, including iOS and web.
 */
private val appNavigationStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(MainRoute::class, MainRoute.serializer())
            subclass(VaultRoute::class, VaultRoute.serializer())
            subclass(ThemeSettingsRoute::class, ThemeSettingsRoute.serializer())
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(appNavigationStateConfiguration, MainRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = { _ ->
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        entryProvider = entryProvider {
            entry<MainRoute> {
                MainScreen(
                    onOpenVault = { backStack.navigateTo(VaultRoute) },
                    onOpenThemeSettings = { backStack.navigateTo(ThemeSettingsRoute) },
                    backStack
                )
            }
            entry<VaultRoute> {
                MainArchive()
            }
            entry<ThemeSettingsRoute> {
                ThemePage(onNavigateBack = { backStack.removeLastOrNull() })
            }
        }
    )
}

/** Prevents repeated taps from pushing the same destination more than once. */
private fun NavBackStack<NavKey>.navigateTo(route: AppRoute) {
    if (lastOrNull() != route) add(route)
}
