package com.imnaiyar.skytimes.reminders.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.EventData
import com.imnaiyar.skytimes.di.AppContainer
import com.imnaiyar.skytimes.getPlatform
import com.imnaiyar.skytimes.reminders.Reminder
import com.imnaiyar.skytimes.reminders.ReminderRepository
import com.imnaiyar.skytimes.reminders.ReminderScheduler
import com.imnaiyar.skytimes.reminders.rememberNotificationPermissionRequester
import com.imnaiyar.skytimes.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.roundToInt

private data class ReminderDraft(
    val eventData: EventData,
    val existingReminder: Reminder? = null,
)

class ReminderFlowController(
    private val scope: CoroutineScope,
    private val settingsRepository: SettingsRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val permissionRequester: ((Boolean) -> Unit) -> Unit,
    private val isAndroid: Boolean = getPlatform().name.startsWith("Android"),
) {

    private var reminderDraft by mutableStateOf<ReminderDraft?>(null)
    private var reminderEditorVisible by mutableStateOf(false)
    private var exactAlarmVisible by mutableStateOf(false)
    private var exactAlarmNextAction by mutableStateOf<(() -> Unit)?>(null)

    private var reminderOffsetMinutes by mutableIntStateOf(0)

    fun requestReminderEditor(eventData: EventData) {
        scope.launch {
            if (!ensureNotificationPermission()) return@launch

            val draft = ReminderDraft(
                eventData = eventData,
                existingReminder = reminderRepository.reminders.value.firstOrNull { it.eventId == eventData.key }
            )

            val showEditor = {
                reminderDraft = draft
                reminderOffsetMinutes = draft.existingReminder?.offsetMinutes ?: 0
                reminderEditorVisible = true
            }

            promptExactAlarmIfNeeded(showEditor)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean, showPrompt: Boolean = true) {
        if (!enabled) {
            scope.launch {
                settingsRepository.setNotificationsEnabled(false)
                reminderScheduler.cancelAll()
            }
            return
        }
        // do nothing if already enabled
        if (settingsRepository.settings.value.notificationsEnabled) return;

        scope.launch {
            if (!ensureNotificationPermission()) {
                settingsRepository.setNotificationsEnabled(false)
                return@launch
            }

            val persistEnabled: () -> Unit = {
                scope.launch {
                    settingsRepository.setNotificationsEnabled(true)
                    reminderScheduler.refresh()
                }

            }

            if (showPrompt) promptExactAlarmIfNeeded(persistEnabled)
            else persistEnabled()
        }
    }

    @Composable
    fun RenderDialogs() {
        if (exactAlarmVisible) {
            ExactAlarmDialog(
                onConfirm = {
                    reminderScheduler.requestExactAlarm()
                    exactAlarmNextAction?.invoke()
                    clearExactAlarmState()
                },
                onDismiss = {
                    exactAlarmNextAction?.invoke()
                    clearExactAlarmState()
                }
            )
        }

        val draft = reminderDraft

        if (reminderEditorVisible && draft != null) {
            ReminderOffsetDialog(
                eventTitle = draft.eventData.name,
                offsetMinutes = reminderOffsetMinutes,
                onOffsetChange = { reminderOffsetMinutes = it.coerceIn(0, 15) },
                onConfirm = {
                    scope.launch {
                        saveReminder(draft, reminderOffsetMinutes)
                    }
                },
                onRemove = draft.existingReminder?.let {
                    {
                        scope.launch {
                            removeReminder(draft)
                        }
                    }
                },
                onDismiss = { clearReminderEditor() }
            )
        }
    }

    private suspend fun ensureNotificationPermission(): Boolean {
        if (reminderScheduler.hasPermission()) return true

        // iOS implementation is simple and is handled by schedular
        // android's depend on activity (needs composable) so has different implementation
        if (!isAndroid) return reminderScheduler.requestPermission()

        return suspendCancellableCoroutine { continuation ->
            permissionRequester { granted ->
                if (continuation.isActive) {
                    continuation.resume(granted)
                }
            }
        }
    }

    private fun promptExactAlarmIfNeeded(nextAction: () -> Unit) {
        if (!isAndroid) {
            nextAction()
            return
        }

        if (!reminderScheduler.hasExactAlarm()) {
            exactAlarmNextAction = nextAction
            exactAlarmVisible = true
        } else nextAction()
    }

    private suspend fun saveReminder(draft: ReminderDraft, offsetMinutes: Int) {
        val reminder = draft.existingReminder?.copy(
            offsetMinutes = offsetMinutes,
            enabled = true,
        ) ?: Reminder(
            id = draft.eventData.key.name,
            eventId = draft.eventData.key,
            enabled = true,
            offsetMinutes = offsetMinutes,
        )

        reminderRepository.upsert(reminder)
        setNotificationsEnabled(enabled = true, showPrompt = false)
        reminderScheduler.scheduleReminder(reminder)
        clearReminderEditor()
    }

    private suspend fun removeReminder(draft: ReminderDraft) {
        reminderScheduler.cancelReminder(draft.eventData.key.name)
        reminderRepository.removeByEvent(draft.eventData.key)
        if (reminderRepository.reminders.value.none(Reminder::enabled)) {
            settingsRepository.setNotificationsEnabled(false)
        }
        clearReminderEditor()
    }

    private fun clearReminderEditor() {
        reminderDraft = null
        reminderEditorVisible = false
    }

    private fun clearExactAlarmState() {
        exactAlarmVisible = false
        exactAlarmNextAction = null
    }
}

@Composable
private fun ExactAlarmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allow exact alarms?") },
        text = {
            Text(
                "We use exact alarms so reminders fire at the chosen offset. If you skip this, reminders can be delayed by the system.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now")
            }
        }
    )
}

@Composable
private fun ReminderOffsetDialog(
    eventTitle: String,
    offsetMinutes: Int,
    onOffsetChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onRemove: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminder for $eventTitle") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Choose how many minutes before the event the reminder should fire.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "$offsetMinutes minutes before",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = offsetMinutes.toFloat(),
                    onValueChange = { onOffsetChange(it.roundToInt()) },
                    valueRange = 0f..15f,
                    steps = 14
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            if (onRemove != null) {
                TextButton(onClick = onRemove) {
                    Text("Remove")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}


@Composable
fun rememberReminderFlow(appContainer: AppContainer): ReminderFlowController {
    val permissionRequester = rememberNotificationPermissionRequester()
    return remember(appContainer) {
        ReminderFlowController(
            scope = appContainer.applicationScope,
            settingsRepository = appContainer.settingsRepository,
            reminderRepository = appContainer.reminderRepository,
            reminderScheduler = appContainer.reminderScheduler,
            permissionRequester
        )
    }
}