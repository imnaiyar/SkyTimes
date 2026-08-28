package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.common.TimeFormatter
import com.imnaiyar.skytimes.core.domain.EventDetails
import com.imnaiyar.skytimes.core.domain.Times
import com.imnaiyar.skytimes.core.ui.animated.AnimatedTimer
import com.imnaiyar.skytimes.core.ui.animated.ClockDirection
import com.imnaiyar.skytimes.core.ui.theme.labelTiny
import com.imnaiyar.skytimes.feature.home.generated.resources.Res
import com.imnaiyar.skytimes.feature.home.generated.resources.notifications
import com.imnaiyar.skytimes.feature.home.generated.resources.pin
import com.materialkolor.ktx.harmonize
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Instant

@Composable
fun success(): Color {
    val scheme = MaterialTheme.colorScheme

    return Color(0xFF4CAF50).harmonize(scheme.primary)
}

@Composable
internal fun EventRow(
    row: IRow.Event,
    eventDetails: EventDetails,
    isActive: Boolean,
    timeFormatter: TimeFormatter,
    now: Instant,
    onPinToggle: () -> Unit,
    onReminderToggle: () -> Unit
) {
    val remainingMillis = if (isActive) {
        eventDetails.status.remaining.inWholeMilliseconds
    } else {
        eventDetails.nextOccurrence.toEpochMilliseconds() - now.toEpochMilliseconds()
    }
    val countdown = timeFormatter.formatMillis(remainingMillis, false)
    val nextAt = when (val status = eventDetails.status) {
        is Times.Active -> status.endTime
        else -> eventDetails.nextOccurrence
    }


    EventColumnLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 5.dp),
        eventName = {
            Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = eventDetails.event.name,
                style = MaterialTheme.typography.labelMedium,
            )
            AnimatedVisibility(isActive) {
                Text(
                    text = "Active (Next at ${timeFormatter.format(eventDetails.nextOccurrence)})",
                    style = MaterialTheme.typography.labelTiny,
                    color = success().copy(0.55f),
                )
            }
            }
        },
        nextTime = {
            Text(
                text = timeFormatter.format(nextAt, withSeconds = false),
                style = MaterialTheme.typography.labelMedium,
            )
        },
        remaining = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isActive) {
                    Text(
                        text = "Ends in",
                        style = MaterialTheme.typography.labelTiny,
                    )
                }
                AnimatedTimer(
                    time = countdown,
                    size = MaterialTheme.typography.labelMedium,
                    direction = ClockDirection.DOWN,
                )
            }
        },
        toggles = {
            EventToggles(
                row.isPinned,
                row.notified,
                onPinToggle,
                onReminderToggle,
            )
        },
    )
}


/** Toggles to pin or set an event reminder */
@Composable
private fun EventToggles(
    isPinned: Boolean = false,
    notified: Notified,
    onPinToggle: () -> Unit,
    onReminderToggle: () -> Unit,
) {
    val iconColor = MaterialTheme.colorScheme.primary
    val pinRotation by animateFloatAsState(
        targetValue = if (isPinned) 30f else 0f,
        label = "PinRotation",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.pin),
            contentDescription = null,
            tint = if (isPinned) iconColor else iconColor.copy(0.5f),
            modifier = Modifier.clickable(onClick = onPinToggle)
                .rotate(pinRotation),
        )


        Icon(
            painter = painterResource(Res.drawable.notifications),
            contentDescription = "Reminder set",
            modifier = Modifier.requiredSize(15.dp).clickable(onClick = onReminderToggle),
            tint = if (notified == Notified.Yes) {
                iconColor
            } else {
                iconColor.copy(alpha = 0.5f)
            },
        )
    }
}
