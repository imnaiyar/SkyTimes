package com.imnaiyar.skytimes.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.imnaiyar.skytimes.di.LocalSettingsViewModel
import com.imnaiyar.skytimes.utils.TimeUtils
import com.imnaiyar.skytimes.utils.TimeValue
import kotlinx.coroutines.delay
import kotlin.time.Clock

private val timeUtils = TimeUtils()

@Composable
fun ClockDisplay(
    modifier: Modifier = Modifier,
    gameZone: Boolean = false,
    size: TextStyle = MaterialTheme.typography.titleMedium,
) {
    val settings by LocalSettingsViewModel.current.settings.collectAsState()
    var now by remember { mutableStateOf(Clock.System.now()) }
    val time = timeUtils.toZone(now, timeZone = if (gameZone) "America/Los_Angeles" else null)

    LaunchedEffect(Unit) {
        while (true) {
            val current = Clock.System.now()
            now = current

            delay(timeMillis = 1000 - (current.toEpochMilliseconds() % 1000))
        }
    }

    AnimatedTimer(
        time = timeUtils.formatTime(TimeValue.localTime(time), settings.use24HourClock),
        size,
        modifier,
        withAnimation = settings.clockAnimation
    )

}
