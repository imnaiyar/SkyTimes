package com.imnaiyar.skytimes.di

import com.imnaiyar.skytimes.settings.SettingsRepository
import com.imnaiyar.skytimes.settings.SettingsViewModel
import com.imnaiyar.skytimes.startup.AppInitializer
import com.imnaiyar.skytimes.startup.AppViewModel
import com.imnaiyar.skytimes.startup.SettingsStartupTask

class AppContainer {
    val settingsRepository = SettingsRepository()

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
}
