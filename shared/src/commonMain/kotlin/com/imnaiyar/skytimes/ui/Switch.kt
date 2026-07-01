package com.imnaiyar.skytimes.ui

import androidx.compose.material3.Switch as BaseSwitch
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun Switch(onChange: (Boolean) -> Unit, checked: Boolean, enabled: Boolean = true) {
    val haptic = LocalHapticFeedback.current

    val switchCallback = { isChecked: Boolean ->
        haptic.performHapticFeedback(
            if (isChecked) HapticFeedbackType.ToggleOn
            else HapticFeedbackType.ToggleOff
        )
        onChange(isChecked)
    }

    BaseSwitch(
        checked = checked,
        onCheckedChange = switchCallback,
        enabled = enabled
    )
}