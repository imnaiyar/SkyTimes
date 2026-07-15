package com.imnaiyar.skytimes.reminders

interface ReminderScheduler {
    suspend fun refresh()
    suspend fun scheduleReminder(reminder: Reminder)
    suspend fun cancelReminder(eventId: String)
    suspend fun cancelAll()
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
}
