package com.imnaiyar.skytimes.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imnaiyar.skytimes.repositories.QuestLoadResult
import com.imnaiyar.skytimes.repositories.QuestRepository
import com.imnaiyar.skytimes.utils.QuestResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuestsViewModel(
    private val repository: QuestRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<QuestsUiState>(QuestsUiState.Loading)
    val state: StateFlow<QuestsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun refresh() {
        val current = _state.value
        if (current !is QuestsUiState.Content || current.isRefreshing) return

        viewModelScope.launch {
            _state.update { state ->
                if (state is QuestsUiState.Content) {
                    state.copy(isRefreshing = true, errorMessage = null)
                } else {
                    state
                }
            }

            when (val result = repository.refreshQuests()) {
                is QuestLoadResult.Success -> {
                    _state.value = QuestsUiState.Content(result.response)
                }

                is QuestLoadResult.Failure -> {
                    val fallback = result.cached ?: current.response
                    _state.value = QuestsUiState.Content(
                        response = fallback,
                        isRefreshing = false,
                        errorMessage = result.message,
                    )
                }

                QuestLoadResult.RefreshSkipped -> {
                    _state.update { state ->
                        if (state is QuestsUiState.Content) {
                            state.copy(isRefreshing = false)
                        } else {
                            state
                        }
                    }
                }
            }
        }
    }

    fun retry() {
        if (_state.value is QuestsUiState.Loading) return
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = QuestsUiState.Loading
            when (val result = repository.loadQuests()) {
                is QuestLoadResult.Success -> {
                    _state.value = QuestsUiState.Content(result.response)
                }

                is QuestLoadResult.Failure -> {
                    _state.value = QuestsUiState.Error(result.message)
                }

                QuestLoadResult.RefreshSkipped -> Unit
            }
        }
    }
}

sealed interface QuestsUiState {
    data object Loading : QuestsUiState

    data class Content(
        val response: QuestResponse,
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null,
    ) : QuestsUiState

    data class Error(
        val message: String,
    ) : QuestsUiState
}
