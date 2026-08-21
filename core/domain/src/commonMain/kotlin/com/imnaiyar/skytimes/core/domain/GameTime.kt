package com.imnaiyar.skytimes.core.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

val GameTimeZone = TimeZone.of("America/Los_Angeles")

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

// similar to how luxon's <DateTime>.isInDST does it
/**
 * Check whether this instant is in DST for the given timezone
 */
fun Instant.isInDST(timeZone: TimeZone): Boolean {
    val year = toLocalDateTime(timeZone).year
    val januaryOffset = LocalDate(year, 1, 1).atStartOfDayIn(timeZone).offsetInSeconds(timeZone)
    val mayOffset = LocalDate(year, 5, 1).atStartOfDayIn(timeZone).offsetInSeconds(timeZone)
    val standardOffset = minOf(januaryOffset, mayOffset)

    return offsetInSeconds(timeZone) > standardOffset
}

private fun Instant.offsetInSeconds(timeZone: TimeZone): Int {
    return timeZone.offsetAt(this).totalSeconds
}
