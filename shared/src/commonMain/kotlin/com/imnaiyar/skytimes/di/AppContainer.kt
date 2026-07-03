package com.imnaiyar.skytimes.di

import com.imnaiyar.skytimes.repositories.ClockRepository
import com.imnaiyar.skytimes.repositories.QuestRepository
import com.imnaiyar.skytimes.repositories.SettingsRepository
import com.imnaiyar.skytimes.startup.AppInitializer
import com.imnaiyar.skytimes.startup.SettingsStartupTask
import com.imnaiyar.skytimes.views.AppViewModel
import com.imnaiyar.skytimes.views.QuestsViewModel
import com.imnaiyar.skytimes.views.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer {
    val settingsRepository = SettingsRepository()
    val questRepository = QuestRepository()

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    val clockRepository = ClockRepository(applicationScope)

    private val startupTasks = listOf(
        SettingsStartupTask(settingsRepository)
    )

    val appInitializer = AppInitializer(startupTasks)

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
