package com.imnaiyar.skytimes.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val appInitializer: AppInitializer
) : ViewModel() {
    private val _state = MutableStateFlow<AppState>(AppState.Loading)
    val state: StateFlow<AppState> = _state.asStateFlow()
    private var initializationJob: Job? = null

    init {
        initialize()
    }

    fun retry() {
        initialize()
    }

    private fun initialize() {
        initializationJob?.cancel()
        _state.value = AppState.Loading

        initializationJob = viewModelScope.launch {
            _state.value = try {
                val result = appInitializer.initialize()
                AppState.Ready(warnings = result.warnings)
            } catch (exception: AppInitializationException) {
                AppState.Error(
                    message = "Unable to finish startup task: ${exception.taskName}",
                    cause = exception.cause
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                AppState.Error(
                    message = "Unable to finish app startup",
                    cause = throwable
                )
            }
        }
    }
}
