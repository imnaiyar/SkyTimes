package com.imnaiyar.skytimes.reminder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * No-op [ReminderScheduler] for JS/Wasm targets.
 */
internal class NoOpReminderScheduler : ReminderScheduler {

    override suspend fun refresh() = Unit

    override suspend fun scheduleReminder(reminder: Reminder) = Unit

    override suspend fun cancelReminder(eventId: String) = Unit

    override suspend fun cancelAll() = Unit
}

actual fun createPlatformReminderScheduler(
    repository: ReminderRepository,
    notificationManager: NotificationManager,
): ReminderScheduler = NoOpReminderScheduler()
