package com.imnaiyar.skytimes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
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
import com.imnaiyar.skytimes.core.common.LocalApplicationScope
import com.imnaiyar.skytimes.core.common.LocalSnackBarState
import com.imnaiyar.skytimes.core.data.LocalClockRepository
import com.imnaiyar.skytimes.core.data.LocalSkyDataRepository
import com.imnaiyar.skytimes.core.navigation.LocalTutorialManager
import com.imnaiyar.skytimes.core.onboarding.TutorialHost
import com.imnaiyar.skytimes.core.ui.LocalClockAnimation
import com.imnaiyar.skytimes.core.ui.LocalUse24HourClock
import com.imnaiyar.skytimes.core.ui.theme.AppTheme
import com.imnaiyar.skytimes.di.AppContainer
import com.imnaiyar.skytimes.feature.home.shards.LocalShardDate
import com.imnaiyar.skytimes.feature.home.shards.ShardDateState
import com.imnaiyar.skytimes.feature.quests.LocalQuestRepository
import com.imnaiyar.skytimes.feature.reminders.LocalNotificationsToggle
import com.imnaiyar.skytimes.feature.reminders.LocalReminderRepository
import com.imnaiyar.skytimes.feature.reminders.LocalReminderScheduler
import com.imnaiyar.skytimes.feature.settings.LocalSettingsViewModel
import com.imnaiyar.skytimes.feature.settings.LocalThemeController

@ExperimentalMaterial3Api
@Composable
fun App() {
    val appContainer = remember { AppContainer() }
    val shardDateState = remember { ShardDateState() }
    val appViewModel = viewModel { appContainer.createAppViewModel() }
    val appState by appViewModel.state.collectAsState()
    val settings by appContainer.settingsRepository.settings.collectAsState()
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
            LaunchedEffect(appState, settings.notificationsEnabled) {
                if (settings.notificationsEnabled) {
                    appContainer.reminderScheduler.refresh()
                } else {
                    appContainer.reminderScheduler.cancelAll()
                }
            }

            val settings by appContainer.settingsRepository.settings.collectAsState()

            val theme by appContainer.themeController.theme.collectAsState()
            AppTheme(themeMode = settings.themeMode, theme.contrast, theme.color) {
                Box {
                    val settingsViewModel = viewModel {
                        appContainer.createSettingsViewModel()
                    }


                    CompositionLocalProvider(
                        LocalSettingsViewModel provides settingsViewModel,
                        LocalTutorialManager provides appContainer.tutorialManager,
                        LocalClockRepository provides appContainer.clockRepository,
                        LocalQuestRepository provides appContainer.questRepository,
                        LocalThemeController provides appContainer.themeController,
                        LocalReminderRepository provides appContainer.reminderRepository,
                        LocalReminderScheduler provides appContainer.reminderScheduler,
                        LocalNotificationsToggle provides appContainer.notificationsToggle,
                        LocalShardDate provides shardDateState,
                        LocalUse24HourClock provides settings.use24HourClock,
                        LocalClockAnimation provides settings.clockAnimation,
                        LocalApplicationScope provides appContainer.applicationScope,
                        LocalSnackBarState provides remember { SnackbarHostState() },
                        LocalSkyDataRepository provides appContainer.skyDataRepository
                    ) {
                        TutorialHost(manager = appContainer.tutorialManager) {
                            AppNavigation()
                        }
                    }
                }
            }
        }

    }

}
