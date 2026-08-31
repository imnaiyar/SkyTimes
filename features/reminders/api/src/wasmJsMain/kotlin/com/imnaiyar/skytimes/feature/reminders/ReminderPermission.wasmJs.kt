package com.imnaiyar.skytimes.feature.reminders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberReminderPermissionController(): ReminderPermissionController {
    return remember {
        object : ReminderPermissionController {
            override suspend fun notificationStatus(): ReminderPermissionStatus =
                ReminderPermissionStatus.Granted

            override suspend fun requestNotificationPermission(): ReminderPermissionStatus =
                ReminderPermissionStatus.Granted

            override fun openNotificationSettings() = Unit
        }
    }
}
