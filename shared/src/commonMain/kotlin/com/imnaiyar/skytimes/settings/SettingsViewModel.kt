package com.imnaiyar.skytimes.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnaiyar.skytimes.constants.EventKey
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

    fun setEventOrder(order: List<EventKey>) {
        viewModelScope.launch {
            repository.setEventOrder(order)
        }
    }
}

