package com.imnaiyar.skytimes.startup

import com.imnaiyar.skytimes.settings.SettingsRepository

class SettingsStartupTask(
    private val settingsRepository: SettingsRepository
) : StartupTask {
    override val name: String = "Load settings"
    override val critical: Boolean = true

    override suspend fun run() {
        settingsRepository.initialize()
    }
}
