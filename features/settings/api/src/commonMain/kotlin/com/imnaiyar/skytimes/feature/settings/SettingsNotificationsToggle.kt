package com.imnaiyar.skytimes.feature.settings

import com.imnaiyar.skytimes.feature.reminders.NotificationsToggle

/**
 * Bridges the app's notifications preference to the reminders flow without
 * coupling the reminders module to settings storage.
 */
class SettingsNotificationsToggle(
    private val repository: SettingsRepository
) : NotificationsToggle {
    override fun isEnabled(): Boolean = repository.settings.value.notificationsEnabled

    override suspend fun setEnabled(enabled: Boolean) {
        repository.setNotificationsEnabled(enabled)
    }
}
