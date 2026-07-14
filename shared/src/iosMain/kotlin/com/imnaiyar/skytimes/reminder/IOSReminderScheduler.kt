package com.imnaiyar.skytimes.reminder

import com.imnaiyar.skytimes.constants.events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSinceNow
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS implementation of [ReminderScheduler] using
 * `UNUserNotificationCenter`.
 *
 * ## Rolling notification window
 *
 * iOS does **not** execute app code when a local notification is
 * delivered.  To work around this limitation the scheduler maintains
 * a *rolling window* of future notifications:
 *
 * 1. On [refresh] (called at launch / foreground), it calculates
 *    [WINDOW_SIZE] future occurrence times for every enabled reminder
 *    using the shared [reminderTimes] function.
 * 2. It cancels any existing pending notifications for that reminder
 *    and posts the new batch.
 * 3. If fewer than [WINDOW_SIZE] notifications are pending (e.g. some
 *    have already been delivered), the remaining slots are filled
 *    starting from the latest scheduled time.
 *
 * This guarantees the user always has upcoming notifications
 * pre-scheduled, subject to iOS's 64-pending-notification limit.
 *
 * ## BGAppRefreshTask supplement
 *
 * `BGAppRefreshTask` is requested on each refresh so that iOS may
 * wake the app in the background periodically to replenish the queue.
 * The actual task handler is registered in the iOS app delegate (see
 * [com.imnaiyar.skytimes.reminder.IOSBackgroundTaskManager]).
 */
class IOSReminderScheduler(
    private val repository: ReminderRepository,
    private val notificationManager: NotificationManager,
) : ReminderScheduler {

    private val center: UNUserNotificationCenter =
        UNUserNotificationCenter.currentNotificationCenter()

    // ── ReminderScheduler ──────────────────────────────────

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val reminders = repository.getAll()
        val now = Clock.System.now()

        for (reminder in reminders) {
            if (!reminder.enabled) {
                cancelReminderNotifications(reminder.id)
                continue
            }
            replenishReminder(reminder, now)
        }

        // Request a background refresh as a supplement.
        requestBackgroundRefresh()
    }

    override suspend fun scheduleReminder(reminder: Reminder) =
        withContext(Dispatchers.IO) {
            if (!reminder.enabled) {
                cancelReminderNotifications(reminder.id)
                return@withContext
            }
            val now = Clock.System.now()
            replenishReminder(reminder, now)
        }

    override suspend fun cancelReminder(eventId: String) =
        withContext(Dispatchers.IO) {
            cancelReminderNotifications(eventId)
        }

    override suspend fun cancelAll() = withContext(Dispatchers.IO) {
        center.removeAllPendingNotificationRequests()
    }

    // ── Rolling window logic ───────────────────────────────

    /**
     * Ensures [WINDOW_SIZE] future notifications are pending for the
     * given reminder.
     *
     * 1. Remove all existing pending notifications for the reminder.
     * 2. Compute [WINDOW_SIZE] future times via [reminderTimes].
     * 3. Schedule each as a `UNNotificationRequest`.
     */
    private suspend fun replenishReminder(reminder: Reminder, now: kotlinx.datetime.Instant) {
        val event = events.firstOrNull { it.key == reminder.eventKey } ?: return

        // Start clean for this reminder.
        cancelReminderNotifications(reminder.id)

        val fireTimes = reminderTimes(reminder, event, now, limit = WINDOW_SIZE)
        if (fireTimes.isEmpty()) return

        for ((index, fireTime) in fireTimes.withIndex()) {
            scheduleNotification(reminder, fireTime, index)
        }
    }

    /**
     * Posts a single future-dated notification for one occurrence.
     *
     * Each notification uses a unique identifier of the form
     * `"{reminderId}_{occurrenceIndex}"` so that individual
     * notifications can be updated or removed without affecting
     * others in the same reminder's batch.
     */
    private fun scheduleNotification(
        reminder: Reminder,
        fireTime: kotlinx.datetime.Instant,
        occurrenceIndex: Int,
    ) {
        val identifier = "${reminder.id}_${occurrenceIndex}"

        // Calculate the interval from now.
        val nowDate = NSDate()
        val fireDate = NSDate(
            timeIntervalSince1970 = fireTime.toEpochMilliseconds() / 1000.0
        )
        val interval = fireDate.timeIntervalSinceDate(nowDate)

        // UNTimeIntervalNotificationTrigger requires a positive interval.
        if (interval <= 0.0) return

        val content = UNMutableNotificationContent().apply {
            title = reminder.title.ifEmpty { "Event Reminder" }
            body = reminder.body.ifEmpty { "Your event is starting soon!" }
            sound = platform.UserNotifications.UNNotificationSound.defaultSound()
            badge = 1
            userInfo = mapOf<Any?, Any?>(
                "reminderId" to reminder.id,
                "eventKey" to reminder.eventKey.name,
                "occurrenceIndex" to occurrenceIndex,
            )
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            interval,
            repeats = false,
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier,
            content,
            trigger,
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                // Logging could be added here.
            }
        }
    }

    // ── Helpers ─────────────────────────────────────────────

    /**
     * Removes **all** pending notifications whose identifier starts
     * with `"${reminderId}_"` (the batch prefix used above).
     *
     * This is intentionally broader than cancelling a single
     * notification so that stale occurrences are always cleaned up.
     */
    private fun cancelReminderNotifications(reminderId: String) {
        center.getPendingNotificationRequests { requests ->
            val idsToRemove = requests
                ?.filter { (it as? UNNotificationRequest)?.identifier?.startsWith("${reminderId}_") == true }
                ?.map { (it as UNNotificationRequest).identifier }
                ?: emptyList()

            if (idsToRemove.isNotEmpty()) {
                center.removePendingNotificationRequestsWithIdentifiers(idsToRemove)
            }
        }
    }

    private fun requestBackgroundRefresh() {
        // BGAppRefreshTask request – handled by the app delegate.
        // This is a "hint" to iOS; delivery is not guaranteed.
        IOSBackgroundTaskManager.requestAppRefresh()
    }

    companion object {
        /**
         * How many future occurrences to pre-schedule for each
         * reminder.  Must not exceed iOS's 64-pending-notification
         * limit divided by the maximum number of enabled reminders.
         *
         * With ~14 event types and a 64-notification cap, a window of
         * 4 per reminder is safe.
         */
        const val WINDOW_SIZE = 4
    }
}
