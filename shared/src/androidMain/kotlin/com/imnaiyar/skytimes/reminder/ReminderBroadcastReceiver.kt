package com.imnaiyar.skytimes.reminder

import android.Manifest
import android.content.BroadcastReceiver
import android.content.BroadcastReceiver.PendingResult
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.constants.events
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receives alarm intents posted by [AndroidReminderScheduler].
 * This receiver is invoked by the system when an alarm fires.
 */
class ReminderBroadcastReceiver : BroadcastReceiver() {

    // Dedicated scope that survives for the duration of this broadcast.
    // goAsync() extends the broadcast timeout, so we have a small
    // window to perform I/O.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @RequiresPermission(allOf = [Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.SCHEDULE_EXACT_ALARM])
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AndroidReminderScheduler.ACTION_REMINDER_ALARM) return

        val reminderId = intent.getStringExtra(AndroidReminderScheduler.EXTRA_REMINDER_ID)
            ?: return
        val eventKeyName = intent.getStringExtra(AndroidReminderScheduler.EXTRA_EVENT_KEY)
            ?: return
        val eventKey = runCatching { EventKey.valueOf(eventKeyName) }.getOrNull()
            ?: return

        val pendingResult = goAsync()

        scope.launch {
            try {
                handleAlarm(context, reminderId, eventKey)
            } finally {
                pendingResult.finish()
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.SCHEDULE_EXACT_ALARM])
    private suspend fun handleAlarm(
        context: Context,
        reminderId: String,
        eventKey: EventKey,
    ) {
        val repository = ReminderRepository()
        repository.initialize()

        val reminder = repository.getById(reminderId) ?: return
        if (!reminder.enabled) return

        val event = events.firstOrNull { it.key == eventKey } ?: return
        val notificationManager = AndroidNotificationManager(context)
        val scheduler = AndroidReminderScheduler(context, repository)

        // 1. Show the notification
        notificationManager.showNotification(reminder)

        // 2. Schedule the *next* alarm if the reminder is still enabled.
        scheduler.scheduleReminder(reminder)
    }
}
