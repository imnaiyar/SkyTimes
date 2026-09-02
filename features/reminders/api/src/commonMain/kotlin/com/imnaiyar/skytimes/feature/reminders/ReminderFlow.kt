package com.imnaiyar.skytimes.feature.reminders

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.imnaiyar.skytimes.core.common.LocalApplicationScope
import com.imnaiyar.skytimes.core.common.LocalSnackBarState
import com.imnaiyar.skytimes.core.common.getPlatform
import com.imnaiyar.skytimes.core.domain.EventData
import com.imnaiyar.skytimes.core.domain.EventKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class ReminderDraft(
    val eventData: EventData,
    val existingReminder: Reminder? = null,
)

class ReminderFlowController(
    private val scope: CoroutineScope,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val notificationsToggle: NotificationsToggle,
    private val permissionController: ReminderPermissionController,
    private val isAndroid: Boolean = getPlatform().name.startsWith("Android"),
) {

    private var reminderDraft by mutableStateOf<ReminderDraft?>(null)
    private var reminderEditorVisible by mutableStateOf(false)
    private var shardEditorVisible by mutableStateOf(false)
    private var permissionSheetVisible by mutableStateOf(false)
    private var permissionSheetNextAction by mutableStateOf<(() -> Unit)?>(null)
    private var notificationPermissionStatus by mutableStateOf(ReminderPermissionStatus.Unavailable)
    private var exactAlarmPermissionStatus by mutableStateOf(ReminderPermissionStatus.Unavailable)

    private var reminderOffsetMinutes by mutableIntStateOf(0)
    private var shardType by mutableStateOf(ShardReminderType.BOTH)

    fun requestShardReminderEditor() {
        runWhenNotificationPermissionReady {
            val existing =
                reminderRepository.reminders.value.firstOrNull { it.eventId == EventKey.SHARDS }
            shardType = existing?.shardType ?: ShardReminderType.BOTH
            reminderOffsetMinutes = existing?.offsetMinutes ?: 0
            shardEditorVisible = true
        }
    }

    fun requestReminderEditor(eventData: EventData) {
        runWhenNotificationPermissionReady {
            val draft = ReminderDraft(
                eventData = eventData,
                existingReminder = reminderRepository.reminders.value.firstOrNull { it.eventId == eventData.key }
            )

            reminderDraft = draft
            reminderOffsetMinutes = draft.existingReminder?.offsetMinutes ?: 0
            reminderEditorVisible = true
        }
    }

    fun setNotificationsEnabled(enabled: Boolean, showPrompt: Boolean = true) {
        if (!enabled) {
            scope.launch {
                notificationsToggle.setEnabled(false)
                reminderScheduler.cancelAll()
            }
            return
        }
        // do nothing if already enabled
        if (notificationsToggle.isEnabled()) return;

        val persistEnabled: () -> Unit = {
            scope.launch {
                notificationsToggle.setEnabled(true)
                reminderScheduler.refresh()
            }
        }

        if (showPrompt) runWhenNotificationPermissionReady(persistEnabled)
        else persistEnabled()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RenderDialogs() {
        val lifecycle = LocalLifecycleOwner.current

        // snackbar host is supplied to scaffold of MainScreen
        val snackBarHostState = LocalSnackBarState.current

        DisposableEffect(lifecycle) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    scope.launch {
                        refreshPermissionStatuses()
                    }
                }
            }

            lifecycle.lifecycle.addObserver(observer)

            onDispose {
                lifecycle.lifecycle.removeObserver(observer)
            }
        }

        if (permissionSheetVisible) {
            LaunchedEffect(permissionSheetVisible) {
                refreshPermissionStatuses()
            }

            ReminderPermissionBottomSheet(
                notificationStatus = notificationPermissionStatus,
                exactAlarmStatus = exactAlarmPermissionStatus,
                showExactAlarm = isAndroid,
                onRequestNotifications = {
                    scope.launch {
                        notificationPermissionStatus =
                            permissionController.requestNotificationPermission()
                        refreshPermissionStatuses()
                    }
                },
                onOpenNotificationSettings = {
                    permissionController.openNotificationSettings()
                },
                onOpenExactAlarmSettings = {
                    reminderScheduler.requestExactAlarm()
                },
                onContinue = {
                    scope.launch {
                        refreshPermissionStatuses()
                        if (notificationPermissionStatus == ReminderPermissionStatus.Granted) {
                            permissionSheetNextAction?.invoke()
                            clearPermissionSheet()
                        }
                    }
                },
                onDismiss = { clearPermissionSheet() }
            )
        }

        val draft = reminderDraft

        if (reminderEditorVisible && draft != null) {
            ReminderOffsetDialog(
                eventTitle = draft.eventData.name,
                offsetMinutes = reminderOffsetMinutes,
                onOffsetChange = { reminderOffsetMinutes = it.coerceIn(0, 15) },
                showExactAlarmHint = isAndroid &&
                        exactAlarmPermissionStatus != ReminderPermissionStatus.Granted,
                openExactAlarm = { reminderScheduler.requestExactAlarm() },
                onConfirm = {
                    scope.launch {
                        saveReminder(draft, reminderOffsetMinutes)

                        snackBarHostState.showSnackbar(
                            "Reminder set for ${draft.eventData.name} with the offset of $reminderOffsetMinutes minutes.",
                            withDismissAction = true
                        )
                    }

                },
                onRemove = draft.existingReminder?.let {
                    {
                        scope.launch {
                            removeReminder(draft)
                            snackBarHostState.showSnackbar(
                                "Reminder disabled for ${draft.eventData.name}.",
                                withDismissAction = true,
                            )
                        }
                    }
                },
                onDismiss = { clearReminderEditor() }
            )
        }

        if (shardEditorVisible) {
            ShardReminderBottomSheet(
                type = shardType,
                offsetMinutes = reminderOffsetMinutes,
                onTypeChange = { shardType = it },
                onOffsetChange = { reminderOffsetMinutes = it.coerceIn(0, 15) },
                showExactAlarmHint = isAndroid && exactAlarmPermissionStatus != ReminderPermissionStatus.Granted,
                openExactAlarm = { reminderScheduler.requestExactAlarm() },
                onConfirm = {
                    scope.launch {
                        saveShardReminder()

                        snackBarHostState.showSnackbar(
                            "Reminder saved for shard type: ${
                                when (shardType) {
                                    ShardReminderType.RED -> "Red"
                                    ShardReminderType.BLACK -> "Black"
                                    else -> "Red and Black"
                                }
                            } shard with the offset $reminderOffsetMinutes minute(s).",
                            withDismissAction = true
                        )
                    }
                },
                onRemove = if (reminderRepository.reminders.value.any { it.eventId == EventKey.SHARDS }) {
                    {
                        scope.launch {
                            removeShardReminder()
                            snackBarHostState.showSnackbar(
                                "Reminder disabled for shards.",
                                withDismissAction = true
                            )
                        }
                    }
                } else null,
                onDismiss = { shardEditorVisible = false }
            )
        }
    }

    private fun runWhenNotificationPermissionReady(action: () -> Unit) {
        scope.launch {
            refreshPermissionStatuses()
            if (notificationPermissionStatus == ReminderPermissionStatus.Granted) {
                action()
            } else {
                permissionSheetNextAction = action
                permissionSheetVisible = true
            }
        }
    }

    private suspend fun refreshPermissionStatuses() {
        notificationPermissionStatus = permissionController.notificationStatus()
        exactAlarmPermissionStatus = if (!isAndroid || reminderScheduler.hasExactAlarm()) {
            ReminderPermissionStatus.Granted
        } else {
            ReminderPermissionStatus.Requestable
        }
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

        notificationsToggle.setEnabled(true)
        reminderRepository.upsert(reminder)
        reminderScheduler.scheduleReminder(reminder)
        clearReminderEditor()
    }

    private suspend fun removeReminder(draft: ReminderDraft) {
        reminderScheduler.cancelReminder(draft.eventData.key.name)
        reminderRepository.removeByEvent(draft.eventData.key)
        if (reminderRepository.reminders.value.none(Reminder::enabled)) {
            notificationsToggle.setEnabled(false)
        }
        clearReminderEditor()
    }

    private suspend fun saveShardReminder() {
        val reminder =
            reminderRepository.reminders.value.firstOrNull { it.eventId == EventKey.SHARDS }
                ?.copy(offsetMinutes = reminderOffsetMinutes, shardType = shardType, enabled = true)
                ?: Reminder(
                    "shards",
                    EventKey.SHARDS,
                    offsetMinutes = reminderOffsetMinutes,
                    shardType = shardType
                )
        notificationsToggle.setEnabled(true)
        reminderRepository.upsert(reminder)
        reminderScheduler.scheduleReminder(reminder)
        shardEditorVisible = false
    }

    private suspend fun removeShardReminder() {
        reminderScheduler.cancelReminder(EventKey.SHARDS.name)
        reminderRepository.remove("shards")
        if (reminderRepository.reminders.value.none(Reminder::enabled)) notificationsToggle.setEnabled(
            false
        )
        shardEditorVisible = false
    }

    private fun clearReminderEditor() {
        reminderDraft = null
        reminderEditorVisible = false
    }

    private fun clearPermissionSheet() {
        permissionSheetVisible = false
        permissionSheetNextAction = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderPermissionBottomSheet(
    notificationStatus: ReminderPermissionStatus,
    exactAlarmStatus: ReminderPermissionStatus,
    showExactAlarm: Boolean,
    onRequestNotifications: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Reminder permissions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    buildAnnotatedString {
                        append("Allow notifications so reminders can fire.")
                        if (showExactAlarm) append(" Without exact alarm, android may delay notifications.")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PermissionRow(
                title = "Notifications",
                subtitle = when (notificationStatus) {
                    ReminderPermissionStatus.Granted -> "Ready to show reminder alerts."
                    ReminderPermissionStatus.SettingsRequired -> "Grant permission through settings."
                    ReminderPermissionStatus.Unavailable -> "Notification permission is unavailable."
                    ReminderPermissionStatus.Requestable -> "Required for reminder alerts."
                },
                status = notificationStatus,
                actionLabel = when (notificationStatus) {
                    ReminderPermissionStatus.Requestable -> "Allow"
                    ReminderPermissionStatus.SettingsRequired -> "Settings"
                    else -> null
                },
                onAction = when (notificationStatus) {
                    ReminderPermissionStatus.Requestable -> onRequestNotifications
                    ReminderPermissionStatus.SettingsRequired -> onOpenNotificationSettings
                    else -> null
                }
            )

            if (showExactAlarm) {
                PermissionRow(
                    title = "Exact alarms",
                    subtitle = when (exactAlarmStatus) {
                        ReminderPermissionStatus.Granted -> "Reminders can fire at the selected time."
                        else -> "Optional. Without this, Android may delay reminders."
                    },
                    status = exactAlarmStatus,
                    actionLabel = if (exactAlarmStatus == ReminderPermissionStatus.Granted) null else "Settings",
                    onAction = if (exactAlarmStatus == ReminderPermissionStatus.Granted) null else onOpenExactAlarmSettings
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Not now")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onContinue,
                    enabled = notificationStatus == ReminderPermissionStatus.Granted
                ) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    subtitle: String,
    status: ReminderPermissionStatus,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PermissionStatusIcon(
            granted = status == ReminderPermissionStatus.Granted,
            modifier = Modifier.size(14.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                modifier = Modifier.clickable(onClick = onAction),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

        }
    }
}

@Composable
private fun PermissionStatusIcon(
    granted: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (granted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawCircle(
            color = color,
            style = Stroke(width = strokeWidth)
        )

        if (granted) {
            drawLine(
                color = color,
                start = Offset(size.width * 0.28f, size.height * 0.52f),
                end = Offset(size.width * 0.44f, size.height * 0.68f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(size.width * 0.44f, size.height * 0.68f),
                end = Offset(size.width * 0.74f, size.height * 0.34f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ReminderOffsetDialog(
    eventTitle: String,
    offsetMinutes: Int,
    onOffsetChange: (Int) -> Unit,
    openExactAlarm: () -> Unit,
    showExactAlarmHint: Boolean,
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

                if (showExactAlarmHint) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(12.dp)
                    ) {
                        Text(
                            buildAnnotatedString {
                                append("Exact alarm permission is off. The reminder will still be saved, but Android may deliver it late.")
                                append("\n")

                                withLink(
                                    LinkAnnotation.Clickable(
                                        "open exact alarm",
                                        styles = TextLinkStyles(
                                            SpanStyle(
                                                MaterialTheme.colorScheme.primary
                                            )
                                        ),
                                        linkInteractionListener = { openExactAlarm() }
                                    )
                                ) {
                                    append("Open settings")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShardReminderBottomSheet(
    type: ShardReminderType,
    offsetMinutes: Int,
    onTypeChange: (ShardReminderType) -> Unit,
    onOffsetChange: (Int) -> Unit,
    showExactAlarmHint: Boolean,
    openExactAlarm: () -> Unit,
    onConfirm: () -> Unit,
    onRemove: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Shard reminders",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text("Choose which shards to be reminded about when they land.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShardReminderType.entries.forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { onTypeChange(option) },
                        label = {
                            Text(
                                option.name.lowercase().replaceFirstChar { it.titlecase() })
                        }
                    )
                }
            }
            Text("$offsetMinutes minutes before", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = offsetMinutes.toFloat(),
                onValueChange = { onOffsetChange(it.roundToInt()) },
                valueRange = 0f..15f,
                steps = 14
            )
            if (showExactAlarmHint) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp)
                ) {
                    Text(
                        buildAnnotatedString {
                            append("Exact alarm permission is off. The reminder will still be saved, but Android may deliver it late.\n")
                            withLink(
                                LinkAnnotation.Clickable(
                                    "open exact alarm",
                                    styles = TextLinkStyles(SpanStyle(MaterialTheme.colorScheme.primary)),
                                    linkInteractionListener = { openExactAlarm() })
                            ) {
                                append("Open settings")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                onRemove?.let { TextButton(onClick = it) { Text("Remove") } }
                TextButton(onClick = onDismiss) { Text("Cancel") }
                TextButton(onClick = onConfirm) { Text("Save") }
            }
        }
    }
}


@Composable
fun rememberReminderFlow(): ReminderFlowController {
    val permissionController = rememberReminderPermissionController()
    val scope = LocalApplicationScope.current
    val reminderRepository = LocalReminderRepository.current
    val reminderScheduler = LocalReminderScheduler.current
    val notificationsToggle = LocalNotificationsToggle.current

    return remember(
        scope,
        reminderRepository,
        reminderScheduler,
        notificationsToggle,
        permissionController
    ) {
        ReminderFlowController(
            scope = scope,
            reminderRepository = reminderRepository,
            reminderScheduler = reminderScheduler,
            notificationsToggle = notificationsToggle,
            permissionController
        )
    }
}
