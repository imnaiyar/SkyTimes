package com.imnaiyar.skytimes.core.common

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope

/** Long-lived application scope, provided by the app root. */
val LocalApplicationScope = staticCompositionLocalOf<CoroutineScope> {
    error("No application scope provided")
}


/**
 * Global Snackbar host state to be reused
 * Pass it to SnackBarHost or scaffold's state before using this
 */
val LocalSnackBarState = staticCompositionLocalOf<SnackbarHostState> {
    error("No snack bar state provided")
}