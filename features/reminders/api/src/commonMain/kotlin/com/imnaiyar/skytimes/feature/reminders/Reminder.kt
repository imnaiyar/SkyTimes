package com.imnaiyar.skytimes.feature.reminders

import androidx.compose.runtime.Composable
import com.imnaiyar.skytimes.core.domain.EventData
import com.imnaiyar.skytimes.core.domain.EventKey
import com.imnaiyar.skytimes.core.domain.EventTimeUtils
import com.imnaiyar.skytimes.core.domain.GameTimeZone
import com.imnaiyar.skytimes.core.domain.events
import com.imnaiyar.skytimes.core.domain.getShard
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
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


/** Platform controller for notification permission prompts and settings redirects. */
@Composable
expect fun rememberReminderPermissionController(): ReminderPermissionController

enum class ReminderPermissionStatus {
    Granted,
    Requestable,
    SettingsRequired,
    Unavailable,
}

interface ReminderPermissionController {
    suspend fun notificationStatus(): ReminderPermissionStatus
    suspend fun requestNotificationPermission(): ReminderPermissionStatus
    fun openNotificationSettings()
}

@Serializable
enum class ShardReminderType { BLACK, RED, BOTH }

@Serializable
data class Reminder(
    val id: String,
    val eventId: EventKey,
    val enabled: Boolean = true,
    val offsetMinutes: Int = 0,
    val shardType: ShardReminderType? = null,
    val metadata: Map<String, String> = emptyMap(),
) {
    init {
        require(offsetMinutes in 0..15) { "offsetMinutes must be between 0 and 15" }
        require((eventId == EventKey.SHARDS) == (shardType != null)) {
            "shardType is only valid for shard reminders"
        }
    }

    companion object {
        private fun capitalizeName(eventId: EventKey): String =
            eventId.name.lowercase().replace('_', ' ').split(' ')
                .joinToString(" ") { name -> name.replaceFirstChar { it.titlecase() } }

        fun title(eventId: EventKey): String = if (eventId == EventKey.SHARDS) "Shard reminder"
        else capitalizeName(eventId) + " reminder"

        fun body(reminder: Reminder, notificationTime: Instant = Clock.System.now()): String =
            if (reminder.eventId == EventKey.SHARDS) {
                val shardType = shardTypeForNotification(reminder, notificationTime)
                val shardLabel = when (shardType) {
                    ShardReminderType.RED -> "A red shard"
                    ShardReminderType.BLACK -> "A black shard"
                    ShardReminderType.BOTH, null -> "A shard"
                }
                shardLabel + if (reminder.offsetMinutes > 0) {
                    " will land in ${reminder.offsetMinutes} minutes"
                } else " is landing"
            } else {
                capitalizeName(reminder.eventId) + if (reminder.offsetMinutes > 0) {
                    " will start in ${reminder.offsetMinutes} minutes"
                } else " is active"
            }
    }
}

private fun shardTypeForNotification(
    reminder: Reminder,
    notificationTime: Instant,
): ShardReminderType? {
    val configuredType = reminder.shardType ?: return null
    if (configuredType != ShardReminderType.BOTH) return configuredType

    val targetLanding = notificationTime.plus(reminder.offsetDuration())
    val date = notificationTime.toLocalDateTime(GameTimeZone).date
    return (0..2).asSequence()
        .map { LocalDate.fromEpochDays(date.toEpochDays() - 1 + it) }
        .mapNotNull(::getShard)
        .flatMap { shard ->
            shard.occurrences.asSequence().map { occurrence ->
                (occurrence.shardLand - targetLanding).absoluteValue to shard.isRed
            }
        }
        .minByOrNull { it.first }
        ?.second
        ?.let { if (it) ShardReminderType.RED else ShardReminderType.BLACK }
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

    if (reminder.eventId == EventKey.SHARDS) return shardReminderTimes(reminder, from, limit)
    val event = events.firstOrNull { it.key == reminder.eventId } ?: return emptyList()

    return upcomingOccurrences(event, from, limit + 8)
        .map { it.minus(reminder.offsetDuration()) }
        .filter { it >= from }
        .distinct()
        .take(limit)
}

fun shardReminderTimes(reminder: Reminder, from: Instant, limit: Int): List<Instant> {
    if (!reminder.enabled || limit <= 0) return emptyList()
    val type = reminder.shardType ?: return emptyList()
    val startDate = from.toLocalDateTime(GameTimeZone).date
    return (0..30).asSequence()
        .map { LocalDate.fromEpochDays(startDate.toEpochDays() + it) }
        .mapNotNull(::getShard)
        .filter { shard ->
            type == ShardReminderType.BOTH ||
                    (type == ShardReminderType.RED) == shard.isRed
        }
        .flatMap { shard -> shard.occurrences.asSequence().map { it.shardLand } }
        .map { it.minus(reminder.offsetDuration()) }
        .filter { it >= from }
        .distinct()
        .sorted()
        .take(limit)
        .toList()
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
