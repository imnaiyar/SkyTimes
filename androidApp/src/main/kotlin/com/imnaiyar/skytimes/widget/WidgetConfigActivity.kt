package com.imnaiyar.skytimes.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.constants.events

/**
 * Widget configuration screen — shown when the user adds the SkyTimes widget.
 */
class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Default result: cancel widget placement if user backs out
        setResult(RESULT_CANCELED)

        // Extract the widget ID from the intent extras
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                WidgetConfigScreen(
                    appWidgetId = appWidgetId,
                    onSave = { selectedEvents ->
                        saveConfiguration(selectedEvents)
                    },
                    onCancel = { finish() },
                )
            }
        }
    }

    private fun saveConfiguration(selectedEvents: Set<com.imnaiyar.skytimes.constants.EventKey>) {
        // Persist the user's selection for this widget instance
        WidgetPreferences.setSelectedEvents(this, appWidgetId, selectedEvents)

        // Trigger an immediate widget update
        SkyTimesWidget.updateWidget(this, appWidgetId)

        // Signal success — the widget will be placed
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}

// ─── Configuration Screen UI ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(
    appWidgetId: Int,
    onSave: (Set<com.imnaiyar.skytimes.constants.EventKey>) -> Unit,
    onCancel: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Load the current (or default) selection for this widget instance
    val selectedState = remember {
        val saved = WidgetPreferences.getSelectedEvents(context, appWidgetId)
        mutableStateMapOf<com.imnaiyar.skytimes.constants.EventKey, Boolean>().apply {
            com.imnaiyar.skytimes.constants.EventKey.entries.forEach { key ->
                this[key] = key in saved
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Widget") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Description
            Text(
                text = "Choose which events to show on this widget:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Event list with toggles
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                item {
                    FlowRow(itemVerticalAlignment = Alignment.CenterVertically) {
                        events.forEach { event ->
                            EventToggleRow(
                                event = event,
                                isChecked = selectedState[event.key] == true,
                                onToggle = { checked ->
                                    selectedState[event.key] = checked
                                },
                            )
                        }
                    }
                }
            }

            // Bottom action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val selected = selectedState
                            .filter { it.value }
                            .keys
                            .toSet()
                        onSave(selected)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedState.any { it.value },
                ) {
                    Text("Save")
                }
            }
        }
    }
}

/**
 * A single row in the configuration list: event name + checkbox toggle.
 */
@Composable
private fun EventToggleRow(
    event: EventData,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    FilterChip(
        selected = isChecked,
        onClick = { onToggle(!isChecked) },
        modifier = Modifier
            .padding(4.dp),
        label = {
            Column {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.bodyLarge,
                )

                val subtitle = buildString {
                    if (event.interval != null) {
                        append("Every ${event.interval}min")
                    }
                    if (event.occursOn != null) {
                        if (isNotEmpty()) append(" · ")
                        val days = event.occursOn!!.weekDays?.mapNotNull { dayNum ->
                            when (dayNum) {
                                1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"
                                5 -> "Fri"; 6 -> "Sat"; 7 -> "Sun"
                                else -> null
                            }
                        }?.joinToString(",")
                        if (!days.isNullOrEmpty()) append(days)
                        event.occursOn!!.dayOfTheMonth?.let { append("Day $it") }
                    }
                    if (isEmpty()) append("—")
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    )
}
