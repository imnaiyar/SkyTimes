package com.imnaiyar.skytimes.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProvider
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle

/**
 * Widget styling constants.
 */
object WidgetTheme {

    // ── Colors

    /** Accent color for active event indicators and the refresh button. */
    val accent = ColorProvider(
        day = Color(0xFF4CAF50),
        night = Color(0xFF4CAF50),
    )

    /** Subtle background tint for rows showing an active event. */
    val activeBackground = ColorProvider(
        day = Color(0x1A4CAF50), // 10% opacity
        night = Color(0x334CAF50), // 20% opacity for better visibility on dark backgrounds
    )

    // ── Typography
    val eventNameStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = GlanceTheme.colors.onSecondaryContainer,
        )

    val eventNameActiveStyle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = accent,
    )

    val countdownStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = GlanceTheme.colors.secondary,
        )

    val countdownActiveStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = accent,
    )

    val emptyStateStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = GlanceTheme.colors.secondary,
        )

    // ── Spacing & Sizing ────────────────────────────────────

    /** Horizontal padding for the widget content area. */
    val contentPadding = 8.dp

    /** Corner radius for the active event background. */
    val activeRowCornerRadius = 6.dp
    val smallWidthThreshold = 150.dp
}
