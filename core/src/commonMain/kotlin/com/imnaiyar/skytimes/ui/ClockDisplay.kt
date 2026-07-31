package com.imnaiyar.skytimes.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.imnaiyar.skytimes.constants.GameTimeZone
import com.imnaiyar.skytimes.utils.rememberTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

@Composable
fun ClockDisplay(
    modifier: Modifier = Modifier,
    gameZone: Boolean = false,
    size: TextStyle = MaterialTheme.typography.titleMedium,
) {
    var now by remember { mutableStateOf(Clock.System.now()) }
    val timeUtils = rememberTimeFormatter()
    val time =
        timeUtils.toZone(now, if (gameZone) GameTimeZone else TimeZone.currentSystemDefault())

    LaunchedEffect(Unit) {
        while (true) {
            val current = Clock.System.now()
            now = current

            delay(timeMillis = 1000 - (current.toEpochMilliseconds() % 1000))
        }
    }

    AnimatedTimer(
        time = timeUtils.format(time),
        size,
        modifier
    )

}
