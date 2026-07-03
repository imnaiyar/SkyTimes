package com.imnaiyar.skytimes

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.GameTimeZone
import com.imnaiyar.skytimes.constants.RoundedCorner
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.ui.ClockDisplay
import com.imnaiyar.skytimes.utils.localDateToIso
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.calendar
import skytimes.shared.generated.resources.clock_analogue
import skytimes.shared.generated.resources.cogwheel
import skytimes.shared.generated.resources.quest_icon
import skytimes.shared.generated.resources.replay
import skytimes.shared.generated.resources.shards_icon
import kotlin.time.Instant

enum class Screen(
    val title: String,
    val icon: DrawableResource,
    val actions: @Composable (RowScope.() -> Unit)? = null
) {
    SkyTimes("SkyClock", Res.drawable.clock_analogue, actions = {
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
            Text(
                text = if (timeZone == GameTimeZone) "LA (Game) Time" else "Local Time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }),
    Quests("Daily Quests", Res.drawable.quest_icon, {
        val date = LocalAppContainer.current.clockRepository.observeDate()

        val tooltipState = rememberTooltipState()
        val scope = rememberCoroutineScope()

        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            state = tooltipState,
            tooltip = {
                PlainTooltip {
                    Text(
                        "The displayed date reflects the game's reset date at midnight, which is in LA timezone where TGC is based on."
                    )
                }
            },
        ) {
            Text(
                modifier = Modifier.clickable { scope.launch { tooltipState.show() } },
                text = date.value.format(localDateToIso),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }),
    Shards("Shards", Res.drawable.shards_icon, {
        val clockRepository = LocalAppContainer.current.clockRepository;
        val todayDate = clockRepository.observeDate()
        val shardDate = clockRepository.shardDate.collectAsState()
        var showPicker by remember { mutableStateOf(false) }

        val pickerState = rememberDatePickerState(
            shardDate.value.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        )
        val tooltipState = rememberTooltipState()

        Row(
            modifier = Modifier.animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Below
                ),
                state = tooltipState,
                tooltip = {
                    PlainTooltip {
                        Text(
                            "The displayed date reflects the game's reset date at midnight," +
                                    " which is in LA timezone where TGC is based on. It may differ from your actual local date"
                        )
                    }
                },
            ) {
                OutlinedButton(onClick = { showPicker = !showPicker }, shape = RoundedCorner) {
                    Icon(
                        painterResource(Res.drawable.calendar),
                        contentDescription = "Calendar Icon"
                    )

                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))

                    Text(shardDate.value.format(localDateToIso))
                }
            }

            if (shardDate.value != todayDate.value) IconButton(onClick = {
                clockRepository.setShardDate(
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
                                clockRepository.setShardDate(
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

    }),
    Settings("Settings", Res.drawable.cogwheel)
}