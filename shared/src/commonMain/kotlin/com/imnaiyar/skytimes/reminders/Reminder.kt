package com.imnaiyar.skytimes.reminders

import androidx.compose.runtime.Composable
import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.constants.events
import com.imnaiyar.skytimes.repositories.SettingsRepository
import com.imnaiyar.skytimes.utils.EventTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Schedular interface to be implemented by each platform */
interface ReminderScheduler {
    suspend fun refresh()
    suspend fun scheduleReminder(reminder: Reminder)
    suspend fun cancelReminder(eventId: String)
    suspend fun cancelAll()
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean

    fun hasExactAlarm(): Boolean
    fun requestExactAlarm(): Unit
}


/**
 * This is only needed for android, because requesting permission requires activity
 * and the utility fun for it is a [Composable], so requires different impl.
 * iOS's is simple and is handled by [ReminderScheduler.requestPermission]
 */
@Composable
expect fun rememberNotificationPermissionRequester(): ((Boolean) -> Unit) -> Unit

expect fun getReminderSchedular(
    settingsRepository: SettingsRepository,
    reminderRepository: ReminderRepository,
    scope: CoroutineScope
): ReminderScheduler

@Serializable
data class Reminder(
    val id: String,
    val eventId: EventKey,
    val enabled: Boolean = true,
    val offsetMinutes: Int = 0,
    val title: String = defaultTitle(eventId),
    val body: String = defaultBody(eventId, offsetMinutes),
    val metadata: Map<String, String> = emptyMap(),
) {
    init {
        require(offsetMinutes in 0..15) { "offsetMinutes must be between 0 and 15" }
    }

    companion object {
        private fun capitalizeName(eventId: EventKey): String =
            eventId.name.lowercase().replace('_', ' ').split(' ')
                .joinToString(" ") { name -> name.replaceFirstChar { it.titlecase() } }

        fun defaultTitle(eventId: EventKey): String = capitalizeName(eventId) + " reminder"

        fun defaultBody(eventId: EventKey, offset: Int): String =
            capitalizeName(eventId) + if (offset > 0) " will start in $offset minutes" else "is active"
    }
}

@Serializable
data class ReminderConfig(
    val reminderWindowSize: Int = 8,
)

fun Reminder.offsetDuration() = offsetMinutes.minutes


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
        cursor = nextOccurrence.plus(1.milliseconds)
    }

    return results.distinct().sorted()
}