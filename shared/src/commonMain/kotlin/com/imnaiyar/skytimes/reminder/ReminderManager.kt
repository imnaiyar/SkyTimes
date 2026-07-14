package com.imnaiyar.skytimes.reminder

import com.imnaiyar.skytimes.constants.EventKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Shared orchestration layer for reminders.
 *
 * This class is the single entry-point that ViewModels (or the
 * Application / AppDelegate) interact with.  It coordinates:
 *
 * - [ReminderRepository]   → persistence
 * - [ReminderScheduler]    → platform alarm/notification scheduling
 * - [NotificationManager]  → immediate notification display
 *
 * ## Typical usage
 *
 * ```kotlin
 * // At app startup / after settings change:
 * reminderManager.refresh()
 *
 * // When the user toggles a reminder:
 * reminderManager.updateReminder(reminder)
 *
 * // When the user dismisses an event:
 * reminderManager.removeReminder(eventId)
 * ```
 */
class ReminderManager(
    val repository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val notificationManager: NotificationManager,
) {

    /**
     * Master override controlled by the Settings "Notifications" toggle.
     *
     * When `false`, [refresh] and [scheduleReminder] cancel alarms without
     * deleting persisted [Reminder] data — toggling back to `true` restores
     * scheduling from persistence.
     */
    var masterEnabled: Boolean = true

    // ── Lifecycle ──────────────────────────────────────────

    /**
     * Must be called once during app startup (or after DI
     * initialisation) to load persisted reminders and schedule
     * platform alarms.
     */
    suspend fun initialize() {
        repository.initialize()
        scheduler.refresh()
    }

    // ── Reminder CRUD ──────────────────────────────────────

    /**
     * Persists the reminder and re-schedules its platform alarm.
     *
     * If [reminder.enabled] is `false` the alarm is cancelled.
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
        for (r in matching) {
            scheduler.cancelReminder(r.id)
            notificationManager.cancelNotification(r.id)
        }
        repository.deleteByEventKey(eventKey.name)
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
}
