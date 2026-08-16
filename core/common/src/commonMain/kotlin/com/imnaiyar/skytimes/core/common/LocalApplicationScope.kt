package com.imnaiyar.skytimes.core.common

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope

/** Long-lived application scope, provided by the app root. */
val LocalApplicationScope = staticCompositionLocalOf<CoroutineScope> {
    error("No application scope provided")
}
