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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
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
    withAnimation: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        time.forEachIndexed { index, c ->
            if (c == ':') {
                Text(
                    ":",
                    style = size
                )
            } else {
                if (c == 'A' || c == 'P') {
                    Text(text = " ")
                }

                AnimatedDigit(
                    digit = c,
                    label = "digit-$index",
                    size,
                    withAnimation
                )
            }
        }
    }
}

@Composable
private fun AnimatedDigit(
    digit: Char,
    label: String,
    size: TextStyle = MaterialTheme.typography.titleMedium,
    withAnimation: Boolean
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
                (slideInVertically { it } + fadeIn())
                    .togetherWith(
                        slideOutVertically { -it } + fadeOut()
                    )
            },
            label = label
        ) { value -> TextDisplay(value) }
        else
            TextDisplay(digit)
    }
}