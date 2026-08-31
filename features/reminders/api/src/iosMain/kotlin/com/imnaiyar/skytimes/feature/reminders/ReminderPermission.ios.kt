package com.imnaiyar.skytimes.feature.reminders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

@Composable
actual fun rememberReminderPermissionController(): ReminderPermissionController {
    return remember {
        IosReminderPermissionController()
    }
}

private class IosReminderPermissionController(
    private val notificationCenter: UNUserNotificationCenter =
        UNUserNotificationCenter.currentNotificationCenter(),
) : ReminderPermissionController {
    override suspend fun notificationStatus(): ReminderPermissionStatus {
        return suspendCancellableCoroutine { continuation ->
            notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
                continuation.resume(settings?.authorizationStatus.toReminderPermissionStatus())
            }
        }
    }

    override suspend fun requestNotificationPermission(): ReminderPermissionStatus {
        val status = notificationStatus()
        if (status != ReminderPermissionStatus.Requestable) return status

        return suspendCancellableCoroutine { continuation ->
            val options = UNAuthorizationOptionAlert or
                    UNAuthorizationOptionSound or
                    UNAuthorizationOptionBadge
            notificationCenter.requestAuthorizationWithOptions(options) { granted, _ ->
                continuation.resume(
                    if (granted) {
                        ReminderPermissionStatus.Granted
                    } else {
                        ReminderPermissionStatus.SettingsRequired
                    }
                )
            }
        }
    }

    override fun openNotificationSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        
        UIApplication.sharedApplication.openURL(
            settingsUrl,
            options = emptyMap<Any?, Any?>(),
            completionHandler = { success ->
                if (!success) println("failed to open settings: $settingsUrl")
            },
        )
    }
}

private fun Long?.toReminderPermissionStatus(): ReminderPermissionStatus {
    return when (this) {
        UNAuthorizationStatusAuthorized,
        UNAuthorizationStatusProvisional,
        UNAuthorizationStatusEphemeral -> ReminderPermissionStatus.Granted

        UNAuthorizationStatusNotDetermined -> ReminderPermissionStatus.Requestable
        UNAuthorizationStatusDenied -> ReminderPermissionStatus.SettingsRequired
        else -> ReminderPermissionStatus.Unavailable
    }
}
