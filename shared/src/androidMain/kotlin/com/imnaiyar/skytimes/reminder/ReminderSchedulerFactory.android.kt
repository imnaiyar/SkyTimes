package com.imnaiyar.skytimes.reminder

actual fun createPlatformReminderScheduler(
    repository: ReminderRepository,
    notificationManager: NotificationManager,
): ReminderScheduler {
    return AndroidReminderScheduler(
        context = AndroidContextHolder.context,
        repository = repository,
        notificationManager = notificationManager,
    )
}
