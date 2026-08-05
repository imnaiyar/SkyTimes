package com.imnaiyar.skytimes.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.di.LocalTutorialManager
import com.imnaiyar.skytimes.home.HomeScreens
import com.imnaiyar.skytimes.reminders.ui.ReminderFlowController
import com.imnaiyar.skytimes.theme.ThemeMode
import com.imnaiyar.skytimes.ui.Card
import com.imnaiyar.skytimes.ui.Grid
import com.imnaiyar.skytimes.ui.SettingsItem
import com.imnaiyar.skytimes.ui.Switch
import org.jetbrains.compose.resources.painterResource
import skytimes.core.generated.resources.Res
import skytimes.core.generated.resources.chevron_right
import skytimes.core.generated.resources.contrast_circle
import skytimes.core.generated.resources.dark_mode
import skytimes.core.generated.resources.light_mode
import skytimes.core.generated.resources.open_in_browser


@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    reminderFlow: ReminderFlowController,
    modifier: Modifier,
    fabPad: PaddingValues,
    onOpenThemeSettings: () -> Unit
) {
    val settings by settingsViewModel.settings.collectAsState()
    val tutorialManager = LocalTutorialManager.current
    val uriHandler = LocalUriHandler.current

    val haptic = LocalHapticFeedback.current

    val triggerSwitch = { isChecked: Boolean, action: (value: Boolean) -> Unit ->
        haptic.performHapticFeedback(
            if (isChecked) HapticFeedbackType.ToggleOff
            else HapticFeedbackType.ToggleOn
        )
        action(!isChecked);
    }



    Box(modifier = modifier.fillMaxSize()) {
        Grid(
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
                                    settingsViewModel::setClockAnimation
                                )
                            }
                        )
                        HorizontalDivider()
                        SwitchItem(
                            "Use 24 hour clock",
                            checked = settings.use24HourClock,
                            onClick = {
                                triggerSwitch(
                                    settings.use24HourClock,
                                    settingsViewModel::set24HourClock
                                )
                            }
                        )
                        HorizontalDivider()
                        SwitchItem(
                            "Notifications",
                            checked = settings.notificationsEnabled,
                            onClick = {
                                triggerSwitch(
                                    settings.notificationsEnabled,
                                    reminderFlow::setNotificationsEnabled
                                )
                            }
                        )
                        HorizontalDivider()

                        SettingsItem(
                            "Default Page",
                            "Choose the default page to open when the app is launched"
                        ) {
                            FlowRow(
                                modifier = Modifier.padding(5.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HomeScreens.entries.forEach { screen ->
                                    FilterChip(
                                        label = {
                                            Text(
                                                screen.title,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        selected = settings.homeScreen == screen,
                                        onClick = { settingsViewModel.setHomeScreen(screen) },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(screen.icon),
                                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Appearances
            item {
                SettingsSection {
                    SettingsHeader("Customization")
                    SettingsCard {
                        SettingsItem("Appearance") {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilterChip(
                                    label = { Text("Light") },
                                    selected = settings.themeMode == ThemeMode.LIGHT,
                                    onClick = { settingsViewModel.updateTheme(ThemeMode.LIGHT) },
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
                                    onClick = { settingsViewModel.updateTheme(ThemeMode.DARK) },
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
                                    onClick = { settingsViewModel.updateTheme(ThemeMode.SYSTEM) },
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
                        HorizontalDivider()
                        SettingsItem(
                            "Theme",
                            "Configure app's theme",
                            {
                                Icon(
                                    painterResource(Res.drawable.chevron_right),
                                    contentDescription = "Chevron"
                                )
                            },
                            onClick = onOpenThemeSettings
                        )
                        HorizontalDivider()

                        SettingsItem(
                            title = "Replay tutorial",
                            subtitle = "Start the guided tour again from the beginning.",
                            action = {
                                TextButton(onClick = tutorialManager::reset) {
                                    Text("Replay")
                                }
                            },
                            onClick = tutorialManager::reset
                        )
                    }
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

        reminderFlow.RenderDialogs()
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
