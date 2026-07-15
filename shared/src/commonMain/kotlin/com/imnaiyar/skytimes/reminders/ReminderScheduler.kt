package com.imnaiyar.skytimes.reminders

import androidx.compose.runtime.Composable
import com.imnaiyar.skytimes.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope

interface ReminderScheduler {
    suspend fun refresh()
    suspend fun scheduleReminder(reminder: Reminder)
    suspend fun cancelReminder(eventId: String)
    suspend fun cancelAll()
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean

    fun hasExactAlarm(): Boolean
    fun requestExactAlarm(): Unit
}


@Composable
expect fun rememberNotificationPermissionRequester(): ((Boolean) -> Unit) -> Unit

expect fun getReminderSchedular(
    settingsRepository: SettingsRepository,
    reminderRepository: ReminderRepository,
    scope: CoroutineScope
): ReminderScheduler
