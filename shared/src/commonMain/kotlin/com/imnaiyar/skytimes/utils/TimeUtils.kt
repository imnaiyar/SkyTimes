package com.imnaiyar.skytimes.utils

import kotlinx.datetime.*
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.format.char
import kotlin.time.Clock
import kotlin.time.Instant

enum class ClockFormat {
    HOUR_12,
    HOUR_24
}
class TimeUtils {
    /**
     * Returns the current time in the specified time zone.
     * If no time zone is provided, it returns the current time in the system's default
     */
    fun getTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalTime {
        val now = Clock.System.now()
        return now.toLocalDateTime(timeZone).time
    }

    fun toZone(time: Instant, timeZone: String? = null): LocalTime {
        val zone = if (timeZone != null)  TimeZone.of(timeZone) else TimeZone.currentSystemDefault()

        return time.toLocalDateTime(zone).time
    }


    /**
     * Formats the given LocalTime into a string representation based on the specified clock format.
     * @param time The LocalTime to be formatted.
     * @param clockFormat The desired clock format (12-hour or 24-hour). Defaults to 24-hour format.
     * @return A string representation of the time in the specified format.
     */
    @OptIn(FormatStringsInDatetimeFormats::class)
    fun formatTime(time: LocalTime, clockFormat: ClockFormat = ClockFormat.HOUR_12): String {
        val formatByHour = { is24: Boolean ->
            LocalTime.Format {
            if (!is24) amPmHour() else hour()
            char(':')
            minute()
            char(':')
            second()
            if (!is24) amPmMarker("AM", "PM")
            }
        }

      return when (clockFormat) {
            ClockFormat.HOUR_12 -> formatByHour(false).format(time)
            ClockFormat.HOUR_24 -> formatByHour(true).format(time)
        }
    }

}