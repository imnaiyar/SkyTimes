package com.imnaiyar.skytimes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch


@Composable
private fun _ToolTip(
    tooltipState: TooltipState = rememberTooltipState(),
    tooltipPosition: TooltipAnchorPosition = TooltipAnchorPosition.Below,
    showOnClick: Boolean = true,
    tooltip: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            tooltipPosition
        ),
        state = tooltipState,
        tooltip = {
            PlainTooltip(
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary
            ) { tooltip() }
        },
    ) {
        Box(modifier = Modifier.clickable {
            if (showOnClick) scope.launch { tooltipState.show() }
        }) {
            content()
        }
    }
}

@Composable
fun Tooltip(
    text: String,
    tooltipState: TooltipState = rememberTooltipState(),
    tooltipPosition: TooltipAnchorPosition = TooltipAnchorPosition.Below,
    showOnClick: Boolean = true,
    content: @Composable () -> Unit
) {

    _ToolTip(tooltipState, tooltipPosition, showOnClick, { Text(text) }, content)
}

@Composable
fun Tooltip(
    tooltip: @Composable () -> Unit,
    tooltipState: TooltipState = rememberTooltipState(),
    tooltipPosition: TooltipAnchorPosition = TooltipAnchorPosition.Below,
    showOnClick: Boolean = true,
    content: @Composable () -> Unit
) {

    _ToolTip(tooltipState, tooltipPosition, showOnClick, tooltip, content)
}