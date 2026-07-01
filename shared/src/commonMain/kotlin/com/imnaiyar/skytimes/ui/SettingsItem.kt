package com.imnaiyar.skytimes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp


sealed interface SettingsAction {
    data class SwitchAction(
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingsAction

    data class NavigateAction(
        val onClick: () -> Unit
    ) : SettingsAction
}

/**
 * A composable that represents a settings item with a title, optional subtitle, and an action.
 *
 * @param title The title of the settings item.
 * @param subtitle An optional subtitle for the settings item.
 * @param action The action associated with the settings item, which can be a switch or a button.
 * @param modifier The modifier to be applied to the settings item.
 */
@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    action: SettingsAction,
    modifier: Modifier = Modifier,
) {

    val haptic = LocalHapticFeedback.current

    val callBack = when (action) {
        is SettingsAction.SwitchAction -> { isChecked: Boolean ->
            haptic.performHapticFeedback(
                if (isChecked) HapticFeedbackType.ToggleOn
                else HapticFeedbackType.ToggleOff
            )
            action.onCheckedChange(isChecked)
        }
        is SettingsAction.NavigateAction -> { _: Boolean ->
            action.onClick()
        }
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        onClick = {
            if (action is SettingsAction.SwitchAction)
                callBack(!action.checked)
            else callBack(true)
        },
    ) {
        Row(
            modifier = Modifier
                .padding(all = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            when (action) {
                is SettingsAction.SwitchAction -> {
                    Switch(
                        checked = action.checked,
                        onCheckedChange = callBack
                    )
                }
                is SettingsAction.NavigateAction -> {
                    Text(
                        text = ">",
                        modifier = Modifier
                            .clickable(onClick = { callBack(true) })
                            .padding(end = 5.dp, top = 5.dp, bottom = 5.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}