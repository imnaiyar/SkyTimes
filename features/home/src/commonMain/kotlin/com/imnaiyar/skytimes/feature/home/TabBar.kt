package com.imnaiyar.skytimes.feature.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.ui.ClockDisplay
import com.imnaiyar.skytimes.core.ui.DecoratedText
import com.imnaiyar.skytimes.core.ui.RoundedCorner
import com.imnaiyar.skytimes.core.ui.Tooltip
import com.imnaiyar.skytimes.core.common.localDateToIso
import com.imnaiyar.skytimes.core.data.LocalClockRepository
import com.imnaiyar.skytimes.core.domain.GameTimeZone
import com.imnaiyar.skytimes.core.navigation.AppTab
import com.imnaiyar.skytimes.core.navigation.AppTutorialStep
import com.imnaiyar.skytimes.core.onboarding.TutorialTarget
import com.imnaiyar.skytimes.feature.home.shards.LocalShardDate
import com.imnaiyar.skytimes.feature.reminders.ReminderFlowController
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import com.imnaiyar.skytimes.feature.home.generated.resources.Res
import com.imnaiyar.skytimes.feature.home.generated.resources.calendar
import com.imnaiyar.skytimes.feature.home.generated.resources.replay
import com.imnaiyar.skytimes.feature.home.generated.resources.notifications
import kotlin.time.Instant

/** Top-bar actions for each main tab, owned by the home feature. */
@OptIn(ExperimentalMaterial3Api::class)
fun AppTab.topBarActions(reminderFlow: ReminderFlowController? = null): (@Composable RowScope.(Boolean) -> Unit)? = when (this) {
    AppTab.SkyTimes -> { _ ->
        var timeZone by remember { mutableStateOf(TimeZone.currentSystemDefault()) }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.clickable(
                onClick = {
                    timeZone = if (timeZone == TimeZone.currentSystemDefault()) {
                        GameTimeZone
                    } else {
                        TimeZone.currentSystemDefault()
                    }
                }
            )) {
            ClockDisplay(gameZone = timeZone == GameTimeZone)
            DecoratedText(
                text = if (timeZone == GameTimeZone) "LA (Game) Time" else "Local Time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    AppTab.Quests -> { _ ->
        val date = LocalClockRepository.current.observeDate()

        Tooltip(dateDisclaimer) {
            Text(
                text = date.value.format(localDateToIso),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }

    AppTab.Shards -> { tutorialTargetsEnabled ->
        val clockRepository = LocalClockRepository.current
        val todayDate = clockRepository.observeDate()
        val shardDateState = LocalShardDate.current
        val shardDate = shardDateState.shardDate.collectAsState()
        var showPicker by remember { mutableStateOf(false) }

        val pickerState = rememberDatePickerState(
            shardDate.value.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        )

        LaunchedEffect(shardDate.value) {
            pickerState.selectedDateMillis =
                shardDate.value
                    .atStartOfDayIn(TimeZone.UTC)
                    .toEpochMilliseconds()
        }

        val isToday = shardDate.value == todayDate.value
        Row(
            modifier = Modifier.animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            reminderFlow?.let { flow ->
                val reminders = com.imnaiyar.skytimes.feature.reminders.LocalReminderRepository.current.reminders.collectAsState()
                IconButton(onClick = flow::requestShardReminderEditor) {
                    Icon(
                        painterResource(Res.drawable.notifications),
                        contentDescription = "Shard reminder",
                        tint = if (reminders.value.any { it.eventId == com.imnaiyar.skytimes.core.domain.EventKey.SHARDS && it.enabled }) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            TutorialTarget(
                id = AppTutorialStep.ShardDatePicker.targetId,
                enabled = tutorialTargetsEnabled
            ) {
                Tooltip(dateDisclaimer, showOnClick = false) {
                    OutlinedButton(onClick = { showPicker = !showPicker }, shape = RoundedCorner) {
                        Icon(
                            painterResource(Res.drawable.calendar),
                            contentDescription = "Calendar Icon"
                        )

                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))

                        Text(if (isToday) "Today" else shardDate.value.format(localDateToIso))
                    }
                }
            }

            if (!isToday) IconButton(onClick = {
                shardDateState.setShardDate(
                    todayDate.value
                )
            }) {
                Icon(
                    painterResource(Res.drawable.replay),
                    contentDescription = "Reset to today's date"
                )
            }
        }

        if (showPicker) {
            DatePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pickerState.selectedDateMillis?.let { date ->
                                shardDateState.setShardDate(
                                    Instant.fromEpochMilliseconds(date)
                                        .toLocalDateTime(TimeZone.UTC).date
                                )
                            }
                            showPicker = false
                        }
                    ) {
                        Text("Ok")
                    }
                }
            ) {
                DatePicker(state = pickerState, showModeToggle = false)
            }
        }
    }

    AppTab.Settings -> null
}
