package com.imnaiyar.skytimes.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.RoundedCorner
import androidx.compose.material3.Card as OGCard

@Composable
fun Card(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCorner,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    elevation: CardElevation = CardDefaults.cardElevation(),
    content: @Composable ColumnScope.() -> Unit
) {
    OGCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(color),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = elevation,
        shape = shape
    ) {
        content()
    }
}