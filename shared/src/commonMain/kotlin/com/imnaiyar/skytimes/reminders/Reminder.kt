package com.imnaiyar.skytimes.reminders

import com.imnaiyar.skytimes.constants.EventKey
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

@Serializable
data class Reminder(
    val id: String,
    val eventId: EventKey,
    val enabled: Boolean = true,
    val offsetMinutes: Int = 0,
    val title: String = defaultTitle(eventId),
    val body: String = defaultBody(eventId),
    val metadata: Map<String, String> = emptyMap(),
) {
    init {
        require(offsetMinutes in 0..15) { "offsetMinutes must be between 0 and 15" }
    }

    companion object {
        fun defaultTitle(eventId: EventKey): String =
            eventId.name.lowercase().replace('_', ' ').replaceFirstChar { it.titlecase() } + " reminder"

        fun defaultBody(eventId: EventKey): String = "${eventId.name.lowercase().replace('_', ' ')} is coming up soon"
    }
}

@Serializable
data class ReminderConfig(
    val reminderWindowSize: Int = 8,
)

fun Reminder.offsetDuration() = offsetMinutes.minutes
