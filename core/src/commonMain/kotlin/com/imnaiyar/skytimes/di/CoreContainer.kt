package com.imnaiyar.skytimes.di

import com.imnaiyar.skytimes.repositories.ClockRepository
import com.imnaiyar.skytimes.repositories.SettingsRepository
import com.imnaiyar.skytimes.theme.ThemeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Core services shared by all modules (settings, clock, theme).
 *
 * The app module's [com.imnaiyar.skytimes.di.AppContainer] extends this and adds
 * feature-level wiring (repositories, schedulers, ViewModel factories).
 */
open class CoreContainer {
    val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    val settingsRepository = SettingsRepository()
    val themeController = ThemeController(settingsRepository, applicationScope)
    val clockRepository = ClockRepository(applicationScope)
}
