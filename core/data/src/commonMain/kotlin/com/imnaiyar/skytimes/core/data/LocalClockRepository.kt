package com.imnaiyar.skytimes.core.data

import androidx.compose.runtime.staticCompositionLocalOf

/** The shared clock ticker, provided by the app root. */
val LocalClockRepository = staticCompositionLocalOf<ClockRepository> {
    error("No ClockRepository provided")
}
