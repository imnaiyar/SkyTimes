package com.imnaiyar.skytimes.settings

import com.imnaiyar.skytimes.repositories.AppSettings
import com.imnaiyar.skytimes.repositories.SettingsRepository
import com.imnaiyar.skytimes.theme.ThemeMode
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsRepositoryTest {
    @Test
    fun emptyStorageEmitsDefaultSettings() {
        val repository =
            SettingsRepository(TestSettings())

        assertEquals(AppSettings(), repository.settings.value)
    }

    @Test
    fun storedValuesAreLoadedOnInitialization() = runTest {
        val storage = TestSettings().apply {
            putString("theme_mode", ThemeMode.DARK.name)
            putBoolean("use_24_hour_clock", false)
            putBoolean("notifications_enabled", false)
            putBoolean("clock_animation", false)
        }

        val repository =
            SettingsRepository(storage)
        
        assertEquals(AppSettings(), repository.settings.value)
        repository.initialize()

        assertEquals(
            AppSettings(
                themeMode = ThemeMode.DARK,
                use24HourClock = false,
                notificationsEnabled = false,
                clockAnimation = false
            ),
            repository.settings.value
        )
    }

    @Test
    fun invalidStoredThemeFallsBackToSystem() = runTest {
        val storage = TestSettings().apply {
            putString("theme_mode", "invalid")
        }

        val repository =
            SettingsRepository(storage)
        repository.initialize()

        assertEquals(ThemeMode.SYSTEM, repository.settings.value.themeMode)
    }

    @Test
    fun updateThemePersistsAndEmits() = runTest {
        val storage = TestSettings()
        val repository =
            SettingsRepository(storage)

        repository.updateTheme(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, repository.settings.value.themeMode)
        assertEquals(ThemeMode.LIGHT.name, storage.getStringOrNull("theme_mode"))
    }

    @Test
    fun setting24HourClockPersistsAndEmits() = runTest {
        val storage = TestSettings()
        val repository =
            SettingsRepository(storage)

        repository.set24HourClock(false)

        assertFalse(repository.settings.value.use24HourClock)
        assertFalse(storage.getBoolean("use_24_hour_clock", true))
    }

    @Test
    fun settingNotificationsPersistsAndEmits() = runTest {
        val storage = TestSettings().apply {
            putBoolean("notifications_enabled", false)
        }
        val repository =
            SettingsRepository(storage)
        repository.initialize()

        repository.setNotificationsEnabled(true)

        assertTrue(repository.settings.value.notificationsEnabled)
        assertTrue(storage.getBoolean("notifications_enabled", false))
    }

    @Test
    fun settingClockAnimationPersistsAndEmits() = runTest {
        val storage = TestSettings()
        val repository =
            SettingsRepository(storage)

        repository.setClockAnimation(false)

        assertFalse(repository.settings.value.clockAnimation)
        assertFalse(storage.getBoolean("clock_animation", true))
    }

    @Test
    fun updatesPreserveUnrelatedSettings() = runTest {
        val repository =
            SettingsRepository(TestSettings())

        repository.updateTheme(ThemeMode.DARK)
        repository.set24HourClock(false)

        assertEquals(
            AppSettings(
                themeMode = ThemeMode.DARK,
                use24HourClock = false,
                notificationsEnabled = true,
                clockAnimation = true
            ),
            repository.settings.value
        )
    }
}

private class TestSettings : Settings {
    private val values = mutableMapOf<String, Any>()

    override val keys: Set<String>
        get() = values.keys

    override val size: Int
        get() = values.size

    override fun clear() {
        values.clear()
    }

    override fun remove(key: String) {
        values.remove(key)
    }

    override fun hasKey(key: String): Boolean {
        return values.containsKey(key)
    }

    override fun putInt(key: String, value: Int) {
        values[key] = value
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getIntOrNull(key) ?: defaultValue
    }

    override fun getIntOrNull(key: String): Int? {
        return values[key] as? Int
    }

    override fun putLong(key: String, value: Long) {
        values[key] = value
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getLongOrNull(key) ?: defaultValue
    }

    override fun getLongOrNull(key: String): Long? {
        return values[key] as? Long
    }

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun getString(key: String, defaultValue: String): String {
        return getStringOrNull(key) ?: defaultValue
    }

    override fun getStringOrNull(key: String): String? {
        return values[key] as? String
    }

    override fun putFloat(key: String, value: Float) {
        values[key] = value
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return getFloatOrNull(key) ?: defaultValue
    }

    override fun getFloatOrNull(key: String): Float? {
        return values[key] as? Float
    }

    override fun putDouble(key: String, value: Double) {
        values[key] = value
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getDoubleOrNull(key) ?: defaultValue
    }

    override fun getDoubleOrNull(key: String): Double? {
        return values[key] as? Double
    }

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getBooleanOrNull(key) ?: defaultValue
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return values[key] as? Boolean
    }
}

