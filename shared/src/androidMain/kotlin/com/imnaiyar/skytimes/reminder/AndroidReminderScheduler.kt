package com.imnaiyar.skytimes.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.constants.events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Android implementation of [ReminderScheduler] backed by [AlarmManager].
 *
 * ## Design decisions
 *
 * - **Exact alarms** are used on API 31+ when the reminder offset is
 *   short (≤ 15 min), which is always the case for this app.  The
 *   `SCHEDULE_EXACT_ALARM` permission is requested in the manifest.
 * - Each alarm carries the [Reminder.id] and [EventKey] as extras so
 *   the [ReminderBroadcastReceiver] can reconstruct state without
 *   keeping any long-lived process alive.
 * - The scheduler itself is stateless – all reminder configuration is
 *   persisted in [ReminderRepository], and alarms are recreated from
 *   scratch on every [refresh] call.
 * - [refresh] uses a batch approach: cancel all existing alarms, then
 *   schedule one alarm per reminder using [reminderTimes].
 */
class AndroidReminderScheduler(
    private val context: Context,
    private val repository: ReminderRepository,
    private val notificationManager: NotificationManager,
) : ReminderScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // ── ReminderScheduler ──────────────────────────────────

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val reminders = repository.getAll()

        // Cancel every alarm we know about before re-scheduling.
        for (r in reminders) {
            cancelAlarmForReminder(r.id)
        }

        reminders
            .filter { it.enabled }
            .forEach { scheduleSingleReminder(it) }
    }

    override suspend fun scheduleReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        if (!reminder.enabled) {
            cancelReminder(reminder.id)
            return@withContext
        }
        scheduleSingleReminder(reminder)
    }

    override suspend fun cancelReminder(eventId: String) = withContext(Dispatchers.IO) {
        cancelAlarmForReminder(eventId)
    }

    override suspend fun cancelAll() = withContext(Dispatchers.IO) {
        val reminders = repository.getAll()
        for (r in reminders) {
            cancelAlarmForReminder(r.id)
        }
    }

    // ── Internal helpers ───────────────────────────────────

    private fun scheduleSingleReminder(reminder: Reminder) {
        val event = events.firstOrNull { it.key == reminder.eventKey } ?: return
        val now = Clock.System.now()

        // Calculate the single *next* fire time.  We only schedule
        // one alarm; when it fires the BroadcastReceiver will schedule
        // the subsequent one.
        val times = reminderTimes(reminder, event, now, limit = 1)
        val nextFire = times.firstOrNull() ?: return

        val intent = buildAlarmIntent(reminder)
        val pendingIntent = buildPendingIntent(reminder, intent)

        scheduleExactAlarm(nextFire, pendingIntent)
    }

    /**
     * Cancels an alarm identified by [reminderId].
     *
     * We use [Reminder.id.hashCode] as the [PendingIntent] request code,
     * so we reconstruct the same PI to cancel it.
     */
    private fun cancelAlarmForReminder(reminderId: String) {
        val placeholder = Reminder(
            id = reminderId,
            eventKey = EventKey.GEYSER, // arbitrary – only used for hash
        )
        val intent = buildAlarmIntent(placeholder)
        val pendingIntent = buildPendingIntent(placeholder, intent)
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleExactAlarm(triggerAt: Instant, pendingIntent: PendingIntent) {
        val triggerMillis = triggerAt.toEpochMilliseconds()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent,
                )
            }
            // Fallback: still set the alarm even without the permission;
            // Android may defer it but it will eventually fire.
            else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent,
                )
            }
        } else {
            @Suppress("DEPRECATION")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent,
            )
        }
    }

    // ── Intent / PendingIntent factories ───────────────────

    private fun buildAlarmIntent(reminder: Reminder): Intent {
        return Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_REMINDER_ALARM
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_EVENT_KEY, reminder.eventKey.name)
        }
    }

    private fun buildPendingIntent(
        reminder: Reminder,
        intent: Intent,
    ): PendingIntent {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            flags,
        )
    }

    companion object {
        const val ACTION_REMINDER_ALARM = "com.imnaiyar.skytimes.reminder.ALARM"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_EVENT_KEY = "event_key"
    }
}
