package com.imnaiyar.skytimes.widgets

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.imnaiyar.skytimes.widgets.skytimes.SkyTimesWidget
import com.imnaiyar.skytimes.widgets.skytimes.WidgetReceiver
import java.util.concurrent.TimeUnit


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

            val request =
                PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
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
                    SkyTimesWidget().updateAll(applicationContext)
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


class WidgetPreviewGenerator(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "preview_generator_work"
        private const val TAG = "WidgetPreviewGenerator"
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request =
                PeriodicWorkRequestBuilder<WidgetPreviewGenerator>(
                    1, TimeUnit.DAYS,
                )
                    .setConstraints(constraints)
                    .addTag(TAG)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }

    @SuppressLint("CheckResult")
    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(applicationContext)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                manager.setWidgetPreviews(WidgetReceiver::class)
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Preview Generation Failed", e)
            Result.retry()
        }
    }
}