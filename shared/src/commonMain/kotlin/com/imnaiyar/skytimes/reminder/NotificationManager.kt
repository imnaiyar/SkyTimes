package com.imnaiyar.skytimes.reminder

/**
 * Platform-agnostic abstraction for displaying a local notification to
 * the user.  Each platform provides an `actual` implementation that
 * delegates to the OS notification subsystem.
 *
 * The reminder subsystem uses this interface so that shared code never
 * imports platform-specific notification APIs directly.
 */
interface NotificationManager {

    /**
     * Display a notification immediately (used when a scheduled alarm
     * fires, or as a fallback).
     *
     * @param reminder  The reminder whose notification should be shown.
     */
    suspend fun showNotification(reminder: Reminder)

    /**
     * Remove any previously-displayed persistent notification for the
     * given reminder (if the platform supports it).
     */
    suspend fun cancelNotification(reminderId: String)

    /**
     * Remove all notifications created by this application.
     */
    suspend fun cancelAllNotifications()
}

/**
 * Platform `expect` declaration – each target provides the appropriate
 * [NotificationManager] implementation.
 *
 * - **Android** → uses `NotificationManagerCompat` + `NotificationChannel`.
 * - **iOS**     → uses `UNUserNotificationCenter`.
 * - **JS/Wasm** → a no-op stub (alerts are not supported in browsers
 *   without a Service Worker, which is out of scope).
 */
expect fun createPlatformNotificationManager(): NotificationManager
