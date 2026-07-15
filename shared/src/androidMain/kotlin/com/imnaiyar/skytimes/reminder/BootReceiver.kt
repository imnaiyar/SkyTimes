package com.imnaiyar.skytimes.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Re-schedules all reminder alarms after the device boots.
 *
 * Android clears all alarms on reboot, so we must recreate them.
 * This receiver is registered in the manifest for
 * `BOOT_COMPLETED` (and `LOCKED_BOOT_COMPLETED` on API 24+ for
 * direct-boot awareness).
 */
class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != LOCKED_BOOT_COMPLETED
        ) return

        val pendingResult = goAsync()

        scope.launch {
            try {
                val repository = ReminderRepository()
                repository.initialize()

                val notificationManager = AndroidNotificationManager(context)
                val scheduler = AndroidReminderScheduler(
                    context, repository
                )
                scheduler.refresh()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val LOCKED_BOOT_COMPLETED =
            "android.intent.action.LOCKED_BOOT_COMPLETED"
    }
}
