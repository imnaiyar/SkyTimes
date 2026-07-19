package com.imnaiyar.skytimes.utils

import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.constants.events
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

sealed interface Times {
    val nextTime: Instant
    val remaining: Duration

    data class Active(
        override val nextTime: Instant,
        override val remaining: Duration,
        val startTime: Instant,
        val endTime: Instant,
    ) : Times

    data class Inactive(
        override val nextTime: Instant,
        override val remaining: Duration,
    ) : Times
}

data class EventDetails(
    val event: EventData,
    val nextOccurrence: Instant,
    val allOccurrences: List<Instant>,
    val status: Times,
)

object EventTimeUtils {

    private val zone = TimeZone.of("America/Los_Angeles")

    private fun todayInZone(now: Instant): LocalDate {
        return now.toLocalDateTime(zone).date
    }

    private fun matchesRule(event: EventData, date: LocalDate): Boolean {
        val rules = event.occursOn ?: return true

        rules.weekDays?.let { weekdays ->
            if (date.dayOfWeek.isoDayNumber !in weekdays) return false
        }

        rules.dayOfTheMonth?.let { day ->
            if (date.day != day) return false
        }

        return true
    }

    private fun occurrenceOn(date: LocalDate, event: EventData): Instant {
        return date
            .atStartOfDayIn(zone)
            .plus(event.offset.minutes)
    }

    private fun getOccurrenceDay(event: EventData, now: Instant): Instant {
        var date = todayInZone(now)

        while (!matchesRule(event, date)) {
            date = date.plus(1, DateTimeUnit.DAY)
        }

        return occurrenceOn(date, event)
    }

    private fun getNextRecurringOccurrence(event: EventData, now: Instant): Instant {
        var date = todayInZone(now)

        while (true) {
            if (matchesRule(event, date)) {
                val candidate = occurrenceOn(date, event)
                if (candidate >= now) return candidate
            }
            date = date.plus(1, DateTimeUnit.DAY)
        }
    }

    fun getNextOccurrence(event: EventData, now: Instant = Clock.System.now()): Instant {
        val nextOccurrence = getOccurrenceDay(event, now)

        val interval = event.interval ?: return if (event.occursOn != null) {
            if (nextOccurrence >= now) nextOccurrence else getNextRecurringOccurrence(event, now)
        } else {
            nextOccurrence
        }

        var current = nextOccurrence
        while (current < now) {
            current += interval.minutes
        }

        return current
    }

    fun getAllOccurrences(
        event: EventData,
        now: Instant = Clock.System.now()
    ): List<Instant> {
        val intervalMinutes = event.interval
            ?: return listOf(getOccurrenceDay(event, now))

        val occurrences = mutableListOf<Instant>()

        val firstOccurrence = getOccurrenceDay(event, now)
        var current = firstOccurrence

        val day = firstOccurrence.toLocalDateTime(zone).date

        while (current.toLocalDateTime(zone).date == day) {
            occurrences += current
            current += intervalMinutes.minutes
        }

        return occurrences
    }

    fun getStatus(
        event: EventData,
        nextOccurrence: Instant,
        now: Instant = Clock.System.now()
    ): Times {
        val remaining = nextOccurrence - now

        val duration = event.duration
            ?: return Times.Inactive(
                nextTime = nextOccurrence,
                remaining = remaining
            )

        val interval = event.interval
            ?: return Times.Inactive(
                nextTime = nextOccurrence,
                remaining = remaining
            )

        val start = nextOccurrence - interval.minutes
        val end = start + duration.minutes

        return if (now in start..end) {
            Times.Active(
                nextTime = nextOccurrence,
                remaining = end - now,
                startTime = start,
                endTime = end
            )
        } else {
            Times.Inactive(
                nextTime = nextOccurrence,
                remaining = remaining
            )
        }
    }

    fun getEventDetails(
        event: EventData,
        now: Instant = Clock.System.now(),
        includeAllOccurrences: Boolean = true
    ): EventDetails {
        val nextOccurrence = getNextOccurrence(event, now)

        return EventDetails(
            event = event,
            nextOccurrence = nextOccurrence,
            allOccurrences = if (includeAllOccurrences) getAllOccurrences(
                event,
                now
            ) else emptyList(),
            status = getStatus(event, nextOccurrence, now)
        )
    }

    fun allEventDetails(now: Instant = Clock.System.now()): List<EventDetails> =
        events.map { getEventDetails(it, now) }
}
