package com.imnaiyar.skytimes.reminder

import com.imnaiyar.skytimes.constants.EventKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Shared orchestration layer for reminders.
 */
class ReminderManager(
    val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val notificationManager: NotificationManager,
) {

    /**
     * Master override controlled by the Settings "Notifications" toggle.
     */
    var masterEnabled: Boolean = true

    // ── Lifecycle

    suspend fun initialize() {
        repository.initialize()
        scheduler.refresh()
    }

    // ── Reminder CRUD

    /**
     * saves the reminder and re-schedules its platform alarm.
     */
    suspend fun updateReminder(reminder: Reminder) {
        repository.upsert(reminder)
        if (masterEnabled) {
            scheduler.scheduleReminder(reminder)
        } else {
            scheduler.cancelReminder(reminder.id)
        }
    }

    /**
     * Removes the reminder from persistence and cancels any pending
     * alarm/notification for it.
     */
    suspend fun removeReminder(reminderId: String) {
        repository.delete(reminderId)
        scheduler.cancelReminder(reminderId)
        notificationManager.cancelNotification(reminderId)
    }

    /**
     * Removes all reminders for the given event and cancels alarms.
     */
    suspend fun removeRemindersForEvent(eventKey: EventKey) {
        val reminders = repository.getAll()
        val matching = reminders.filter { it.eventKey == eventKey }
        for (r in matching) removeReminder(r.id)
    }

    // ── Scheduling ─────────────────────────────────────────

    /**
     * Re-evaluates all reminders and ensures platform alarms are
     * current.  Safe to call frequently – it is idempotent.
     */
    suspend fun refresh() {
        if (!masterEnabled) {
            scheduler.cancelAll()
            return
        }
        scheduler.refresh()
    }

    /**
     * Convenience: refresh in the given [scope] without blocking
     * the caller.
     */
    fun refreshIn(scope: CoroutineScope) {
        scope.launch {
            scheduler.refresh()
        }
    }

    /**
     * Cancels every pending alarm and removes all persisted reminders.
     */
    suspend fun cancelAll() {
        scheduler.cancelAll()
        notificationManager.cancelAllNotifications()
        repository.deleteAll()
    }

    // ── Permission helpers ──────────────────────────────────

    /**
     * Delegates to [NotificationManager.isPermissionGranted].
     */
    suspend fun isNotificationPermissionGranted(): Boolean =
        notificationManager.isPermissionGranted()

    /**
     * Delegates to [NotificationManager.requestPermission].
     */
    suspend fun requestNotificationPermission(): Boolean =
        notificationManager.requestPermission()
}
