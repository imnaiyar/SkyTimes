package com.imnaiyar.skytimes.reminder

/**
 * Manager for notification
 */
interface NotificationManager {

    /**
     * Display a notification immediately (used when a scheduled alarm
     * fires, or as a fallback).
     */
    suspend fun showNotification(reminder: Reminder)

    /**
     * Remove previous notifs, if any
     */
    suspend fun cancelNotification(reminderId: String)

    /**
     * Remove all notifs
     */
    suspend fun cancelAllNotifications()

    // ── Permission ─────────────────────────────────────────

    /**
     * Whether app has notification permission
     */
    suspend fun isPermissionGranted(): Boolean

    /**
     * Requests the required notification permission(s) from the OS.
     *
     * @return `true` if permission was granted, `false` otherwise.
     */
    suspend fun requestPermission(): Boolean
}

/**
 * Platform `expect` declaration – each target provides the appropriate
 * [NotificationManager] implementation.
 */
expect fun createPlatformNotificationManager(): NotificationManager
