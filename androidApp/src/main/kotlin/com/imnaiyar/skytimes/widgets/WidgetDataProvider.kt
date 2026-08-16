package com.imnaiyar.skytimes.widgets

import android.content.Context
import android.content.SharedPreferences
import com.imnaiyar.skytimes.core.common.TimeUtils
import com.imnaiyar.skytimes.core.domain.EventDetails
import com.imnaiyar.skytimes.core.domain.EventKey
import com.imnaiyar.skytimes.core.domain.EventTimeUtils
import com.imnaiyar.skytimes.core.domain.Times
import com.imnaiyar.skytimes.core.ui.theme.DefaultThemeColor
import kotlin.time.Instant


data class WidgetEventRowData(
    val eventKey: String,
    val eventName: String,
    val countdownText: String,
    val isActive: Boolean,
)

/**
 * Bridge between shared KMP business logic and Android widget display.
 */
object WidgetDataProvider {

    /**
     * Produces the list of event rows to display on the widget.
     */
    fun getDisplayEvents(
        context: Context,
        now: Instant,
        appWidgetId: Int? = null,
    ): List<WidgetEventRowData> {

        val selectedKeys = WidgetPreferences.getSelectedEvents(context, appWidgetId)

        val allDetails = EventTimeUtils.allEventDetails(now)

        val filtered = allDetails.filter { it.event.key in selectedKeys }

        // sort active events first, then by next occurrence ascending
        val sorted = filtered.sortedWith(
            compareByDescending<EventDetails> { detail ->
                detail.status is Times.Active
            }.thenBy { it.nextOccurrence }
        )

        val timeUtils = TimeUtils()
        return sorted.map { detail ->
            val remaining = detail.status.remaining
            val remainingMs = remaining.inWholeMilliseconds
            val isActive = detail.status is Times.Active

            val countdownText = when {
                isActive -> {
                    if (remainingMs <= 0L) {
                        "Active now"
                    } else {
                        "Ends in ${timeUtils.formatMillis(remainingMs, withSeconds = false)}"
                    }
                }

                else -> {
                    val millis =
                        detail.nextOccurrence.toEpochMilliseconds() - now.toEpochMilliseconds()
                    if (millis <= 0L) {
                        "Starting soon"
                    } else {
                        "Starts in ${timeUtils.formatMillis(millis, withSeconds = false)}"
                    }
                }
            }

            WidgetEventRowData(
                eventKey = detail.event.key.name,
                eventName = detail.event.name,
                countdownText = countdownText,
                isActive = isActive,
            )
        }
    }
}


/**
 * Per-widget-instance persistent configuration.
 */
object WidgetPreferences {
    private const val PREFS_NAME = "skytimes_widget_prefs"
    private const val KEY_SELECTED_EVENTS = "widget_%d_selected_events"
    private const val KEY_LAST_UPDATE = "widget_%d_last_update"

    /**
     * Returns the set of [EventKey]s enabled for display on the given widget instance.
     */
    fun getSelectedEvents(context: Context, appWidgetId: Int? = null): Set<EventKey> {
        if (appWidgetId == null) return EventKey.entries.toSet()

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(keyFor(KEY_SELECTED_EVENTS, appWidgetId), null)

        if (raw.isNullOrBlank()) return EventKey.entries.toSet()

        return raw.split("|")
            .mapNotNull { name ->
                try {
                    EventKey.valueOf(name.trim())
                } catch (_: IllegalArgumentException) {
                    null // Silently skip removed/renamed enum values
                }
            }
            .toSet()
            .ifEmpty { EventKey.entries.toSet() } // Fallback if all values are invalid
    }

    /**
     * Saves the set of enabled [EventKey]s for the given widget instance.
     */
    fun setSelectedEvents(context: Context, appWidgetId: Int, events: Set<EventKey>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            if (events.isEmpty()) {
                remove(keyFor(KEY_SELECTED_EVENTS, appWidgetId))
            } else {
                val serialized = events.joinToString("|") { it.name }
                putString(keyFor(KEY_SELECTED_EVENTS, appWidgetId), serialized)
            }
        }
    }

    /**
     * Removes all stored preferences for a deleted widget instance.
     */
    fun removeWidget(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(keyFor(KEY_SELECTED_EVENTS, appWidgetId))
            remove(keyFor(KEY_LAST_UPDATE, appWidgetId))
        }
    }

    private fun keyFor(template: String, appWidgetId: Int): String =
        template.format(appWidgetId)

    /**
     * Inline helper to reduce boilerplate when editing SharedPreferences.
     */
    private inline fun SharedPreferences.edit(
        block: SharedPreferences.Editor.() -> Unit
    ) {
        edit().apply(block).apply()
    }
}


object WidgetSettingsReader {

    /**
     * These keys are used by setting repository, we just need to read it
     */
    private const val KEY_USE_24_HOUR_CLOCK = "use_24_hour_clock"

    private const val SEED_COLOR_KEY = "theme_color"


    private fun getPref(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            "${context.packageName}_preferences",
            Context.MODE_PRIVATE
        )
    }

    fun is24HourClock(context: Context): Boolean {
        return getPref(context).getBoolean(KEY_USE_24_HOUR_CLOCK, true)
    }

    fun getSeedColor(context: Context): Int {
        return getPref(context).getInt(SEED_COLOR_KEY, DefaultThemeColor.toInt())
    }
}