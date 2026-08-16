package com.imnaiyar.skytimes.startup

import com.imnaiyar.skytimes.core.common.AppStartupWarning

sealed interface AppState {
    data object Loading : AppState

    data class Ready(
        val warnings: List<AppStartupWarning> = emptyList()
    ) : AppState

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : AppState
}
