package com.imnaiyar.skytimes.reminder

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler

/**
 * Manages `BGAppRefreshTask` registration and request submission for
 * iOS background notification replenishment.
 */
object IOSBackgroundTaskManager {

    private const val TASK_ID = "com.imnaiyar.skytimes.refresh"

    /**
     * Requests a background app refresh from the system.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun requestAppRefresh() {
        val request = BGAppRefreshTaskRequest(TASK_ID).apply {
            earliestBeginDate = null // as soon as possible
        }
        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
        } catch (_: Exception) {
            // Request may fail if too many are already pending; safe to ignore.
        }
    }

    /**
     * Called by the native app delegate when the system delivers
     * a background refresh task.
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
                )
                scheduler.refresh()
            } finally {
                task.setTaskCompletedWithSuccess(success = true)
            }
        }
    }
}
