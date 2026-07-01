package com.imnaiyar.skytimes.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.LocalViewModel
import com.imnaiyar.skytimes.utils.rememberDigitWidth


/**
 * Formats a given utf time string in animated manner which looks smooths
 * each digit changes itself by going up from down
 */
@Composable
fun AnimatedTimer(
    time: String,
    size: TextStyle = MaterialTheme.typography.titleMedium,
    modifier: Modifier = Modifier,
    direction: ClockDirection = ClockDirection.UP,
    withAnimation: Boolean = true
) {
    val settings by LocalViewModel.current.settings.collectAsState()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        time.forEachIndexed { index, c ->
            if (c == ':' || c == ' ') {
                Text(
                    text = c.toString(),
                    style = size
                )
            } else {
                AnimatedDigit(
                    digit = c,
                    label = "digit-$index",
                    size,
                    // if withAnimation is explicitly passed false, then it shouldn't use animation at all
                    // otherwise depend on settings
                    withAnimation = if (withAnimation) settings.clockAnimation else false,
                    direction
                )
            }
        }
    }
}

enum class ClockDirection {
    UP, DOWN
}
@Composable
private fun AnimatedDigit(
    digit: Char,
    label: String,
    size: TextStyle = MaterialTheme.typography.titleMedium,
    withAnimation: Boolean,
    direction: ClockDirection = ClockDirection.UP
) {
    val width = rememberDigitWidth(size)

    @Composable
    fun TextDisplay (text: Char) {
        Text(
        text = text.toString(),
        style = size,
        color = if (text in charArrayOf('A', 'P', 'M'))
            MaterialTheme.colorScheme.tertiary
        else
            MaterialTheme.colorScheme.onSurface
       )
    }
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center
    ) {
       if (withAnimation) AnimatedContent(
            targetState = digit,
           transitionSpec = {
               if (direction == ClockDirection.UP) {
                   (slideInVertically { it } + fadeIn())
                       .togetherWith(
                           slideOutVertically { -it } + fadeOut()
                       )
               } else {
                   (slideInVertically { -it } + fadeIn())
                       .togetherWith(
                           slideOutVertically { it } + fadeOut()
                       )
               }
           },
            label = label
        ) { value -> TextDisplay(value) }
        else
            TextDisplay(digit)
    }
}