package com.imnaiyar.skytimes.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import com.imnaiyar.skytimes.widget.ui.WidgetContent

/**
 * SkyTimes home screen widget
 */
class SkyTimesWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            SMALL_BOX,
            BIG_BOX,
            ROW,
            LARGE_ROW,
            COLUMN,
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        provideContent {
            GlanceTheme {
                WidgetContent(
                    context = context,
                    appWidgetId = appWidgetId,
                )
            }
        }
    }


    companion object {

        // sizes for the widget
        private val SMALL_BOX = DpSize(90.dp, 90.dp)
        private val BIG_BOX = DpSize(180.dp, 180.dp)
        private val ROW = DpSize(180.dp, 48.dp)
        private val LARGE_ROW = DpSize(300.dp, 48.dp)
        private val COLUMN = DpSize(48.dp, 180.dp)

        /**
         * Triggers an immediate update for a specific widget instance.
         */
        fun updateWidget(context: Context, appWidgetId: Int) {
            val intent = Intent(context, WidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            context.sendBroadcast(intent)
        }

        /**
         * Triggers an update for ALL widget instances currently placed
         * on the home screen. Used when app-side data changes (e.g., new
         * event version, settings change) need to refresh all widgets.
         */
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, WidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(
                        android.content.ComponentName(
                            context,
                            WidgetReceiver::class.java
                        )
                    )
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}

/**
 * Glance [ActionCallback] triggered by the refresh button on the widget.
 */
class WidgetRefreshCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        SkyTimesWidget.updateWidget(context, appWidgetId)
    }
}