package com.imnaiyar.skytimes.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.ui.SwitchItem

@Composable
fun SettingsScreen(
    modifier: Modifier
) {
    var checked by remember { mutableStateOf(true) }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            SettingsSection {
                SettingsHeader("Basic")
                SettingsCard {
                    SwitchItem("Dark Mode", checked = checked, onCheckedChange = { checked = it })
                    HorizontalDivider()
                    SwitchItem("Dark Mode", checked = checked, onCheckedChange = { checked = it })
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

