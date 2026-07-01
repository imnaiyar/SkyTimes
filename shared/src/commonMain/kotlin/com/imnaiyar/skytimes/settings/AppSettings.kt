package com.imnaiyar.skytimes.settings

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val use24HourClock: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val clockAnimation: Boolean = true
)

