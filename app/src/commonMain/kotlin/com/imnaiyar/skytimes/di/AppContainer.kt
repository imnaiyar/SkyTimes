package com.imnaiyar.skytimes.di

import com.imnaiyar.skytimes.core.navigation.AppTutorialStep
import com.imnaiyar.skytimes.core.onboarding.TutorialManager
import com.imnaiyar.skytimes.feature.reminders.ReminderRepository
import com.imnaiyar.skytimes.feature.reminders.ReminderScheduler
import com.imnaiyar.skytimes.feature.settings.SettingsNotificationsToggle
import com.imnaiyar.skytimes.feature.settings.SettingsRepository
import com.imnaiyar.skytimes.feature.settings.SettingsViewModel
import com.imnaiyar.skytimes.feature.settings.ThemeController
import com.imnaiyar.skytimes.onboarding.FirstLaunchTutorialFlow
import com.imnaiyar.skytimes.reminders.getReminderSchedular
import com.imnaiyar.skytimes.feature.quests.QuestRepository
import com.imnaiyar.skytimes.core.common.AppInitializer
import com.imnaiyar.skytimes.core.data.ClockRepository
import com.imnaiyar.skytimes.views.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer() {

    val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    val settingsRepository = SettingsRepository()
    val questRepository = QuestRepository()
    val reminderRepository = ReminderRepository()
    val reminderScheduler: ReminderScheduler =
        getReminderSchedular(settingsRepository, reminderRepository, applicationScope)

    val themeController = ThemeController(settingsRepository, applicationScope)
    val clockRepository = ClockRepository(applicationScope)
    val notificationsToggle = SettingsNotificationsToggle(settingsRepository)


    val appInitializer = AppInitializer(
        listOf(
            settingsRepository,
            reminderRepository
        )
    )

    /** Created after startup has loaded SettingsRepository, when App first accesses it. */
    val tutorialManager by lazy {
        TutorialManager(
            flows = listOf(FirstLaunchTutorialFlow),
            scope = applicationScope,
            repository = settingsRepository
        )
    }

    fun createAppViewModel(): AppViewModel {
        return AppViewModel(appInitializer)
    }

    fun createSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(settingsRepository)
    }
}
