package com.imnaiyar.skytimes.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.unit.ColorProvider

@Composable
fun Divider(
    modifier: GlanceModifier = GlanceModifier,
    color: ColorProvider = GlanceTheme.colors.outline,
    thickness: Dp = 1.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(thickness)
                .background(color)
        ) {}
    }
}