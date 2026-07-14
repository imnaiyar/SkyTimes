package com.imnaiyar.skytimes.reminder

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS implementation of [NotificationManager] using
 * `UNUserNotificationCenter`.
 *
 * Because iOS does **not** wake the app when a local notification is
 * delivered, this manager only handles **immediate** notifications
 * (shown when the app is in the foreground as an in-app alert).
 *
 * The bulk of the scheduling is handled by [IOSReminderScheduler],
 * which posts future-dated notifications directly to
 * `UNUserNotificationCenter`.
 */
internal class IOSNotificationManager : NotificationManager {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun showNotification(reminder: Reminder) {
        val content = UNMutableNotificationContent().apply {
            title = reminder.title.ifEmpty { "Event Reminder" }
            body = reminder.body.ifEmpty { "Your event is starting soon!" }
            sound = platform.UserNotifications.UNNotificationSound.defaultSound()
            userInfo = mapOf<Any?, Any?>(
                "reminderId" to reminder.id,
                "eventKey" to reminder.eventKey.name,
            )
        }

        // Fire immediately – useful when the app is foregrounded.
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            1.0, // 1 second from now
            repeats = false,
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            reminder.id,
            content,
            trigger,
        )

        center.addNotificationRequest(request) { _ -> /* fire-and-forget */ }
    }

    override suspend fun cancelNotification(reminderId: String) {
        center.removePendingNotificationRequestsWithIdentifiers(
            listOf(reminderId)
        )
    }

    override suspend fun cancelAllNotifications() {
        center.removeAllPendingNotificationRequests()
    }
}

// ────────────────────────────────────────────────────────────
// Platform `actual`
// ────────────────────────────────────────────────────────────

actual fun createPlatformNotificationManager(): NotificationManager =
    IOSNotificationManager()
