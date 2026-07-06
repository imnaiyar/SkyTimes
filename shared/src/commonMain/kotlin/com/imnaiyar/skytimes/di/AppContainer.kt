package com.imnaiyar.skytimes.di

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

class AppContainer(
) {

    val settingsRepository = SettingsRepository()
    val questRepository = QuestRepository()

    val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    val themeController = ThemeController(settingsRepository, applicationScope)
    val clockRepository = ClockRepository(applicationScope)


    val appInitializer = AppInitializer(
        listOf(
            settingsRepository
        )
    )

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
