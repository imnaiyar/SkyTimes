package com.imnaiyar.skytimes.home.skytimes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.theme.labelTiny
import com.imnaiyar.skytimes.ui.Tooltip
import com.imnaiyar.skytimes.ui.animated.AnimatedTimer
import com.imnaiyar.skytimes.ui.animated.ClockDirection
import com.imnaiyar.skytimes.ui.animated.pulse
import com.imnaiyar.skytimes.utils.EventDetails
import com.imnaiyar.skytimes.utils.TimeFormatter
import com.imnaiyar.skytimes.utils.Times
import com.materialkolor.ktx.harmonize
import org.jetbrains.compose.resources.painterResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.notifications
import skytimes.shared.generated.resources.pin
import kotlin.time.Instant

@Composable
fun success(): Color {
    val scheme = MaterialTheme.colorScheme

    return Color(0xFF4CAF50).harmonize(scheme.primary)
}

@Composable
internal fun EventRow(
    row: IRow.Event,
    reorderMode: Boolean,
    eventDetails: EventDetails,
    isActive: Boolean,
    timeFormatter: TimeFormatter,
    now: Instant,
) {
    val remainingMillis = if (isActive) {
        eventDetails.status.remaining.inWholeMilliseconds
    } else {
        eventDetails.nextOccurrence.toEpochMilliseconds() - now.toEpochMilliseconds()
    }
    val countdown = timeFormatter.formatMillis(remainingMillis, false)
    val nextAt =
        if (eventDetails.status is Times.Active) eventDetails.status.endTime else eventDetails.nextOccurrence


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            EventNameLabel(
                eventDetails = eventDetails,
                isPinned = row.isPinned,
                notified = row.notified,
            )

            AnimatedVisibility(isActive) {
                Text(
                    text = "Active (Next at ${timeFormatter.format(eventDetails.nextOccurrence)})",
                    style = MaterialTheme.typography.labelTiny,
                    color = success(),
                    modifier = Modifier.pulse(speed = 2000, scale = 1.04f)
                )
            }
        }

        AnimatedVisibility(
            visible = !reorderMode,
            enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut(),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (isActive) {
                    Text(
                        text = "Ends in",
                        style = MaterialTheme.typography.labelTiny
                    )
                }
                AnimatedTimer(
                    time = countdown,
                    size = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 8.dp),
                    direction = ClockDirection.DOWN,
                )
                Text(
                    text = "At ${timeFormatter.format(nextAt)}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}


/** Event name with an optional pin badge and notification bell. */
@Composable
private fun EventNameLabel(
    eventDetails: EventDetails,
    isPinned: Boolean = false,
    notified: Notified,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.labelMedium,
) {
    val iconColor = MaterialTheme.colorScheme.primary

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = eventDetails.event.name, style = style, color = color)

        AnimatedVisibility(visible = isPinned) {
            Tooltip("This event is pinned to the top") {
                Icon(
                    painter = painterResource(Res.drawable.pin),
                    contentDescription = "Pinned",
                    modifier = Modifier.rotate(30f).size(18.dp),
                    tint = iconColor,
                )
            }
        }

        AnimatedVisibility(visible = notified != Notified.No) {
            Tooltip(text = "Reminders enabled for this event" + if (notified == Notified.YesButGlobalDisabled) " (notifications are globally disabled)" else "") {
                Icon(
                    painter = painterResource(Res.drawable.notifications),
                    contentDescription = "Reminder set",
                    modifier = Modifier.size(12.dp),
                    tint = if (notified == Notified.Yes) iconColor else iconColor.copy(alpha = 0.5f),
                )
            }
        }
    }
}