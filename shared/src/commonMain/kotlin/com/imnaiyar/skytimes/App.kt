package com.imnaiyar.skytimes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.imnaiyar.skytimes.di.AppContainer
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.di.LocalSettingsViewModel
import com.imnaiyar.skytimes.nav.MainRoute
import com.imnaiyar.skytimes.nav.mainGraph
import com.imnaiyar.skytimes.startup.AppState
import com.imnaiyar.skytimes.theme.AppTheme

val NavController =
    staticCompositionLocalOf<NavHostController> {
        error("No NavController provided")
    }

@ExperimentalMaterial3Api
@Composable
fun App() {
    SideEffect {
        println("FIRST COMPOSITION")
    }
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
            ) {
                SplashScreen(
                    message = if (appState is AppState.Error)
                        (appState as AppState.Error).message
                    else null,
                    isError = appState is AppState.Error,
                    onRetry = if (appState is AppState.Error)
                        appViewModel::retry
                    else null,
                )
            }

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

                val navController = rememberNavController()
                AppTheme(themeMode = settings.themeMode, theme.contrast, theme.color) {
                    Box {
                        val settingsViewModel = viewModel {
                            appContainer.createSettingsViewModel()
                        }

                        CompositionLocalProvider(
                            LocalSettingsViewModel provides settingsViewModel,
                            NavController provides navController
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = MainRoute
                            ) {
                                mainGraph()
                            }

                            // Reveal from top to bottom
                        }
                    }
                }
            }
        }

    }

}
