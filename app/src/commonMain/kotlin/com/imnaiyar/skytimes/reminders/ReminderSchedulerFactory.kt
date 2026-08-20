package com.imnaiyar.skytimes.reminders

import com.imnaiyar.skytimes.feature.reminders.ReminderRepository
import com.imnaiyar.skytimes.feature.reminders.ReminderScheduler
import com.imnaiyar.skytimes.feature.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope

/**
 * Platform entry point for the reminder scheduler. The composition root owns
 * this factory so no feature module depends on platform scheduling code.
 */
expect fun getReminderSchedular(
    settingsRepository: SettingsRepository,
    reminderRepository: ReminderRepository,
    scope: CoroutineScope
): ReminderScheduler
