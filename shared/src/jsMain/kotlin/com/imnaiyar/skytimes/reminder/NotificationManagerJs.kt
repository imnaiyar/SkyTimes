package com.imnaiyar.skytimes.reminder

/**
 * No-op [NotificationManager] for JS / Wasm targets.
 *
 * Browser notifications require a Service Worker, which is out of
 * scope for this cross-platform shared module.
 */
internal class NoOpNotificationManager : NotificationManager {

    override suspend fun showNotification(reminder: Reminder) {
        // No-op: browser notifications not supported in this module.
    }

    override suspend fun cancelNotification(reminderId: String) {
        // No-op
    }

    override suspend fun cancelAllNotifications() {
        // No-op
    }
}

actual fun createPlatformNotificationManager(): NotificationManager =
    NoOpNotificationManager()
