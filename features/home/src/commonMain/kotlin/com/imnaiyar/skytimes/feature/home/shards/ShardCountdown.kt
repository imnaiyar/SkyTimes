package com.imnaiyar.skytimes.feature.home.shards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.common.TimeUtils
import com.imnaiyar.skytimes.core.common.toOrdinal
import com.imnaiyar.skytimes.core.domain.ShardData
import com.imnaiyar.skytimes.core.domain.ShardOccurrence
import com.imnaiyar.skytimes.core.ui.animated.AnimatedTimer
import com.imnaiyar.skytimes.core.ui.animated.ClockDirection
import com.imnaiyar.skytimes.core.ui.rememberTimeFormatter
import kotlin.math.abs
import kotlin.time.Instant

@Composable
fun ShardCountdown(
    occurrence: ShardOccurrence?,
    now: Instant,
    shard: ShardData,
    onClick: () -> Unit
) {
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
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