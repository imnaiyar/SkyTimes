package com.imnaiyar.skytimes.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

enum class ThemeContrast {
    Normal,
    Medium,
    High,
}

@Composable
fun AppTheme(
    themeMode: ThemeMode,
    contrast: ThemeContrast,
    themeColor: String? = null,
    content: @Composable () -> Unit
) {
    val isDark =
        if (themeMode == ThemeMode.SYSTEM) isSystemInDarkTheme() else themeMode == ThemeMode.DARK

    val color = when (contrast) {
        ThemeContrast.Normal -> if (isDark) DarkColor else LightColor
        ThemeContrast.Medium -> if (isDark) mediumContrastDarkColorScheme else mediumContrastLightColorScheme
        ThemeContrast.High -> if (isDark) highContrastDarkColorScheme else highContrastLightColorScheme
    }
    
    MaterialTheme(
        colorScheme = color,
        content = content,
        typography = appTypography()
    )
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}