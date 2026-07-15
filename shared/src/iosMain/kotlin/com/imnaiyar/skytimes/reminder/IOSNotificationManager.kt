package com.imnaiyar.skytimes.reminder

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

/**
 * iOS implementation of [NotificationManager]
 *
 * Because iOS does **not** wake the app when a local notification is
 * delivered, this manager only handles **immediate** notifications
 * (shown when the app is in the foreground as an in-app alert).
 */
internal class IOSNotificationManager : NotificationManager {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun showNotification(reminder: Reminder) {
        val content = UNMutableNotificationContent().apply {
            setTitle(reminder.title.ifEmpty { "Event Reminder" })
            setBody(reminder.body.ifEmpty { "Your event is starting soon!" })
            setSound(platform.UserNotifications.UNNotificationSound.defaultSound())
            setUserInfo(mapOf<Any?, Any?>(
                "reminderId" to reminder.id,
                "eventKey" to reminder.eventKey.name,
            ))
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

    // ── Permission ─────────────────────────────────────────

    override suspend fun isPermissionGranted(): Boolean {

        return suspendCancellableCoroutine { continuation ->
            center.getNotificationSettingsWithCompletionHandler { settings ->
                val granted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
                    || settings?.authorizationStatus == UNAuthorizationStatusProvisional
                    || settings?.authorizationStatus == UNAuthorizationStatusEphemeral
                continuation.resume(granted)
            }
        }
    }

    override suspend fun requestPermission(): Boolean {
        if (isPermissionGranted()) return true

        return suspendCancellableCoroutine { continuation ->
            val options = UNAuthorizationOptionAlert or
                UNAuthorizationOptionSound or
                UNAuthorizationOptionBadge
            center.requestAuthorizationWithOptions(options) { granted, _ ->
                continuation.resume(granted)
            }
        }
    }
}

// ────────────────────────────────────────────────────────────
// Platform `actual`
// ────────────────────────────────────────────────────────────

actual fun createPlatformNotificationManager(): NotificationManager =
    IOSNotificationManager()
