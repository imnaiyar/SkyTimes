package com.imnaiyar.skytimes.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.imnaiyar.skytimes.core.common.TimeFormatter

/** 24-hour vs 12-hour clock preference, provided by the app root. */
val LocalUse24HourClock = staticCompositionLocalOf { true }

/** Whether animated clock digits are enabled, provided by the app root. */
val LocalClockAnimation = staticCompositionLocalOf { true }

/** Builds a [TimeFormatter] that respects the user's clock-format preference. */
@Composable
fun rememberTimeFormatter(): TimeFormatter {
    val use24HourClock = LocalUse24HourClock.current
    return remember(use24HourClock) { TimeFormatter(use24HourClock) }
}
