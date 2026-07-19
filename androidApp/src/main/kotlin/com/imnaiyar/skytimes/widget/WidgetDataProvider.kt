package com.imnaiyar.skytimes.widget

import android.content.Context
import android.content.SharedPreferences
import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.theme.DefaultThemeColor
import com.imnaiyar.skytimes.utils.TimeUtils
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
        appWidgetId: Int,
        now: Instant,
    ): List<WidgetEventRowData> {
        // 1. Load user's event selection for this widget instance
        val selectedKeys = WidgetPreferences.getSelectedEvents(context, appWidgetId)

        // 2. Get full event details from shared business logic
        val allDetails = com.imnaiyar.skytimes.utils.EventTimeUtils.allEventDetails(now)

        // 3. Filter to selected events only
        val filtered = allDetails.filter { it.event.key in selectedKeys }

        // 4. Sort: active events first, then by next occurrence ascending
        val sorted = filtered.sortedWith(
            compareByDescending<com.imnaiyar.skytimes.utils.EventDetails> { detail ->
                detail.status is com.imnaiyar.skytimes.utils.Times.Active
            }.thenBy { it.nextOccurrence }
        )

        // 5. Map to display data
        val timeUtils = TimeUtils()
        return sorted.map { detail ->
            val remaining = detail.status.remaining
            val remainingMs = remaining.inWholeMilliseconds
            val isActive = detail.status is com.imnaiyar.skytimes.utils.Times.Active

            val countdownText = when {
                // Active events: show "Active now" or "Ends in X"
                isActive -> {
                    if (remainingMs <= 0L) {
                        "Active now"
                    } else {
                        "Ends in ${timeUtils.formatMillis(remainingMs, withSeconds = false)}"
                    }
                }
                // Inactive events: show "Starts in X"
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
    fun getSelectedEvents(context: Context, appWidgetId: Int): Set<EventKey> {
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
     * Records the last time this widget instance was updated for diagnostics
     */
    fun recordUpdate(context: Context, appWidgetId: Int, timestampMs: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putLong(keyFor(KEY_LAST_UPDATE, appWidgetId), timestampMs)
        }
    }

    /**
     * Returns the last update timestamp for this widget, or 0 if never updated.
     */
    fun getLastUpdate(context: Context, appWidgetId: Int): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(keyFor(KEY_LAST_UPDATE, appWidgetId), 0L)
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
     * This key is used by setting repository, we just need to read it
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