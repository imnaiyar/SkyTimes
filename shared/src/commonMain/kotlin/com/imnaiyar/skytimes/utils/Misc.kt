package com.imnaiyar.skytimes.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp

@Composable
fun rememberDigitWidth(
    textStyle: TextStyle
): Dp {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val maxWidthPx = remember(textStyle) {
        ('0'..'9').maxOf { digit ->
            textMeasurer.measure(
                text = digit.toString(),
                style = textStyle
            ).size.width
        }
    }

    return with(density) { maxWidthPx.toDp() }
}