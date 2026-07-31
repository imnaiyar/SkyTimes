package com.imnaiyar.skytimes.reminders

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.imnaiyar.skytimes.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Shows current notification for event and schedules next one
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    @RequiresPermission(allOf = [Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.SCHEDULE_EXACT_ALARM])
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settingsRepository = SettingsRepository()
                val reminderRepository = ReminderRepository()
                val scheduler = AndroidReminderScheduler(
                    appContext,
                    settingsRepository,
                    reminderRepository,
                    this
                )
                val reminder = scheduler.resolveReminderFromIntent(intent)
                    ?: return@launch

                scheduler.onAlarmTriggered(reminder)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

/**
 * Reschedule all notifications on restart
 */
class ReminderBootReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settingsRepository = SettingsRepository()
                val reminderRepository = ReminderRepository()
                val scheduler = AndroidReminderScheduler(
                    appContext,
                    settingsRepository,
                    reminderRepository,
                    this
                )
                scheduler.refresh()
            } finally {
                pendingResult.finish()
            }
        }
    }
}