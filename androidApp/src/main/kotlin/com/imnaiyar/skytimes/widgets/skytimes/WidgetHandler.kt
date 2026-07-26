package com.imnaiyar.skytimes.widgets.skytimes

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.imnaiyar.skytimes.widgets.WidgetPreferences
import com.imnaiyar.skytimes.widgets.WidgetUpdateWorker

/**
 *  Widget receiver for SkyTimes.
 */
class SkytimesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = SkyTimesWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        // enqueues periodic updates at 15 min interval if it hasn't already been enqueued
        WidgetUpdateWorker.enqueuePeriodicUpdate(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { id ->
            WidgetPreferences.removeWidget(context, id)
        }

        // If no widgets remain, cancel periodic updates
        val remaining = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, SkytimesWidgetReceiver::class.java))
        if (remaining.isEmpty()) {
            WidgetUpdateWorker.cancelPeriodicUpdate(context)
        }

    }


    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}

