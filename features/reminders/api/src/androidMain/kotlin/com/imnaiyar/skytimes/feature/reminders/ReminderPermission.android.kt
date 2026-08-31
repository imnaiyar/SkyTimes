package com.imnaiyar.skytimes.feature.reminders

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
actual fun rememberReminderPermissionController(): ReminderPermissionController {
    var pendingCallback by remember { mutableStateOf<((ReminderPermissionStatus) -> Unit)?>(null) }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val appContext = context.applicationContext
    val permissionState = remember(appContext) { AndroidNotificationPermissionState(appContext) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val status = if (granted) {
            ReminderPermissionStatus.Granted
        } else {
            permissionState.notificationStatus(activity)
        }
        pendingCallback?.invoke(status)
        pendingCallback = null
    }

    return remember(appContext, activity, permissionState, launcher) {
        object : ReminderPermissionController {
            override suspend fun notificationStatus(): ReminderPermissionStatus {
                return permissionState.notificationStatus(activity)
            }

            override suspend fun requestNotificationPermission(): ReminderPermissionStatus {
                if (permissionState.notificationStatus(activity) == ReminderPermissionStatus.Granted) {
                    return ReminderPermissionStatus.Granted
                }

                if (permissionState.notificationStatus(activity) == ReminderPermissionStatus.SettingsRequired) {
                    return ReminderPermissionStatus.SettingsRequired
                }

                return suspendCancellableCoroutine { continuation ->
                    permissionState.markRequested()
                    pendingCallback = { status ->
                        if (continuation.isActive) continuation.resume(status)
                    }
                    continuation.invokeOnCancellation { pendingCallback = null }
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            override fun openNotificationSettings() {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, appContext.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                runCatching { appContext.startActivity(intent) }
                    .recoverCatching {
                        appContext.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData("package:${appContext.packageName}".toUri())
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
            }
        }
    }
}

private class AndroidNotificationPermissionState(private val context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun notificationStatus(activity: Activity?): ReminderPermissionStatus {
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return ReminderPermissionStatus.Granted
        }

        if (activity == null) return ReminderPermissionStatus.Unavailable

        val requestCount = preferences.getInt(KEY_NOTIFICATION_REQUEST_COUNT, 0)
        val canShowPromptAgain = ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        )

        return if (requestCount >= REQUIRED_DENIALS_BEFORE_SETTINGS && !canShowPromptAgain) {
            ReminderPermissionStatus.SettingsRequired
        } else {
            ReminderPermissionStatus.Requestable
        }
    }

    fun markRequested() {
        val requestCount = preferences.getInt(KEY_NOTIFICATION_REQUEST_COUNT, 0)
        preferences.edit {
            putInt(KEY_NOTIFICATION_REQUEST_COUNT, requestCount + 1)
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "reminder_permissions"
        const val KEY_NOTIFICATION_REQUEST_COUNT = "notification_request_count"
        const val REQUIRED_DENIALS_BEFORE_SETTINGS = 2
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
