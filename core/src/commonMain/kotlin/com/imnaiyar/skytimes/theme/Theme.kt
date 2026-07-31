package com.imnaiyar.skytimes.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.imnaiyar.skytimes.repositories.SettingsRepository
import com.materialkolor.Contrast
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val DefaultThemeColor = 0xFF769CDF

data class ThemeState(
    val color: Int,
    val contrast: Contrast
)

class ThemeController(
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope
) {

    /**
     * Temporary preview while the user is editing.
     * Null = use the saved theme.
     */
    private val preview = MutableStateFlow<ThemeState?>(null)

    /**
     * Theme observed by the entire app.
     *
     * If a preview exists, use it.
     * Otherwise use the saved settings.
     */
    val theme: StateFlow<ThemeState> = combine(
        settingsRepository.settings,
        preview
    ) { settings, preview ->
        preview ?: ThemeState(
            color = settings.themeColor,
            contrast = settings.themeContrast
        )
    }.stateIn(
        scope = scope,
        SharingStarted.Eagerly,
        ThemeState(
            color = settingsRepository.settings.value.themeColor,
            contrast = settingsRepository.settings.value.themeContrast
        )
    )

    fun preview(themeColor: Int, contrast: Contrast) {
        preview.value = ThemeState(themeColor, contrast)
    }

    fun discardPreview() {
        preview.value = null
    }

    fun commit() {
        val previewTheme = preview.value ?: return
        scope.launch {
            settingsRepository.setTheme(
                previewTheme.color,
                previewTheme.contrast
            )
            preview.value = null
        }
    }

    val hasUnsavedChanges: StateFlow<Boolean> =
        combine(
            settingsRepository.settings,
            preview
        ) { saved, preview ->

            preview != null &&
                    (
                            preview.color != saved.themeColor ||
                                    preview.contrast != saved.themeContrast
                            )

        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            false
        )
}

@Composable
fun AppTheme(
    themeMode: ThemeMode,
    contrast: Contrast,
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