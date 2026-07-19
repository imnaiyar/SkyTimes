package com.imnaiyar.skytimes.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 *  Widget receiver for SkyTimes.
 */
class WidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = SkyTimesWidget()

    /**
     * Clean preferences when instance is deleted
     */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { id ->
            WidgetPreferences.removeWidget(context, id)
        }

        // If no widgets remain, cancel periodic updates
        val remaining = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, WidgetReceiver::class.java))
        if (remaining.isEmpty()) {
            WidgetUpdateWorker.cancelPeriodicUpdate(context)
        }
    }


    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Ensure periodic background updates are scheduled
        WidgetUpdateWorker.enqueuePeriodicUpdate(context)
    }
}


/**
 * WorkManager-based periodic widget updater.
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "skytimes_widget_periodic_update"
        private const val TAG = "WidgetUpdateWorker"

        /**
         * Schedules a recurring 15-minute update for all SkyTimes widgets.
         */
        fun enqueuePeriodicUpdate(context: Context) {
            val constraints = Constraints.Builder()
                .build()

            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES,
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                // prevents duplicates
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        /**
         * Cancels the periodic update schedule. Called when all widgets are removed.
         */
        fun cancelPeriodicUpdate(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val componentName = ComponentName(applicationContext, WidgetReceiver::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (widgetIds.isEmpty()) {
                Log.d(TAG, "No widgets found — cancelling periodic work")
                return Result.success()
            }

            widgetIds.forEach { id ->
                try {
                    SkyTimesWidget.updateWidget(applicationContext, id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update widget $id", e)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "WorkManager update failed", e)
            Result.retry()
        }
    }
}
