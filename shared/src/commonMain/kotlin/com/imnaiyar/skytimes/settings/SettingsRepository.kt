package com.imnaiyar.skytimes.settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SettingsRepository(
    private val storage: Settings = Settings()
) {
    private val updateMutex = Mutex()
    private val _settings = MutableStateFlow(loadSettings())

    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    suspend fun updateTheme(mode: ThemeMode) {
        update { current -> current.copy(themeMode = mode) }
    }

    suspend fun set24HourClock(enabled: Boolean) {
        update { current -> current.copy(use24HourClock = enabled) }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        update { current -> current.copy(notificationsEnabled = enabled) }
    }

    suspend fun setClockAnimation(enabled: Boolean) {
        update { current -> current.copy(clockAnimation = enabled) }
    }

    private suspend inline fun update(transform: (AppSettings) -> AppSettings) {
        updateMutex.withLock {
            val current = _settings.value
            val next = transform(current)

            if (next == current) return

            // Persist before publishing so collectors never observe state that is not durable.
            saveChangedSettings(current, next)
            _settings.value = next
        }
    }

    private fun loadSettings(): AppSettings {
        return AppSettings(
            themeMode = storage.getStringOrNull(SettingsKeys.ThemeMode)
                ?.let(::parseThemeMode)
                ?: AppSettings().themeMode,
            use24HourClock = storage.getBoolean(
                SettingsKeys.Use24HourClock,
                AppSettings().use24HourClock
            ),
            notificationsEnabled = storage.getBoolean(
                SettingsKeys.NotificationsEnabled,
                AppSettings().notificationsEnabled
            ),
            clockAnimation = storage.getBoolean(
                SettingsKeys.ClockAnimation,
                AppSettings().clockAnimation
            )
        )
    }

    private fun saveChangedSettings(current: AppSettings, next: AppSettings) {
        if (current.themeMode != next.themeMode) {
            storage.putString(SettingsKeys.ThemeMode, next.themeMode.name)
        }
        if (current.use24HourClock != next.use24HourClock) {
            storage.putBoolean(SettingsKeys.Use24HourClock, next.use24HourClock)
        }
        if (current.notificationsEnabled != next.notificationsEnabled) {
            storage.putBoolean(SettingsKeys.NotificationsEnabled, next.notificationsEnabled)
        }
        if (current.clockAnimation != next.clockAnimation) {
            storage.putBoolean(SettingsKeys.ClockAnimation, next.clockAnimation)
        }
    }

    private fun parseThemeMode(value: String): ThemeMode {
        return ThemeMode.entries.firstOrNull { it.name == value } ?: ThemeMode.SYSTEM
    }
}

private object SettingsKeys {
    const val ThemeMode = "theme_mode"
    const val Use24HourClock = "use_24_hour_clock"
    const val NotificationsEnabled = "notifications_enabled"
    const val ClockAnimation = "clock_animation"
}

