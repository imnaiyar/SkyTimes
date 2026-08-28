package com.imnaiyar.skytimes.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnaiyar.skytimes.core.domain.EventKey
import com.imnaiyar.skytimes.core.domain.EventCategory
import com.imnaiyar.skytimes.core.navigation.AppTab
import com.imnaiyar.skytimes.core.ui.theme.ThemeMode
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

    fun setTheme(color: Int, contrast: Contrast) {
        viewModelScope.launch {
            repository.setTheme(color, contrast)
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


    fun setHomeScreen(screen: AppTab) {
        viewModelScope.launch {
            repository.setHomeScreen(screen)
        }
    }

    fun setPinnedEvents(events: List<EventKey>) {
        viewModelScope.launch {
            repository.setPinnedEvents(events)
        }
    }

    fun setCategoryOrder(order: List<EventCategory>) {
        viewModelScope.launch {
            repository.setCategoryOrder(order)
        }
    }
}
