package com.imnaiyar.skytimes.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Android implementation of [NotificationManager].
 *
 * Responsibilities:
 * - Creates the mandatory notification channel on Android 8+.
 * - Posts a heads-up notification with the reminder's title and body.
 * - Cancels individual or all notifications.
 */
internal class AndroidNotificationManager(
    private val context: Context,
) : NotificationManager {

    private val systemManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    // ── NotificationManager ────────────────────────────────

    override suspend fun showNotification(reminder: Reminder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted – silently skip.
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(reminder.title.ifEmpty { "Event Reminder" })
            .setContentText(reminder.body.ifEmpty { "Your event is starting soon!" })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(createAppLaunchIntent(reminder))
            .build()

        systemManager.notify(reminder.id.hashCode(), notification)
    }

    override suspend fun cancelNotification(reminderId: String) {
        systemManager.cancel(reminderId.hashCode())
    }

    override suspend fun cancelAllNotifications() {
        systemManager.cancelAll()
    }

    // ── Helpers ─────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as AndroidNotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Creates a [PendingIntent] that launches the app's main activity.
     * The reminder id is passed as an extra so the app can navigate
     * directly to the relevant event if desired.
     */
    private fun createAppLaunchIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(context, Class.forName(LAUNCHER_ACTIVITY)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_EVENT_KEY, reminder.eventKey.name)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, reminder.id.hashCode(), intent, flags)
    }

    companion object {
        const val CHANNEL_ID = "event_reminders"
        const val CHANNEL_NAME = "Event Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for upcoming game events"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_EVENT_KEY = "extra_event_key"

        /** Fully-qualified name of the launcher Activity. */
        const val LAUNCHER_ACTIVITY = "com.imnaiyar.skytimes.MainActivity"
    }
}

// ────────────────────────────────────────────────────────────
// Platform `actual` for NotificationManager
// ────────────────────────────────────────────────────────────

/**
 * Returns an [AndroidNotificationManager] bound to the application context.
 *
 * The context is obtained via a lightweight holder that is initialised
 * in [com.imnaiyar.skytimes.GameTimeApplication].
 */
actual fun createPlatformNotificationManager(): NotificationManager {
    return AndroidNotificationManager(AndroidContextHolder.context)
}
