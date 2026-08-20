package com.imnaiyar.skytimes.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.Contrast
import com.materialkolor.rememberDynamicColorScheme

const val DefaultThemeColor = 0xFF769CDF

@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    contrast: Contrast = Contrast.Default,
    themeColor: Int?,
    content: @Composable () -> Unit
) {
    val isDark =
        if (themeMode == ThemeMode.SYSTEM) isSystemInDarkTheme() else themeMode == ThemeMode.DARK


    val color = rememberDynamicColorScheme(
        Color(themeColor ?: DefaultThemeColor.toInt()), isDark,
        contrastLevel = contrast.value
    )


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