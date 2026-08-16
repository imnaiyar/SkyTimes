package com.imnaiyar.skytimes.reminders

import com.imnaiyar.skytimes.feature.reminders.IosReminderScheduler
import com.imnaiyar.skytimes.feature.reminders.ReminderRepository
import com.imnaiyar.skytimes.feature.reminders.ReminderScheduler
import com.imnaiyar.skytimes.feature.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

actual fun getReminderSchedular(
    settingsRepository: SettingsRepository,
    reminderRepository: ReminderRepository,
    scope: CoroutineScope
): ReminderScheduler {
    val scheduler = IosReminderScheduler(settingsRepository, reminderRepository, scope)
    IosReminderBridge.install(scheduler)
    return scheduler
}

/**
 * Called by the iOS app (AppDelegate) to refresh reminders from a
 * BGAppRefreshTask, so it lives in the app module and is exported in the
 * Shared framework.
 */
object IosReminderBridge {
    private val bridgeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** This is initialized in [getReminderSchedular] which is initialized once in the app container. */
    private var scheduler: IosReminderScheduler? = null

    fun install(scheduler: IosReminderScheduler) {
        this.scheduler = scheduler
    }

    fun refresh() {
        scheduler?.refreshNow()
    }

    fun requestPermission() {
        scheduler?.requestPermissionNow()
    }
}
