package com.imnaiyar.skytimes.widget.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.GridCells
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import com.imnaiyar.skytimes.R
import com.imnaiyar.skytimes.utils.TimeFormatter
import com.imnaiyar.skytimes.widget.WidgetDataProvider
import com.imnaiyar.skytimes.widget.WidgetEventRowData
import com.imnaiyar.skytimes.widget.WidgetPreferences
import com.imnaiyar.skytimes.widget.WidgetRefreshCallback
import com.imnaiyar.skytimes.widget.WidgetSettingsReader
import kotlin.time.Clock

@Composable
fun WidgetContent(
    context: Context,
    appWidgetId: Int,
) {
    val now = Clock.System.now()
    val events = WidgetDataProvider.getDisplayEvents(context, appWidgetId, now)

    // Record the update timestamp for diagnostics
    WidgetPreferences.recordUpdate(context, appWidgetId, now.toEpochMilliseconds())


    val timeUtils = TimeFormatter(WidgetSettingsReader.is24HourClock(context))

    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)

    if (events.isEmpty()) throw IllegalStateException("Widget has no events")
    
    val launchModifier = if (launchIntent != null) GlanceModifier.clickable(
        onClick = actionStartActivity(
            launchIntent
        )
    ) else GlanceModifier

    Scaffold(
        modifier = launchModifier,
        titleBar = {
            TopBar(
                onRefreshClick = actionRunCallback<WidgetRefreshCallback>(),
                timeUtils.format(now)
            )
        },
        backgroundColor = GlanceTheme.colors.widgetBackground,
        horizontalPadding = WidgetTheme.contentPadding
    ) {
        LazyVerticalGrid(
            gridCells = GridCells.Adaptive(minSize = 120.dp),
            modifier = GlanceModifier
                .padding(top = 10.dp)
                .fillMaxSize()
        ) {
            items(items = events) { event ->
                Column(
                    modifier = GlanceModifier.fillMaxWidth().padding(2.dp),
                ) {
                    WidgetEventRow(data = event)
                }
            }
        }
    }
}


/**
 * Shown when no events are enabled — prompts the user to configure.
 */
@Composable
private fun WidgetEmptyState(
    onClick: Action,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No events selected",
            style = WidgetTheme.emptyStateStyle,
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Tap to configure",
            style = WidgetTheme.emptyStateStyle.copy(
                color = WidgetTheme.accent,
            ),
        )
    }
}


@Composable
private fun TopBar(onRefreshClick: Action, updatedAt: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier.padding(10.dp).fillMaxWidth()
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    ImageProvider(R.mipmap.app_icon_foreground),
                    contentDescription = "App Icon",
                    modifier = GlanceModifier.size(36.dp)
                )
                Text(
                    "SkyTimes",
                    style = WidgetTheme.headerTitleStyle,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
            Text("Last updated at $updatedAt", style = WidgetTheme.countdownStyle)
        }
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.refresh_icon),
            contentDescription = "Refresh",
            contentColor = GlanceTheme.colors.secondary,
            backgroundColor = null, // transparent
            onClick = onRefreshClick
        )
    }
    Divider(modifier = GlanceModifier.padding(horizontal = 10.dp))
}


@Composable
private fun WidgetEventRow(
    data: WidgetEventRowData,
    modifier: GlanceModifier = GlanceModifier,
) {
    val bgColor =
        if (data.isActive) WidgetTheme.activeBackground else GlanceTheme.colors.secondaryContainer

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(bgColor)
            .cornerRadius(WidgetTheme.activeRowCornerRadius)
    ) {
        // Event name
        Text(
            text = data.eventName,
            style = if (data.isActive) WidgetTheme.eventNameActiveStyle else WidgetTheme.eventNameStyle,
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 1,
        )

        // Countdown / status
        Text(
            text = data.countdownText,
            style = if (data.isActive) WidgetTheme.countdownActiveStyle else WidgetTheme.countdownStyle,
            maxLines = 1,
        )
    }
}
