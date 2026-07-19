package com.imnaiyar.skytimes.reminders

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.imnaiyar.skytimes.repositories.SettingsRepository
import com.imnaiyar.skytimes.shared.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class AndroidReminderScheduler(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val reminderRepository: ReminderRepository,
    private val scope: CoroutineScope,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
) : ReminderScheduler {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)


    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override suspend fun refresh() {
        ensureStateLoaded()
        cancelAll()
        reminderRepository.reminders.value
            .filter(Reminder::enabled)
            .forEach { scheduleReminder(it) }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override suspend fun scheduleReminder(reminder: Reminder) {
        ensureStateLoaded()

        if (!reminder.enabled) {
            cancelReminder(reminder.eventId.name)
            return
        }

        val triggerAt = reminderTimes(reminder, Clock.System.now(), 1).firstOrNull()
            ?: return

        val pendingIntent = buildPendingIntent(reminder)
        scheduleExact(triggerAt.toEpochMilliseconds(), pendingIntent)
    }

    override suspend fun cancelReminder(eventId: String) {
        ensureStateLoaded()

        reminderRepository.reminders.value
            .filter { it.eventId.name == eventId }
            .forEach { reminder ->
                alarmManager.cancel(buildPendingIntent(reminder))
            }
    }

    override suspend fun cancelAll() {
        ensureStateLoaded()

        reminderRepository.reminders.value.forEach { reminder ->
            alarmManager.cancel(buildPendingIntent(reminder))
        }
    }

    override suspend fun hasPermission(): Boolean {
        return NotificationManagerCompat.from(appContext).areNotificationsEnabled()
    }

    /**
     * Since requesting permission on android is little complex, and we also need to redirect to
     * exact alarm setting, this is handled from ui side of things
     */
    override suspend fun requestPermission() = true

    override fun hasExactAlarm(): Boolean = alarmManager.canScheduleExactAlarms()

    override fun requestExactAlarm() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            .apply {
                data = "package:${context.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        runCatching { context.startActivity(intent) }
            .onFailure {
                Log.e("Alarm", "Failed to launch settings", it)
            }
    }

    private suspend fun ensureStateLoaded() {
        settingsRepository.initialize()
        reminderRepository.initialize()
    }

    private fun buildPendingIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(appContext, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_REMINDER_ALARM
            putExtra(EXTRA_REMINDER_JSON, json.encodeToString(reminder))
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            appContext,
            reminder.id.hashCode(),
            intent,
            flags
        )
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleExact(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        ensureChannel()


        try {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
        }

        val notificationManager = appContext.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(reminder: Reminder) {

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_monochrome)
            .setContentTitle(Reminder.title(reminder.eventId))
            .setContentText(
                Reminder.body(
                    reminder.eventId,
                    reminder.offsetMinutes
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getLaunchIntent(reminder))
            .build()

        NotificationManagerCompat.from(appContext)
            .notify(reminder.id.hashCode(), notification)
    }

    private fun getLaunchIntent(reminder: Reminder): PendingIntent {
        val openIntent = appContext.packageManager
            .getLaunchIntentForPackage(appContext.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                // TODO: in case I need to use this in future
                putExtra(EXTRA_REMINDER_ID, reminder.id)
                putExtra(EXTRA_EVENT_KEY, reminder.eventId.name)
            }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getActivity(context, reminder.id.hashCode(), openIntent, flags)
    }

    private fun loadReminder(reminderId: String): Reminder? {
        return reminderRepository.reminders.value.firstOrNull { it.id == reminderId }
    }

    companion object {
        const val ACTION_REMINDER_ALARM = "com.imnaiyar.skytimes.reminders.ACTION_REMINDER_ALARM"
        const val EXTRA_REMINDER_JSON = "extra_reminder_json"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_EVENT_KEY = "extra_event_key"
        private const val CHANNEL_ID = "event_reminders"
        private const val CHANNEL_NAME = "Event Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for upcoming event reminders"
    }

    @RequiresPermission(allOf = [Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.SCHEDULE_EXACT_ALARM])
    internal fun onAlarmTriggered(reminder: Reminder) {

        scope.launch {
            if (hasPermission()) {
                showNotification(reminder)
            }
            scheduleReminder(reminder)
        }
    }

    internal fun resolveReminderFromIntent(intent: Intent): Reminder? {
        runBlocking {
            ensureStateLoaded()
        }

        val reminderJson = intent.getStringExtra(EXTRA_REMINDER_JSON)
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID)

        val loaded = reminderId?.let(::loadReminder)
        if (loaded != null) return loaded

        return reminderJson?.let { payload ->
            runCatching { json.decodeFromString(Reminder.serializer(), payload) }
                .getOrNull()
        }
    }
}

actual fun getReminderSchedular(
    settingsRepository: SettingsRepository,
    reminderRepository: ReminderRepository,
    scope: CoroutineScope
): ReminderScheduler {
    return AndroidReminderScheduler(
        ContextHolder.context,
        settingsRepository,
        reminderRepository,
        scope
    )
}

@Composable
actual fun rememberNotificationPermissionRequester(): ((Boolean) -> Unit) -> Unit {
    var pendingCallback by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        pendingCallback?.invoke(granted)
        pendingCallback = null
        if (!granted && activity != null) {
            // TODO: handle showing dialogue to redirect to app settings
            println(
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }

    return remember {
        { callback: (Boolean) -> Unit ->
            pendingCallback = callback
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}


private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}