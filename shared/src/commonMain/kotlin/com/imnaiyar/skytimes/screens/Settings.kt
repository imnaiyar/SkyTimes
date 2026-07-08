package com.imnaiyar.skytimes.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.NavController
import com.imnaiyar.skytimes.di.LocalSettingsViewModel
import com.imnaiyar.skytimes.nav.ThemeSettingsRoute
import com.imnaiyar.skytimes.theme.ThemeMode
import com.imnaiyar.skytimes.ui.Card
import com.imnaiyar.skytimes.ui.SettingsItem
import com.imnaiyar.skytimes.ui.Switch
import org.jetbrains.compose.resources.painterResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.chevron_right
import skytimes.shared.generated.resources.contrast_circle
import skytimes.shared.generated.resources.dark_mode
import skytimes.shared.generated.resources.light_mode
import skytimes.shared.generated.resources.open_in_browser


@Composable
fun SettingsScreen(
    modifier: Modifier,
    fabPad: PaddingValues
) {
    val viewModel = LocalSettingsViewModel.current
    val settings by viewModel.settings.collectAsState()
    val uriHandler = LocalUriHandler.current

    val navController = NavController.current
    val haptic = LocalHapticFeedback.current

    val triggerSwitch = { isChecked: Boolean, action: (value: Boolean) -> Unit ->
        haptic.performHapticFeedback(
            if (isChecked) HapticFeedbackType.ToggleOff
            else HapticFeedbackType.ToggleOn
        )
        action(!isChecked);
    }



    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = fabPad
    ) {
        item {
            SettingsSection {
                SettingsHeader("Preferences")
                SettingsCard {
                    SwitchItem(
                        "Clock Animation",
                        "Enable or disable clock digit's animation",
                        checked = settings.clockAnimation,
                        onClick = {
                            triggerSwitch(
                                settings.clockAnimation,
                                viewModel::setClockAnimation
                            )
                        }
                    )
                    HorizontalDivider()
                    SwitchItem(
                        "Use 24 hour clock",
                        "Enable or disable 24 hour clock format",
                        checked = settings.use24HourClock,
                        onClick = {
                            triggerSwitch(
                                settings.use24HourClock,
                                viewModel::set24HourClock
                            )
                        }
                    )
                    HorizontalDivider()
                    SwitchItem(
                        "Notifications",
                        "Enable or disable app notifications",
                        checked = settings.notificationsEnabled,
                        onClick = {
                            triggerSwitch(
                                settings.notificationsEnabled,
                                viewModel::setNotificationsEnabled
                            )
                        }
                    )
                }
            }
        }

        // Appearences
        item {
            SettingsSection {
                SettingsHeader("Appearance")
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        label = { Text("Light") },
                        selected = settings.themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.updateTheme(ThemeMode.LIGHT) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.light_mode),
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                                contentDescription = null
                            )
                        }
                    )
                    FilterChip(
                        label = { Text("Dark") },
                        selected = settings.themeMode == ThemeMode.DARK,
                        onClick = { viewModel.updateTheme(ThemeMode.DARK) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.dark_mode),
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                                contentDescription = null
                            )
                        }
                    )
                    FilterChip(
                        label = { Text("System") },
                        selected = settings.themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.updateTheme(ThemeMode.SYSTEM) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.contrast_circle),
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }

        // Contrast
        item {
            SettingsCard {
                SettingsItem(
                    "Theme",
                    "Configure app's theme",
                    {
                        Icon(
                            painterResource(Res.drawable.chevron_right),
                            contentDescription = "Chevron"
                        )
                    },
                    onClick = { navController.navigate(ThemeSettingsRoute) }
                )
            }
        }
        // Links
        item {
            SettingsSection {
                SettingsHeader("Links")
                SettingsCard {
                    SettingsItem(
                        "Privacy Policy",
                        action = {
                            Icon(
                                painterResource(Res.drawable.open_in_browser),
                                modifier = Modifier.size(30.dp),
                                contentDescription = null
                            )
                        },
                        onClick = { uriHandler.openUri("https://next.skyhelper.xyz/privacy") }
                    )
                }
            }
        }
    }
}


@Composable
fun SettingsSection(content: @Composable () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card() {
        Column(
            modifier = Modifier.padding(all = 5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsHeader(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall
    )
}


@Composable
fun SwitchItem(title: String, subtitle: String? = null, checked: Boolean, onClick: () -> Unit) {
    SettingsItem(
        title,
        subtitle,
        onClick = onClick,
        action = {
            Switch(checked = checked, onChange = { onClick() })
        }
    )
}
