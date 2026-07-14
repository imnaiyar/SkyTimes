package com.imnaiyar.skytimes.reminder

actual fun createPlatformReminderScheduler(
    repository: ReminderRepository,
    notificationManager: NotificationManager,
): ReminderScheduler {
    return IOSReminderScheduler(
        repository = repository,
        notificationManager = notificationManager,
    )
}
