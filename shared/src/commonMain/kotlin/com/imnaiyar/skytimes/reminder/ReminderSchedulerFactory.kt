package com.imnaiyar.skytimes.reminder

/**
 * Platform `expect` declaration for creating a [ReminderScheduler]
 * appropriate for the current target.
 *
 * - **Android** → [AndroidReminderScheduler] (AlarmManager).
 * - **iOS**     → [IOSReminderScheduler] (UNUserNotificationCenter).
 * - **JS/Wasm** → no-op stub.
 */
expect fun createPlatformReminderScheduler(
    repository: ReminderRepository,
    notificationManager: NotificationManager,
): ReminderScheduler
