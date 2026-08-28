package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun EventColumnLayout(
    modifier: Modifier = Modifier,
    eventName: @Composable () -> Unit,
    nextTime: @Composable () -> Unit,
    remaining: @Composable () -> Unit,
    toggles: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1.5f),
            contentAlignment = Alignment.CenterStart,
        ) {
            eventName()
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            nextTime()
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            remaining()
        }

        Box(
            modifier = Modifier.weight(0.8f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            toggles()
        }
    }
}

// TODO: remove if not using in the future, this is just an idea for now
@Composable
private fun EventColumnDivider(color: Color) {
    VerticalDivider(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp),
        color = color,
        thickness = 1.dp,
    )
}
