package com.imnaiyar.skytimes.nav

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.home.MainScreen
import com.imnaiyar.skytimes.reminders.ui.rememberReminderFlow
import com.imnaiyar.skytimes.settings.ThemePage
import com.imnaiyar.skytimes.vault.VaultArchive
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

    val appContainer = LocalAppContainer.current
    val questsViewModel = viewModel { appContainer.createQuestsViewModel() }
    val settingsViewModel = viewModel { appContainer.createSettingsViewModel() }
    val reminderFlow = rememberReminderFlow(
        scope = appContainer.applicationScope,
        settingsRepository = appContainer.settingsRepository,
        reminderRepository = appContainer.reminderRepository,
        reminderScheduler = appContainer.reminderScheduler,
    )

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
                    settingsViewModel = settingsViewModel,
                    questsViewModel = questsViewModel,
                    reminderFlow = reminderFlow,
                    onOpenVault = { backStack.navigateTo(VaultRoute) },
                    onOpenThemeSettings = { backStack.navigateTo(ThemeSettingsRoute) },
                    backStack = backStack,
                    isOnMainRoute = backStack.lastOrNull() is MainRoute,
                )
            }
            entry<VaultRoute> {
                VaultArchive()
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
