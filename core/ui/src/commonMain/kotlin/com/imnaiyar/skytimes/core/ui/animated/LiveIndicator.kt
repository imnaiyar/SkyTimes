package com.imnaiyar.skytimes.core.ui.animated

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LiveIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4CAF50),
    size: Dp = 6.dp,
) {
    Box(
        modifier
            .pulse()
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}