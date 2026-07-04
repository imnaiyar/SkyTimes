package com.imnaiyar.skytimes.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imnaiyar.skytimes.constants.GameTimeZone
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.ui.AnimatedTimer
import com.imnaiyar.skytimes.ui.Card
import com.imnaiyar.skytimes.ui.ClockDirection
import com.imnaiyar.skytimes.ui.DecoratedText
import com.imnaiyar.skytimes.ui.Tooltip
import com.imnaiyar.skytimes.utils.ShardData
import com.imnaiyar.skytimes.utils.ShardOccurrence
import com.imnaiyar.skytimes.utils.TimeUtils
import com.imnaiyar.skytimes.utils.getShard
import com.imnaiyar.skytimes.utils.rememberTimeFormatter
import com.imnaiyar.skytimes.utils.toOrdinal
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.ac
import skytimes.shared.generated.resources.chevron_right
import skytimes.shared.generated.resources.wax
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun ShardsScreen(modifier: Modifier, fabPad: PaddingValues) {
    val clockRepository = LocalAppContainer.current.clockRepository

    val now = clockRepository.now.collectAsState()
    val shardDate = clockRepository.shardDate.collectAsState()
    val shard = getShard(shardDate.value)
    if (shard == null) {
        NoShardDisplay(modifier.padding(fabPad), shardDate.value)
        return
    }

    val upcomingOrActive =
        shard.occurrences.find { occurrence -> occurrence.shardEnd > now.value }


    LazyColumn(modifier = modifier, contentPadding = fabPad) {

        // some space at the start
        item { Spacer(Modifier.height(15.dp)) }
        // shard title
        item { ShardTitle(shard) }

        // shard area
        item {
            ShardArea(shard)
        }

        item { Spacer(Modifier.height(30.dp)) }

        // current upcoming shard countdown/info
        item { ShardCountdown(upcomingOrActive, now.value, shard) }

        item { Spacer(Modifier.height(30.dp)) }
        // timeline
        item {
            ShardTimeline(upcomingOrActive.takeIf { it != null } ?: shard.occurrences.last(),
                now.value)
        }
    }
}


@Composable
private fun NoShardDisplay(modifier: Modifier, date: LocalDate) {
    val isToday = date == Clock.System.now().toLocalDateTime(GameTimeZone).date

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "No shards ${
                if (isToday) "today" else "on ${
                    date.format(LocalDate.Format {
                        monthName(MonthNames.ENGLISH_ABBREVIATED)
                        char(' ')
                        day()
                        chars(", ")
                        year()
                    })
                }"
            }!", color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}


@Composable
private fun ShardTimeline(occurrence: ShardOccurrence, now: Instant) {
    val list = mapOf(
        "Early Sky Change" to occurrence.skyChange,
        "Gate Shard" to occurrence.gateShard,
        "Shard Lands" to occurrence.shardLand,
        "Shard Ends" to occurrence.shardEnd
    ).entries

    var showTimeline by remember { mutableStateOf(false) }
    val timeUtils = rememberTimeFormatter()

    val rotation by animateFloatAsState(
        targetValue = if (showTimeline) 90f else 0f,
        label = "ChevronRotation"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(5.dp).clickable {
                showTimeline = !showTimeline
            },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Shard Timelines",
                style = MaterialTheme.typography.bodyMedium
            )

            Icon(
                painterResource(Res.drawable.chevron_right),
                contentDescription = "Chevron",
                modifier = Modifier.rotate(rotation)
            )
        }

        // timeline row (could  also be separated)
        AnimatedVisibility(showTimeline) {
            FlowRow() {
                repeat(4) { index ->
                    val (title, dur) = list.elementAt(index)
                    Card(
                        modifier = Modifier.widthIn(min = 150.dp).weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(title, style = MaterialTheme.typography.labelMedium)


                            Spacer(Modifier.height(5.dp))
                            HorizontalDivider()

                            Spacer(Modifier.height(10.dp))

                            Tooltip(
                                "${
                                    timeUtils.format(
                                        dur,
                                        GameTimeZone
                                    )
                                } in Los Angeles (Game's Timezone)"
                            ) {
                                DecoratedText(
                                    text = timeUtils.format(dur),
                                    textDecoration =
                                        if (now > dur) TextDecoration.LineThrough
                                        else TextDecoration.None,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ShardTitle(shard: ShardData) {
    val iconId = "rewardIcon"

    Text(
        text = buildAnnotatedString {
            withStyle(
                MaterialTheme.typography.titleLargeEmphasized.toSpanStyle()
                    .copy(if (shard.isRed) Color.Red else Color.Black)
            ) {
                append(
                    if (shard.isRed) "Red Shard" else "Black Shard"
                )
            }
            append("  ")
            append("(Reward: ")
            withStyle(MaterialTheme.typography.bodySmall.toSpanStyle()) {
                append((if (shard.isRed) shard.reward!! else 200.0).toString())
            }
            appendInlineContent(iconId, "[icon]")
            append(")")
        },
        style = MaterialTheme.typography.bodySmall,
        inlineContent = mapOf(
            iconId to InlineTextContent(
                Placeholder(
                    18.sp,
                    18.sp,
                    PlaceholderVerticalAlign.Center
                )
            ) {
                Image(
                    painterResource(if (shard.isRed) Res.drawable.ac else Res.drawable.wax),
                    contentDescription = if (shard.isRed) "Ascended Candle" else "Wax",
                    modifier = Modifier.size(20.dp)
                )
            }
        )
    )
}


@Composable
private fun ShardArea(shard: ShardData) {

    Text(
        text = buildAnnotatedString {
            append("at ")
            withStyle(
                MaterialTheme.typography.labelMedium.toSpanStyle()
                    .copy(color = MaterialTheme.colorScheme.primary)
            ) { append(shard.area.displayName) }
            append(" in ")
            withStyle(
                MaterialTheme.typography.labelMedium.toSpanStyle()
                    .copy(color = MaterialTheme.colorScheme.primary)
            ) { append(shard.realm.displayName) }
        },
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
private fun ShardCountdown(occurrence: ShardOccurrence?, now: Instant, shard: ShardData) {
    var timer: Long
    var timerHeader: String
    var timerSubtitle: String

    val formatter = rememberTimeFormatter()



    occurrence?.let {
        val shardIndex = (shard.occurrences.indexOf(it) + 1).toOrdinal()

        if (it.shardLand > now) {
            timer = (it.shardLand - now).inWholeMilliseconds
            timerHeader = "$shardIndex Shard Lands in"
            timerSubtitle = "At: " + formatter.format(it.shardLand)
        } else {
            timer = (it.shardEnd - now).inWholeMilliseconds
            timerHeader = "$shardIndex Shard Ends in"
            timerSubtitle = "At: " + formatter.format(it.shardEnd)
        }
    } ?: run {
        // means all shards has ended
        val lastShard = shard.occurrences.last()
        timer = (lastShard.shardEnd - now).inWholeMilliseconds
        timerHeader = "All shards ended"

        timerSubtitle = "Ago"
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                timerHeader,
                style = MaterialTheme.typography.labelLarge
            )

            AnimatedTimer(
                TimeUtils().formatMillis(abs(timer)),
                size = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.primary,
                direction = if (timer > 0) ClockDirection.DOWN else ClockDirection.UP
            )

            Text(timerSubtitle, style = MaterialTheme.typography.labelLarge)
        }
    }
}