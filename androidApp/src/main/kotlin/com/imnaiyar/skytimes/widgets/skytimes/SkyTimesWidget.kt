package com.imnaiyar.skytimes.widgets.skytimes

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.PreviewSizeMode
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.material3.ColorProviders
import com.imnaiyar.skytimes.widgets.WidgetDataProvider
import com.imnaiyar.skytimes.widgets.WidgetSettingsReader
import com.imnaiyar.skytimes.widgets.skytimes.ui.WidgetContent
import com.materialkolor.rememberDynamicColorScheme
import kotlin.time.Clock

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

    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(
        setOf(
            SMALL_BOX,
            BIG_BOX,
            ROW,
            LARGE_ROW,
            COLUMN,
        )
    )


    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)

        provideContent {
            Content(context, launchIntent, id)
        }
    }

    @Composable
    private fun Content(context: Context, launchIntent: Intent? = null, id: GlanceId? = null) {
        val appWidgetId =
            GlanceAppWidgetManager(context).takeIf { id != null }?.getAppWidgetId(id!!)

        val events = WidgetDataProvider.getDisplayEvents(context, Clock.System.now(), appWidgetId)

        val seedColor = WidgetSettingsReader.getSeedColor(context)
        val dark = rememberDynamicColorScheme(seedColor = Color(seedColor), isDark = true)
        val light = rememberDynamicColorScheme(seedColor = Color(seedColor), isDark = false)

        GlanceTheme(colors = ColorProviders(light, dark)) {
            WidgetContent(
                context = context,
                events,
                launchIntent
            )
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {

        provideContent { Content(context) }
    }

    companion object {

        // sizes for the widget
        private val SMALL_BOX = DpSize(90.dp, 90.dp)
        private val BIG_BOX = DpSize(180.dp, 180.dp)
        private val ROW = DpSize(180.dp, 48.dp)
        private val LARGE_ROW = DpSize(300.dp, 48.dp)
        private val COLUMN = DpSize(48.dp, 180.dp)

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
        SkyTimesWidget().update(context, glanceId)
    }
}