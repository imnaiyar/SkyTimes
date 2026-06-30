package com.imnaiyar.skytimes.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.imnaiyar.skytimes.utils.ClockFormat
import com.imnaiyar.skytimes.utils.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

private val timeUtils = TimeUtils()

@Composable
fun ClockDisplay(
    modifier: Modifier = Modifier,
    gameZone: Boolean = false,
    clockFormat: ClockFormat = ClockFormat.HOUR_12,
    size: TextStyle = MaterialTheme.typography.titleMedium,
    withAnimation: Boolean = true
) {
   var now by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            val current = Clock.System.now()
            now = current

            delay(timeMillis = 1000 - (current.toEpochMilliseconds() % 1000))
        }
    }

    val time = timeUtils.toZone(now, timeZone = if (gameZone) "America/Los_Angeles" else null)

        AnimatedTimer(
        time = timeUtils.formatTime(time, clockFormat),
        size,
        modifier,
        withAnimation
      )

}