package com.imnaiyar.skytimes.reminder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler

/**
 * Manages `BGAppRefreshTask` registration and request submission for
 * iOS background notification replenishment.
 *
 * ## Usage
 *
 * ### In the Swift / Obj-C app delegate:
 *
 * ```swift
 * BGTaskScheduler.shared.register(
 *     forTaskWithIdentifier: "com.imnaiyar.skytimes.refresh",
 *     using: nil
 * ) { task in
 *     IOSBackgroundTaskManagerKt.handleAppRefresh(task)
 * }
 * ```
 *
 * ### Requesting the task:
 *
 * Called automatically by [IOSReminderScheduler.refresh].
 *
 * ### Task handler:
 *
 * [handleAppRefresh] is called by the app delegate when iOS grants
 * background execution time.  It re-runs [IOSReminderScheduler.refresh]
 * to replenish the notification queue.
 */
object IOSBackgroundTaskManager {

    private const val TASK_ID = "com.imnaiyar.skytimes.refresh"

    /**
     * Requests a background app refresh from the system.
     *
     * This is a best-effort request; iOS may defer or deny it.
     */
    fun requestAppRefresh() {
        val request = BGAppRefreshTaskRequest(TASK_ID).apply {
            earliestBeginDate = null // as soon as possible
        }
        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request)
        } catch (_: Exception) {
            // Request may fail if too many are already pending – safe to ignore.
        }
    }

    /**
     * Called by the native app delegate when the system delivers
     * a background refresh task.
     *
     * @param task The [BGAppRefreshTask] provided by iOS.
     * @param repository The [ReminderRepository] to read current state from.
     */
    fun handleAppRefresh(task: BGAppRefreshTask, repository: ReminderRepository) {
        task.expirationHandler = {
            // Mark the task as incomplete if we run out of time.
        }

        // Launch the replenishment on a background queue.
        // The task's setTaskCompleted must be called when done.
        GlobalScope.launch(Dispatchers.Main) {
            try {
                repository.initialize()
                val scheduler = IOSReminderScheduler(
                    repository,
                    IOSNotificationManager(),
                )
                scheduler.refresh()
            } finally {
                task.setTaskCompleted(success = true)
            }
        }
    }
}
