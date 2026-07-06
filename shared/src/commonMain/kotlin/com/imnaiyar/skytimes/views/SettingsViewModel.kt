package com.imnaiyar.skytimes.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnaiyar.skytimes.constants.EventKey
import com.imnaiyar.skytimes.repositories.AppSettings
import com.imnaiyar.skytimes.repositories.SettingsRepository
import com.imnaiyar.skytimes.theme.ThemeMode
import com.materialkolor.Contrast
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.settings

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch {
            repository.updateTheme(mode)
        }
    }

    fun set24HourClock(enabled: Boolean) {
        viewModelScope.launch {
            repository.set24HourClock(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
        }
    }

    fun setClockAnimation(enabled: Boolean) {
        viewModelScope.launch {
            repository.setClockAnimation(enabled)
        }
    }

    fun setThemeColor(color: Int) {
        viewModelScope.launch {
            repository.setThemeColor(color)
        }
    }

    fun setThemeContrast(contrast: Contrast) {
        viewModelScope.launch {
            repository.setThemeContrast(contrast)
        }
    }

    fun setEventOrder(order: List<EventKey>) {
        viewModelScope.launch {
            repository.setEventOrder(order)
        }
    }
}