package com.imnaiyar.skytimes.feature.reminders

/**
 * Abstraction over the global notifications preference so the reminder flow
 * stays decoupled from any particular settings storage.
 */
interface NotificationsToggle {
    fun isEnabled(): Boolean
    suspend fun setEnabled(enabled: Boolean)
}
