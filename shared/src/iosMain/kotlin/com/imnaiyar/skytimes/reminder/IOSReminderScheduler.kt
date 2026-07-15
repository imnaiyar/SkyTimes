package com.imnaiyar.skytimes.reminder

import com.imnaiyar.skytimes.constants.events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSDate
import platform.Foundation.NSNumber
import platform.Foundation.timeIntervalSinceDate
import platform.Foundation.timeIntervalSinceNow
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.Clock

/**
 * iOS implementation of [ReminderScheduler]
 *
 * `BGAppRefreshTask` is requested on each refresh so that iOS may
 * wake the app in the background periodically to replenish the queue.
 * The actual task handler is registered in the iOS app delegate (see
 * [com.imnaiyar.skytimes.reminder.IOSBackgroundTaskManager]).
 */
class IOSReminderScheduler(
    private val repository: ReminderRepository,
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
            timeIntervalSinceReferenceDate = fireTime.toEpochMilliseconds() / 1000.0
        )
        val interval = fireDate.timeIntervalSinceDate(nowDate)

        // UNTimeIntervalNotificationTrigger requires a positive interval.
        if (interval <= 0.0) return

        val content = UNMutableNotificationContent().apply {
            setTitle(reminder.title.ifEmpty { "Event Reminder" })
            setBody(reminder.body.ifEmpty { "Your event is starting soon!" })
            setSound(platform.UserNotifications.UNNotificationSound.defaultSound())
            setBadge(NSNumber(1))
            setUserInfo(mapOf<Any?, Any?>(
                "reminderId" to reminder.id,
                "eventKey" to reminder.eventKey.name,
                "occurrenceIndex" to occurrenceIndex,
            ))
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
                println("Error scheduling notification: $error")
            }
        }
    }

    // ── Helpers ─────────────────────────────────────────────

    /**
     * Removes **all** pending notifications whose identifier starts
     * with `"${reminderId}_"` (the batch prefix used above).
     */
    private fun cancelReminderNotifications(reminderId: String) {
        center.getPendingNotificationRequestsWithCompletionHandler { requests ->
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
        IOSBackgroundTaskManager.requestAppRefresh()
    }

    companion object {
        /**
         * With ~14 event types and a 64-notification cap, a window of
         * 4 per reminder is safe.
         */
        const val WINDOW_SIZE = 4
    }
}
