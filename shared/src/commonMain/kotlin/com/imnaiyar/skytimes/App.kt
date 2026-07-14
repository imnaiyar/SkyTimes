package com.imnaiyar.skytimes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnaiyar.skytimes.di.AppContainer
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.di.LocalSettingsViewModel
import com.imnaiyar.skytimes.di.LocalTutorialManager
import com.imnaiyar.skytimes.nav.AppNavigation
import com.imnaiyar.skytimes.onboarding.TutorialHost
import com.imnaiyar.skytimes.screens.SplashScreen
import com.imnaiyar.skytimes.startup.AppState
import com.imnaiyar.skytimes.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun App() {
    val appContainer = remember { AppContainer() }
    val appViewModel = viewModel { appContainer.createAppViewModel() }
    val appState by appViewModel.state.collectAsState()
    val progress by animateFloatAsState(
        targetValue = if (appState is AppState.Ready) 1f else 0f,
        animationSpec = tween(600)
    )

    when (appState) {
        AppState.Loading ->
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent() {
                        clipRect(
                            top = size.height * progress,
                            bottom = size.height
                        ) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) { SplashScreen() }

        is AppState.Error -> SplashScreen(
            message = (appState as AppState.Error).message,
            isError = true,
            onRetry = appViewModel::retry
        )

        is AppState.Ready -> {
            CompositionLocalProvider(
                LocalAppContainer provides appContainer
            ) {

                val settings by appContainer.settingsRepository.settings.collectAsState()
                val theme by appContainer.themeController.theme.collectAsState()

                // Initialise the reminder subsystem once startup completes.
                LaunchedEffect(Unit) {
                    appContainer.reminderManager.initialize()
                }

                // React to the master Notifications toggle in Settings.
                LaunchedEffect(settings.notificationsEnabled) {
                    appContainer.reminderManager.masterEnabled = settings.notificationsEnabled
                    appContainer.reminderManager.refresh()
                }

                AppTheme(themeMode = settings.themeMode, theme.contrast, theme.color) {
                    Box {
                        val settingsViewModel = viewModel {
                            appContainer.createSettingsViewModel()
                        }

                        CompositionLocalProvider(
                            LocalSettingsViewModel provides settingsViewModel,
                            LocalTutorialManager provides appContainer.tutorialManager
                        ) {
                            TutorialHost(manager = appContainer.tutorialManager) {
                                AppNavigation()
                            }

                            // Reveal from top to bottom
                        }
                    }
                }
            }
        }

    }

}
