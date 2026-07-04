package com.imnaiyar.skytimes.repositories

import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.theme.ThemeContrast
import com.imnaiyar.skytimes.theme.ThemeMode
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val use24HourClock: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val clockAnimation: Boolean = true,
    val eventOrder: List<EventKey> = EventKey.entries,
    val themeContrast: ThemeContrast = ThemeContrast.Normal,
    val themeColor: String? = null
)


class SettingsRepository(
    private val storage: Settings = Settings()
) {
    private val updateMutex = Mutex()
    private val _settings =
        MutableStateFlow(AppSettings())

    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    suspend fun initialize() {
        updateMutex.withLock {
            _settings.value = loadSettings()
        }
    }

    suspend fun updateTheme(mode: ThemeMode) {
        update { current -> current.copy(themeMode = mode) }
    }

    suspend fun set24HourClock(enabled: Boolean) {
        update { current -> current.copy(use24HourClock = enabled) }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        update { current -> current.copy(notificationsEnabled = enabled) }
    }

    suspend fun setThemeColor(color: String?) {
        update { current -> current.copy(themeColor = color) }
    }

    suspend fun setThemeContrast(contrast: ThemeContrast) {
        update { current -> current.copy(themeContrast = contrast) }
    }

    suspend fun setClockAnimation(enabled: Boolean) {
        update { current -> current.copy(clockAnimation = enabled) }
    }

    suspend fun setEventOrder(order: List<EventKey>) {
        update { current -> current.copy(eventOrder = order) }
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
            ),
            themeColor = storage.getStringOrNull(SettingsKeys.ThemeColor),
            themeContrast = storage.getStringOrNull(SettingsKeys.ThemeContrast)
                ?.let(ThemeContrast::valueOf)
                ?: ThemeContrast.Normal,
            
            eventOrder = storage.getString(
                SettingsKeys.EventOrder,
                AppSettings().eventOrder.joinToString(
                    "|"
                )
            )
                .split("|")
                .map(EventKey::valueOf)
                .let { ordered ->
                    // this is bcz if new keys are introduced, it will not be included in the list
                    // so just append them at the end here
                    ordered + EventKey.entries.filterNot { it in ordered }
                }
        )
    }

    private fun saveChangedSettings(
        current: AppSettings,
        next: AppSettings
    ) {
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
        if (current.eventOrder != next.eventOrder) {
            storage.putString(SettingsKeys.EventOrder, next.eventOrder.joinToString("|"))
        }
        if (current.themeColor != next.themeColor) {
            if (next.themeColor == null)
            // if it was set to null, then delete it
                storage.remove(SettingsKeys.ThemeColor)
            else
                storage.putString(SettingsKeys.ThemeColor, next.themeColor)
        }
        if (current.themeContrast != next.themeContrast) {
            storage.putString(SettingsKeys.ThemeContrast, next.themeContrast.name)
        }
    }

    private fun parseThemeMode(value: String): ThemeMode {
        return ThemeMode.entries.firstOrNull { it.name == value }
            ?: ThemeMode.SYSTEM
    }
}

private object SettingsKeys {
    const val ThemeMode = "theme_mode"
    const val Use24HourClock = "use_24_hour_clock"
    const val NotificationsEnabled = "notifications_enabled"
    const val ClockAnimation = "clock_animation"
    const val EventOrder = "event_order"
    const val ThemeColor = "theme_color"
    const val ThemeContrast = "theme_contrast"
}

