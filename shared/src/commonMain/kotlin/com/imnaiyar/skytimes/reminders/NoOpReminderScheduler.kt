package com.imnaiyar.skytimes.reminders

/**
 * Mainly for web targets
 */
object NoOpReminderScheduler : ReminderScheduler {
    override suspend fun refresh() = Unit
    override suspend fun scheduleReminder(reminder: Reminder) = Unit
    override suspend fun cancelReminder(eventId: String) = Unit
    override suspend fun cancelAll() = Unit
    override suspend fun hasPermission(): Boolean = true
    override suspend fun requestPermission() = true
    override fun hasExactAlarm(): Boolean = true
    override fun requestExactAlarm() = Unit
}
