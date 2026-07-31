package com.imnaiyar.skytimes.reminders

import androidx.compose.runtime.Composable
import com.imnaiyar.skytimes.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope

actual fun getReminderSchedular(
    settingsRepository: SettingsRepository,
    reminderRepository: ReminderRepository,
    scope: CoroutineScope
): ReminderScheduler {
    return NoOpReminderScheduler
}

@Composable
actual fun rememberNotificationPermissionRequester(): ((Boolean) -> Unit) -> Unit = {}