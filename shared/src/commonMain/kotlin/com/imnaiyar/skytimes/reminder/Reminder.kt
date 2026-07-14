package com.imnaiyar.skytimes.reminder

import com.imnaiyar.skytimes.constants.EventKey
import kotlinx.serialization.Serializable

/**
 * Represents a user-configurable reminder for a specific event.
 *
 * Each reminder is associated with an event and specifies how far in advance
 * (the [offsetMinutes]) the user wants to be notified before the event occurs.
 *
 * The [offsetMinutes] must be between 0 and 15 inclusive, as per the
 * application's constraints.
 */
@Serializable
data class Reminder(
    val id: String,
    val eventKey: EventKey,
    val enabled: Boolean = true,
    val offsetMinutes: Int = 0,
    val title: String = "",
    val body: String = "",
)

/**
 * Pre-validated offset constraints.
 */
object ReminderConstraints {
    const val MIN_OFFSET_MINUTES = 0
    const val MAX_OFFSET_MINUTES = 15

    fun isValidOffset(minutes: Int): Boolean =
        minutes in MIN_OFFSET_MINUTES..MAX_OFFSET_MINUTES
}
