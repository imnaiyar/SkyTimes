package com.imnaiyar.skytimes.home.skytimes

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.pin

/** Long-press context menu for a single event row: pin/unpin and set a reminder. */
@Composable
internal fun ContextMenu(
    isOpen: Boolean,
    isPinned: Boolean,
    onDismiss: () -> Unit,
    onPinClick: () -> Unit,
    onReminderClick: () -> Unit,
) {

    val pinRotation by animateFloatAsState(
        targetValue = if (isPinned) 30f else 0f,
        label = "PinRotation",
    )

    DropdownMenu(
        modifier = Modifier.width(180.dp),
        expanded = isOpen,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    if (isPinned) "Unpin" else "Pin",
                    modifier = Modifier.animateContentSize()
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.pin),
                    contentDescription = null,
                    modifier = Modifier.rotate(pinRotation),
                )
            },
            onClick = onPinClick,
            colors = if (isPinned) {
                MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.primary,
                    trailingIconColor = MaterialTheme.colorScheme.primary,
                )
            } else {
                MenuDefaults.itemColors()
            },
        )
        HorizontalDivider(modifier = Modifier.padding(5.dp))
        DropdownMenuItem(text = { Text("Reminder") }, onClick = onReminderClick)
    }
}