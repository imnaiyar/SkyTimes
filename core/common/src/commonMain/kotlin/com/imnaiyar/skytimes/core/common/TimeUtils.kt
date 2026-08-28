package com.imnaiyar.skytimes.core.common

import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormatBuilder
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** The device's current system time zone. */
val LocalTimeZone = TimeZone.currentSystemDefault()

enum class ClockFormat {
    HOUR_12,
    HOUR_24
}

open class TimeUtils {
    private val hour12Format = LocalTime.Format {
        amPmHour()
        char(':')
        minute()
        char(':')
        second()
        char(' ')
        amPmMarker("AM", "PM")
    }

    private val hour12FormatNoSeconds = LocalTime.Format {
        amPmHour()
        char(':')
        minute()
        char(' ')
        amPmMarker("AM", "PM")
    }

    private val hour24Format = LocalTime.Format {
        hour()
        char(':')
        minute()
        char(':')
        second()
    }

    private val hour24FormatNoSeconds = LocalTime.Format {
        hour()
        char(':')
        minute()
    }

    /**
     * Returns the current time in the specified time zone.
     * If no time zone is provided, it returns the current time in the system's default
     */
    fun getTime(timeZone: TimeZone = LocalTimeZone): LocalDateTime {
        val now = Clock.System.now()
        return now.toLocalDateTime(timeZone)
    }

    private fun _toZone(time: Instant, timeZone: TimeZone): LocalTime {

        return time.toLocalDateTime(timeZone).time
    }

    fun toZone(time: Instant): LocalTime {
        return _toZone(time, LocalTimeZone)
    }

    fun toZone(time: Instant, timeZone: TimeZone): LocalTime {
        return _toZone(time, timeZone)
    }

    fun formatMillis(
        millis: Long,
        withSeconds: Boolean = true,
    ): String {
        var totalSeconds = millis / 1000

        if (!withSeconds) {
            totalSeconds = (totalSeconds / 60 + 1) * 60  // round to next minute
        }

        val days = totalSeconds / 86_400
        totalSeconds %= 86_400

        val hours = totalSeconds / 3_600
        totalSeconds %= 3_600

        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return buildList {
            if (days > 0) add("${days}d")
            if (hours > 0 || isNotEmpty()) add("${hours.toString().padStart(2, '0')}h")
            add("${minutes.toString().padStart(2, '0')}m")
            if (withSeconds) add("${seconds.toString().padStart(2, '0')}s")
        }.joinToString(" ")
    }

    /**
     * Formats the given LocalTime into a string representation based on the specified clock format.
     * @param timeValue The TimeValue to be formatted.
     * @param use24HourClock Whether to use 24-hour clock format.
     * @param zone The time zone to use.
     * @param withSeconds Whether to include seconds in the formatted string.
     * @return A string representation of the time in the specified format.
     */
    open fun formatTime(
        timeValue: TimeValue,
        use24HourClock: Boolean,
        zone: TimeZone = LocalTimeZone,
        withSeconds: Boolean = true
    ): String {
        val time = when (timeValue) {
            is TimeValue.localTime -> timeValue.time
            is TimeValue.instant -> toZone(timeValue.instant, zone)
        }
        val clockFormat = if (use24HourClock) ClockFormat.HOUR_24 else ClockFormat.HOUR_12

        return when (clockFormat) {
            ClockFormat.HOUR_12 -> if (withSeconds) hour12Format.format(time) else hour12FormatNoSeconds.format(time)
            ClockFormat.HOUR_24 -> if (withSeconds) hour24Format.format(time) else hour24FormatNoSeconds.format(time)
        }
    }

}

@Stable
class TimeFormatter(
    private val use24HourClock: Boolean,
) : TimeUtils() {

    fun formatTime(
        time: TimeValue,
        zone: TimeZone = LocalTimeZone,
        withSeconds: Boolean = true
    ): String =
        super.formatTime(time, use24HourClock, zone, withSeconds)

    fun format(
        instant: Instant,
        zone: TimeZone = LocalTimeZone,
        withSeconds: Boolean = true
    ): String =
        formatTime(TimeValue.instant(instant), zone, withSeconds)

    fun format(
        localTime: LocalTime,
        zone: TimeZone = LocalTimeZone,
        withSeconds: Boolean = true
    ): String =
        formatTime(TimeValue.localTime(localTime), zone, withSeconds)
}

private fun DateTimeFormatBuilder.WithDate.dayMonthYear() {
    day()
    char('-')
    monthNumber()
    char('-')
    year()
}

val isoDateFormat = DateTimeComponents.Format {
    dayMonthYear()
}

val localDateToIso = LocalDate.Format {
    dayMonthYear()
}

sealed interface TimeValue {
    data class instant(val instant: Instant) : TimeValue
    data class localTime(val time: LocalTime) : TimeValue
}
