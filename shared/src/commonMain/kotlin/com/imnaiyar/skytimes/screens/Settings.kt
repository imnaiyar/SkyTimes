package com.imnaiyar.skytimes.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.NavController
import com.imnaiyar.skytimes.nav.SplashRoute
import com.imnaiyar.skytimes.theme.LocalThemeState
import com.imnaiyar.skytimes.theme.ThemeMode
import com.imnaiyar.skytimes.ui.SettingsAction
import com.imnaiyar.skytimes.ui.SettingsItem
import org.jetbrains.compose.resources.painterResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.contrast_circle
import skytimes.shared.generated.resources.dark_mode
import skytimes.shared.generated.resources.light_mode

@Composable
fun SettingsScreen(
    modifier: Modifier
) {
    var clockAnimation by remember { mutableStateOf(true) }

    val navigator = NavController.current

    val themeProvider = LocalThemeState.current

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            SettingsSection {
                SettingsHeader("Basic")
                SettingsCard {
                    SettingsItem(
                        "Clock Animation",
                        "Enable or disable clock digit's animation",
                        action = SettingsAction.SwitchAction(
                            checked = clockAnimation,
                            onCheckedChange = { clockAnimation = it }
                        )
                    )
                    HorizontalDivider()
                    SettingsItem(
                        "Preferences",
                        action = SettingsAction.NavigateAction(onClick = { navigator.navigate(SplashRoute) })
                    )
                    HorizontalDivider()
                    SettingsItem(
                        "Use 24 hour clock",
                        "Enable or disable 24 hour clock format",
                        action = SettingsAction.SwitchAction(
                            false,
                            onCheckedChange = {}
                        )
                    )
                }
            }
        }
        item {
            SettingsSection {
                SettingsHeader("Appearance")
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        label = { Text("Light") },
                        selected = themeProvider.mode == ThemeMode.LIGHT,
                        onClick = { themeProvider.setMode(ThemeMode.LIGHT) },
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
                        selected = themeProvider.mode == ThemeMode.DARK,
                        onClick = { themeProvider.setMode(ThemeMode.DARK) },
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
                        selected = themeProvider.mode == ThemeMode.SYSTEM,
                        onClick = { themeProvider.setMode(ThemeMode.SYSTEM) },
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
    }
}


@Composable
fun SettingsSection(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(all = 10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        content()
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(all = 5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
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

