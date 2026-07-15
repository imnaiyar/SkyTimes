package com.imnaiyar.skytimes.di

import com.imnaiyar.skytimes.onboarding.FirstLaunchTutorialFlow
import com.imnaiyar.skytimes.onboarding.TutorialManager
import com.imnaiyar.skytimes.reminders.ReminderRepository
import com.imnaiyar.skytimes.reminders.ReminderScheduler
import com.imnaiyar.skytimes.reminders.getReminderSchedular
import com.imnaiyar.skytimes.repositories.ClockRepository
import com.imnaiyar.skytimes.repositories.QuestRepository
import com.imnaiyar.skytimes.repositories.SettingsRepository
import com.imnaiyar.skytimes.startup.AppInitializer
import com.imnaiyar.skytimes.theme.ThemeController
import com.imnaiyar.skytimes.views.AppViewModel
import com.imnaiyar.skytimes.views.QuestsViewModel
import com.imnaiyar.skytimes.views.SettingsViewModel
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

    fun createQuestsViewModel(): QuestsViewModel {
        return QuestsViewModel(questRepository)
    }
}
