package com.imnaiyar.skytimes.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.constants.events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Android implementation of [ReminderScheduler] backed by [AlarmManager].
 */
class AndroidReminderScheduler(
    private val context: Context,
    private val repository: ReminderRepository,
) : ReminderScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // ── ReminderScheduler
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
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

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
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

    // ── Internal helpers

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
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

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleExactAlarm(triggerAt: Instant, pendingIntent: PendingIntent) {
        val triggerMillis = triggerAt.toEpochMilliseconds()

        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent,
            )
        }
        // Fallback: still set the alarm even without the permission;
        // Android may defer it, but it will eventually fire.
        else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent,
            )
        }
    }

    // ── Intent / PendingIntent factories

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
