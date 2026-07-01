package com.imnaiyar.skytimes.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
 }

data class ThemeState(
    val mode: ThemeMode,
    val setMode: (ThemeMode) -> Unit
)

val LocalThemeState = staticCompositionLocalOf<ThemeState> {
    error("No ThemeState provided")
 }

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    var themeMode by remember{ mutableStateOf(ThemeMode.SYSTEM) }
    val isDark = if (themeMode == ThemeMode.SYSTEM) isSystemInDarkTheme() else themeMode == ThemeMode.DARK
    CompositionLocalProvider(
        LocalThemeState provides ThemeState(mode = themeMode, setMode = { themeMode = it })
    ) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColor else LightColor,
            content = content,
            typography = appTypography()
        )
    }
}
