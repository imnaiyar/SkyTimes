package com.imnaiyar.skytimes.feature.settings

import com.imnaiyar.skytimes.core.common.StartupTask
import com.imnaiyar.skytimes.core.domain.EventKey
import com.imnaiyar.skytimes.core.domain.EventCategory
import com.imnaiyar.skytimes.core.navigation.AppTab
import com.imnaiyar.skytimes.core.onboarding.TutorialProgressRepository
import com.imnaiyar.skytimes.core.ui.theme.DefaultThemeColor
import com.imnaiyar.skytimes.core.ui.theme.ThemeMode
import com.materialkolor.Contrast
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val use24HourClock: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val clockAnimation: Boolean = true,
    val categoryOrder: List<EventCategory> = EventCategory.entries,
    val themeContrast: Contrast = Contrast.Default,
    val pinnedEvents: List<EventKey> = emptyList(),
    val themeColor: Int = DefaultThemeColor.toInt(),
    val homeScreen: AppTab = AppTab.SkyTimes,
    val completedTutorialStepKeys: Set<String> = emptySet()
)


class SettingsRepository(
    private val storage: Settings = Settings()
) : StartupTask, TutorialProgressRepository {
    override val name = "Settings"
    override val critical = true
    private val updateMutex = Mutex()
    private val _settings =
        MutableStateFlow(AppSettings())

    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    override suspend fun initialize() {
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

    suspend fun setTheme(color: Int, contrast: Contrast) {
        update { current -> current.copy(themeColor = color, themeContrast = contrast) }
    }

    suspend fun setClockAnimation(enabled: Boolean) {
        update { current -> current.copy(clockAnimation = enabled) }
    }

    suspend fun setPinnedEvents(events: List<EventKey>) {
        update { current -> current.copy(pinnedEvents = events) }
    }

    suspend fun setCategoryOrder(order: List<EventCategory>) {
        update { current -> current.copy(categoryOrder = order) }
    }

    suspend fun setHomeScreen(screen: AppTab) {
        update { current -> current.copy(homeScreen = screen) }
    }

    override suspend fun readCompletedStepKeys(): Set<String> =
        settings.value.completedTutorialStepKeys

    override suspend fun saveCompletedStepKeys(keys: Set<String>) {
        update { current -> current.copy(completedTutorialStepKeys = keys) }
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
        val defaults = AppSettings()
        println("Pinned: ${storage.getStringOrNull(SettingsKeys.PinnedEvents)}")
        return AppSettings(
            themeMode = storage.getStringOrNull(SettingsKeys.ThemeMode)
                ?.let(::parseThemeMode)
                ?: defaults.themeMode,
            use24HourClock = storage.getBoolean(
                SettingsKeys.Use24HourClock,
                defaults.use24HourClock
            ),
            notificationsEnabled = storage.getBoolean(
                SettingsKeys.NotificationsEnabled,
                defaults.notificationsEnabled
            ),
            clockAnimation = storage.getBoolean(
                SettingsKeys.ClockAnimation,
                defaults.clockAnimation
            ),
            themeColor = storage.getInt(
                SettingsKeys.ThemeColor,
                defaults.themeColor
            ),
            themeContrast = storage.getStringOrNull(SettingsKeys.ThemeContrast)
                ?.let(Contrast::valueOf)
                ?: Contrast.Default,

            pinnedEvents = storage.getStringOrNull(SettingsKeys.PinnedEvents)
                // in case it returns empty string, valueOf will throw errors
                ?.let { it.ifEmpty { null } }
                ?.split("|")
                ?.map(EventKey::valueOf)
                ?: emptyList(),

            categoryOrder = storage.getString(
                SettingsKeys.CategoryOrder,
                defaults.categoryOrder.joinToString("|") { it.name }
            )
                .split("|")
                .mapNotNull { value -> EventCategory.entries.firstOrNull { it.name == value } }
                .let { ordered ->
                    ordered + EventCategory.entries.filterNot { it in ordered }
                },

            homeScreen = storage.getStringOrNull(SettingsKeys.HomeScreen)
                ?.let(AppTab::valueOf)
                ?: defaults.homeScreen,
            completedTutorialStepKeys = storage.getStringOrNull(SettingsKeys.TutorialCompletedSteps)
                ?.takeIf { it.isNotEmpty() }
                ?.split("|")
                ?.toSet()
                ?: emptySet(),
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
        if (current.categoryOrder != next.categoryOrder) {
            storage.putString(SettingsKeys.CategoryOrder, next.categoryOrder.joinToString("|") { it.name })
        }
        if (current.themeColor != next.themeColor) {
            storage.putInt(SettingsKeys.ThemeColor, next.themeColor)
        }
        if (current.themeContrast != next.themeContrast) {
            storage.putString(SettingsKeys.ThemeContrast, next.themeContrast.name)
        }
        if (current.pinnedEvents != next.pinnedEvents) {
            if (next.pinnedEvents.isEmpty())
                storage.remove(SettingsKeys.PinnedEvents)
            else
                storage.putString(SettingsKeys.PinnedEvents, next.pinnedEvents.joinToString("|"))
        }

        if (current.homeScreen != next.homeScreen) {
            storage.putString(SettingsKeys.HomeScreen, next.homeScreen.name)
        }
        if (current.completedTutorialStepKeys != next.completedTutorialStepKeys) {
            if (next.completedTutorialStepKeys.isEmpty()) {
                storage.remove(SettingsKeys.TutorialCompletedSteps)
            } else {
                storage.putString(
                    SettingsKeys.TutorialCompletedSteps,
                    next.completedTutorialStepKeys.joinToString("|")
                )
            }
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
    const val CategoryOrder = "category_order"
    const val PinnedEvents = "pinned_events"
    const val ThemeColor = "theme_color"
    const val ThemeContrast = "theme_contrast"
    const val HomeScreen = "home_screen"
    const val TutorialCompletedSteps = "tutorial_completed_steps"
}
