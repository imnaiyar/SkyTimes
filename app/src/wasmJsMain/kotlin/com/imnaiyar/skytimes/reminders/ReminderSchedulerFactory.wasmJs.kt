package com.imnaiyar.skytimes.reminders

import com.imnaiyar.skytimes.feature.reminders.NoOpReminderScheduler
import com.imnaiyar.skytimes.feature.reminders.ReminderRepository
import com.imnaiyar.skytimes.feature.reminders.ReminderScheduler
import com.imnaiyar.skytimes.feature.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope

actual fun getReminderSchedular(
    settingsRepository: SettingsRepository,
    reminderRepository: ReminderRepository,
    scope: CoroutineScope
): ReminderScheduler {
    return NoOpReminderScheduler
}
