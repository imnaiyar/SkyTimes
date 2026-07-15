package com.imnaiyar.skytimes.reminder

import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.utils.EventTimeUtils
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Instant

// ──────────────────────────────────────────────
// Platform-independent scheduler abstraction
// ──────────────────────────────────────────────

/**
 * Platform-independent interface for scheduling reminder notifications.
 */
interface ReminderScheduler {

    /**
     * Re-evaluates all currently persisted reminders and ensures that
     * platform alarms/notifications are up to date.
     */
    suspend fun refresh()

    /**
     * Schedules (or re-schedules) a specific reminder.
     */
    suspend fun scheduleReminder(reminder: Reminder)

    /**
     * Cancels all pending notifications for the given event.
     */
    suspend fun cancelReminder(eventId: String)

    /**
     * Cancels every pending reminder notification across all events.
     */
    suspend fun cancelAll()
}

// ──────────────────────────────────────────────
// Shared helper logic – used by both platforms
// ──────────────────────────────────────────────

/**
 * Produces an ordered list of future [Instant]s at which a reminder
 * notification should fire.
 */
fun reminderTimes(
    reminder: Reminder,
    event: EventData,
    from: Instant,
    limit: Int,
): List<Instant> {
    if (!reminder.enabled) return emptyList()
    if (!ReminderConstraints.isValidOffset(reminder.offsetMinutes)) return emptyList()

    val offset: Duration = reminder.offsetMinutes.minutes
    val results = mutableListOf<Instant>()

    // Seed the search with the first occurrence on or after [from].
    var nextOccurrence = EventTimeUtils.getNextOccurrence(event, from)

    // Walk forward until we have collected enough future times.
    while (results.size < limit) {
        val reminderTime = nextOccurrence - offset
        if (reminderTime > from) {
            results += reminderTime
        }
        // Advance past the current occurrence so we land on a later one.
        nextOccurrence = EventTimeUtils.getNextOccurrence(
            event,
            nextOccurrence + 1.nanoseconds
        )
    }

    return results
}
