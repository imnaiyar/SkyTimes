package com.imnaiyar.skytimes.feature.home.skytimes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.common.TimeFormatter
import com.imnaiyar.skytimes.core.domain.EventDetails
import com.imnaiyar.skytimes.core.domain.Times
import com.imnaiyar.skytimes.core.ui.Tooltip
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


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            EventNameLabel(
                eventDetails = eventDetails,
                isPinned = row.isPinned,
                notified = row.notified,
                modifier = Modifier.fillMaxWidth(),
            )

            AnimatedVisibility(isActive) {
                Text(
                    text = "Active (Next at ${timeFormatter.format(eventDetails.nextOccurrence)})",
                    style = MaterialTheme.typography.labelTiny,
                    color = success().copy(0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "At ${timeFormatter.format(nextAt)}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
        ) {
            if (isActive) {
                Text(
                    text = "Ends in",
                    style = MaterialTheme.typography.labelTiny,
                )
            }

            AnimatedTimer(
                time = countdown,
                size = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp),
                direction = ClockDirection.DOWN,
            )
        }
    }
}


/** Event name with an optional pin badge and notification bell. */
@Composable
private fun EventNameLabel(
    eventDetails: EventDetails,
    isPinned: Boolean = false,
    notified: Notified,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.labelSmall,
) {
    val iconColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = eventDetails.event.name,
            style = style,
            color = color,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .basicMarquee(),
        )

        AnimatedVisibility(visible = isPinned) {
            Tooltip("This event is pinned to the top") {
                Icon(
                    painter = painterResource(Res.drawable.pin),
                    contentDescription = "Pinned",
                    modifier = Modifier
                        .rotate(30f)
                        .size(18.dp),
                    tint = iconColor,
                )
            }
        }

        AnimatedVisibility(visible = notified != Notified.No) {
            Tooltip(
                text = "Reminders enabled for this event" +
                        if (notified == Notified.YesButGlobalDisabled) {
                            " (notifications are globally disabled)"
                        } else {
                            ""
                        }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.notifications),
                    contentDescription = "Reminder set",
                    modifier = Modifier.size(12.dp),
                    tint = if (notified == Notified.Yes) {
                        iconColor
                    } else {
                        iconColor.copy(alpha = 0.5f)
                    },
                )
            }
        }
    }
}