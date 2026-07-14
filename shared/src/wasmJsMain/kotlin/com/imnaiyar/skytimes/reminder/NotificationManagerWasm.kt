package com.imnaiyar.skytimes.reminder

/**
 * No-op [NotificationManager] for Wasm-JS target.
 */
internal class NoOpNotificationManagerWasm : NotificationManager {

    override suspend fun showNotification(reminder: Reminder) {
        // No-op
    }

    override suspend fun cancelNotification(reminderId: String) {
        // No-op
    }

    override suspend fun cancelAllNotifications() {
        // No-op
    }
}

actual fun createPlatformNotificationManager(): NotificationManager =
    NoOpNotificationManagerWasm()
