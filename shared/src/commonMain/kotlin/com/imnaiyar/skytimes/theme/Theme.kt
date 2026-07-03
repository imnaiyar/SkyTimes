package com.imnaiyar.skytimes.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val isDark =
        if (themeMode == ThemeMode.SYSTEM) isSystemInDarkTheme() else themeMode == ThemeMode.DARK
    MaterialTheme(
        colorScheme = if (isDark) DarkColor else LightColor,
        content = content,
        typography = appTypography()
    )
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}