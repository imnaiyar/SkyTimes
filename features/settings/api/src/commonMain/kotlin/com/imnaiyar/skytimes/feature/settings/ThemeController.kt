package com.imnaiyar.skytimes.feature.settings

import com.materialkolor.Contrast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
