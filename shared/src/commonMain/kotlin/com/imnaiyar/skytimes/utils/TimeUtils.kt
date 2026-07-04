package com.imnaiyar.skytimes.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.imnaiyar.skytimes.constants.GameTimeZone
import com.imnaiyar.skytimes.di.LocalAppContainer
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

    private val hour24Format = LocalTime.Format {
        hour()
        char(':')
        minute()
        char(':')
        second()
    }

    /**
     * Returns the current time in the specified time zone.
     * If no time zone is provided, it returns the current time in the system's default
     */
    fun getTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
        val now = Clock.System.now()
        return now.toLocalDateTime(timeZone)
    }

    private fun _toZone(time: Instant, timeZone: TimeZone): LocalTime {

        return time.toLocalDateTime(timeZone).time
    }

    fun toZone(time: Instant): LocalTime {
        return _toZone(time, TimeZone.currentSystemDefault())
    }

    fun toZone(time: Instant, timeZone: TimeZone): LocalTime {
        return _toZone(time, timeZone)
    }

    fun formatMillis(millis: Long, withSeconds: Boolean = true): String {
        var totalSeconds = millis / 1000

        if (!withSeconds && totalSeconds % 60 != 0L) {
            totalSeconds += 60 - (totalSeconds % 60)
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
     * @param time The LocalTime to be formatted.
     * @param clockFormat The desired clock format (12-hour or 24-hour). Defaults to 24-hour format.
     * @return A string representation of the time in the specified format.
     */
    open fun formatTime(timeValue: TimeValue, use24HourClock: Boolean): String {
        val time = when (timeValue) {
            is TimeValue.localTime -> timeValue.time
            is TimeValue.instant -> timeValue.instant.toLocalDateTime(TimeZone.currentSystemDefault()).time
        }
        val clockFormat = if (use24HourClock) ClockFormat.HOUR_24 else ClockFormat.HOUR_12

        return when (clockFormat) {
            ClockFormat.HOUR_12 -> hour12Format.format(time)
            ClockFormat.HOUR_24 -> hour24Format.format(time)
        }
    }

}

@Stable
class TimeFormatter(
    private val use24HourClock: Boolean,
) : TimeUtils() {

    fun formatTime(time: TimeValue): String =
        super.formatTime(time, use24HourClock)

    fun format(instant: Instant): String =
        formatTime(TimeValue.instant(instant))

    fun format(localTime: LocalTime): String =
        formatTime(TimeValue.localTime(localTime))
}

/**
 * Helper utility with user settings respected passed
 */
@Composable
fun rememberTimeFormatter(): TimeFormatter {
    val settings = LocalAppContainer.current.settingsRepository.settings.collectAsState()

    return remember(settings.value.use24HourClock) {
        TimeFormatter(
            use24HourClock = settings.value.use24HourClock,
        )
    }
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

fun isTodayInGame(value: String): Boolean {
    val lastUpdatedDate = parseAsGameDate(value) ?: return false
    val today = Clock.System.now().toLocalDateTime(GameTimeZone).date
    return lastUpdatedDate == today
}

fun parseAsGameDate(value: String): LocalDate? {
    return runCatching { Instant.parse(value).toLocalDateTime(GameTimeZone).date }
        .getOrNull()
        ?: runCatching { LocalDateTime.parse(value).date }.getOrNull()
        ?: value.take(10).let { date ->
            runCatching { LocalDate.parse(date) }.getOrNull()
        }
}

sealed interface TimeValue {
    data class instant(val instant: Instant) : TimeValue
    data class localTime(val time: LocalTime) : TimeValue
}
