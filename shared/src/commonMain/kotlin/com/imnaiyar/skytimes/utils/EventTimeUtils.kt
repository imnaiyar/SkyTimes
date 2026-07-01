package com.imnaiyar.skytimes.utils

import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.constants.events
import kotlinx.datetime.*
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

val timeUtils = TimeUtils()
object EventTimeUtils {

    private val zone = TimeZone.of("America/Los_Angeles")

    private fun todayInZone(): LocalDate {
        return timeUtils.getTime(zone).date
    }

    private fun getOccurrenceDay(event: EventData): Instant {
        var date = todayInZone()

        if (event.occursOn != null) {

            event.occursOn.weekDays?.let { weekdays ->
                while (date.dayOfWeek.isoDayNumber !in weekdays) {
                    date = date.plus(1, DateTimeUnit.DAY)
                }
            }

            event.occursOn.dayOfTheMonth?.let { day ->
                while (date.day != day) {
                    date = date.plus(1, DateTimeUnit.DAY)
                }
            }
        }

        return date
            .atStartOfDayIn(zone)
            .plus(event.offset.minutes)
    }

    fun getNextOccurrence(event: EventData): Instant {
        val now = Clock.System.now()

        var nextOccurrence = getOccurrenceDay(event)

        val interval = event.interval ?: return nextOccurrence

        while (nextOccurrence < now) {
            nextOccurrence += interval.minutes
        }

        return nextOccurrence
    }

    fun getAllOccurrences(event: EventData): List<Instant> {
        val intervalMinutes = event.interval
            ?: return listOf(getOccurrenceDay(event))

        val occurrences = mutableListOf<Instant>()

        val firstOccurrence = getOccurrenceDay(event)
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
        nextOccurrence: Instant
    ): Times {
        val now = Clock.System.now()

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

    fun getEventDetails(event: EventData): EventDetails {
        val nextOccurrence = getNextOccurrence(event)

        return EventDetails(
            event = event,
            nextOccurrence = nextOccurrence,
            allOccurrences = getAllOccurrences(event),
            status = getStatus(event, nextOccurrence)
        )
    }

    fun allEventDetails(): List<EventDetails> =
        events.map(::getEventDetails)
}