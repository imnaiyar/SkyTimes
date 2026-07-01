package com.imnaiyar.skytimes.settings

import com.imnaiyar.skytimes.constants.EventKey

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val use24HourClock: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val clockAnimation: Boolean = true,
    val eventOrder: List<EventKey> = EventKey.entries
)

