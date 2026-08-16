package com.imnaiyar.skytimes.feature.reminders

import androidx.compose.runtime.staticCompositionLocalOf

/** The reminders repository, provided by the app root. */
val LocalReminderRepository = staticCompositionLocalOf<ReminderRepository> {
    error("No ReminderRepository provided")
}

/** The platform reminder scheduler, provided by the app root. */
val LocalReminderScheduler = staticCompositionLocalOf<ReminderScheduler> {
    error("No ReminderScheduler provided")
}

/** The global notifications toggle, provided by the app root. */
val LocalNotificationsToggle = staticCompositionLocalOf<NotificationsToggle> {
    error("No NotificationsToggle provided")
}
