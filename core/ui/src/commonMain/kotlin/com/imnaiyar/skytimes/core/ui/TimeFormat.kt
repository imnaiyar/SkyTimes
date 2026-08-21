package com.imnaiyar.skytimes.core.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import com.imnaiyar.skytimes.core.common.TimeFormatter
import com.imnaiyar.skytimes.core.domain.GameTimeZone
import kotlin.time.Instant

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

/**
 * Display given instant into readable time
 *
 * @param time The time to format
 * @param now
 * @param withTooltip Whether to show the given time in Game's timezone on a tooltip,
 * if true the time will decorated with dotted underline to indicate it's clickable
 * @param useDecoration Whether to strike through the time if it's crossed [now] to indicate it's in the past, or has occurred
 * @param transform Transform the resulting time string,
 * this is useful if appending additional text string like `At` at the start of the text
 */
@Composable
fun TimeDisplay(
    time: Instant,
    now: Instant? = null,
    withTooltip: Boolean = true,
    useDecoration: Boolean = true,
    textColor: Color = LocalContentColor.current,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    transform: (String) -> String = { it }
) {
    val timeUtils = rememberTimeFormatter()
    val decorateText = now != null && useDecoration

    Tooltip(
        "${
            timeUtils.format(
                time,
                GameTimeZone
            )
        } in Los Angeles (Game's Timezone)",
        enabled = withTooltip,
    ) {
        DecoratedText(
            text = transform(timeUtils.format(time)),
            textDecoration =
                if (decorateText && now > time) TextDecoration.LineThrough
                else TextDecoration.None,
            color = if (decorateText && now > time) textColor.copy(0.5f)
            else textColor,
            style = textStyle,
            underlineStyle = if (withTooltip) UnderlineStyle.Dotted else UnderlineStyle.None
        )
    }
}