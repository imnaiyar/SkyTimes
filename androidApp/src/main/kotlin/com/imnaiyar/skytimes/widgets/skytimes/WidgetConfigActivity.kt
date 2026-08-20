package com.imnaiyar.skytimes.widgets.skytimes

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.imnaiyar.skytimes.R
import com.imnaiyar.skytimes.core.domain.EventData
import com.imnaiyar.skytimes.core.domain.EventKey
import com.imnaiyar.skytimes.core.domain.events
import com.imnaiyar.skytimes.core.ui.Callout
import com.imnaiyar.skytimes.widgets.WidgetPreferences
import com.imnaiyar.skytimes.widgets.WidgetSettingsReader
import com.materialkolor.rememberDynamicColorScheme
import kotlinx.coroutines.launch

/**
 * Widget configuration screen — shown when the user adds the SkyTimes widget.
 */
class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        // install splash so that splash screen theme works correctly
        installSplashScreen()
        super.onCreate(savedInstanceState)

        //  cancel widget placement if user backs out
        setResult(RESULT_CANCELED)


        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val seedColor = WidgetSettingsReader.getSeedColor(this)
        setContent {
            val color = rememberDynamicColorScheme(Color(seedColor), isDark = isSystemInDarkTheme())
            MaterialTheme(colorScheme = color) {
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

    private fun saveConfiguration(selectedEvents: Set<EventKey>) {
        // save to preference
        WidgetPreferences.setSelectedEvents(this, appWidgetId, selectedEvents)

        // Trigger an immediate widget update
        val glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)

        lifecycleScope.launch {
            SkyTimesWidget().update(this@WidgetConfigActivity, glanceId)
        }

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}

// ─── Configuration HomeScreens UI ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(
    appWidgetId: Int,
    onSave: (Set<EventKey>) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current

    // Load the current (or default) selection for this widget instance
    val selectedState = remember {
        val saved = WidgetPreferences.getSelectedEvents(context, appWidgetId)
        mutableStateMapOf<EventKey, Boolean>().apply {
            EventKey.entries.forEach { key ->
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
                .padding(padding.plus(PaddingValues(10.dp))),
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

                item {
                    Callout(
                        "Events are ordered based on their occurrence time." +
                                " Events appearing earlier will be displayed first on the widget.",
                        modifier = Modifier.padding(4.dp)
                    )
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
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
            .padding(horizontal = 4.dp),
        label = {
            Column {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        leadingIcon = if (isChecked) {
            {
                Icon(
                    painterResource(R.drawable.check),
                    contentDescription = "done",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
    )
}
