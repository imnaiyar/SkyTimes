package com.imnaiyar.skytimes.core.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
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
