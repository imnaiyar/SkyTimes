package com.imnaiyar.skytimes.reminders

import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.constants.events
import com.imnaiyar.skytimes.utils.EventTimeUtils
import kotlin.time.Clock
import kotlin.time.Instant

fun reminderTimes(
    reminder: Reminder,
    from: Instant,
    limit: Int
): List<Instant> {
    if (!reminder.enabled || limit <= 0) return emptyList()

    val event = events.firstOrNull { it.key == reminder.eventId } ?: return emptyList()
    return upcomingOccurrences(event, from, limit + 8)
        .map { it.minus(reminder.offsetDuration()) }
        .filter { it >= from }
        .distinct()
        .take(limit)
}

fun upcomingOccurrences(
    event: EventData,
    from: Instant = Clock.System.now(),
    limit: Int = 10
): List<Instant> {
    if (limit <= 0) return emptyList()

    val results = mutableListOf<Instant>()
    var cursor = from

    while (results.size < limit) {
        val nextOccurrence = EventTimeUtils.getNextOccurrence(event, cursor)
        if (nextOccurrence < cursor) break

        results += nextOccurrence
        cursor = nextOccurrence.plus(1, kotlinx.datetime.DateTimeUnit.MILLISECOND)
    }

    return results.distinct().sorted()
}
