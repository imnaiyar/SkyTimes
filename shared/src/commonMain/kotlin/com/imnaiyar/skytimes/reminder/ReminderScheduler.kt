package com.imnaiyar.skytimes.reminder

import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.utils.EventTimeUtils
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// ──────────────────────────────────────────────
// Platform-independent scheduler abstraction
// ──────────────────────────────────────────────

/**
 * Platform-independent interface for scheduling reminder notifications.
 *
 * The shared code uses this interface to express **what** needs scheduling;
 * each platform provides a concrete implementation that decides **how**
 * the scheduling is performed (e.g. AlarmManager on Android,
 * UNUserNotificationCenter on iOS).
 */
interface ReminderScheduler {

    /**
     * Re-evaluates all currently persisted reminders and ensures that
     * platform alarms/notifications are up to date.
     *
     * Called on app launch, after settings changes, and when more
     * notification slots need to be filled (iOS replenishment).
     */
    suspend fun refresh()

    /**
     * Schedules (or re-schedules) a specific reminder.
     *
     * If the reminder is disabled, this is equivalent to [cancelReminder].
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
 *
 * Algorithm:
 * 1. Compute successive event occurrences starting from [from].
 * 2. Subtract the reminder's [Reminder.offsetMinutes] from each occurrence.
 * 3. Discard times that are in the past (relative to [from]).
 * 4. Return at most [limit] times.
 *
 * Both Android and iOS implementations should call this function so that
 * scheduling logic is never duplicated.
 *
 * @param reminder  The reminder configuration.
 * @param event     The [EventData] describing the event's recurrence pattern.
 * @param from      The reference instant — only times strictly after this
 *                  are returned.
 * @param limit     Maximum number of future times to return.
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
            nextOccurrence + kotlin.time.Duration.Companion.nanoseconds(1)
        )
    }

    return results
}
