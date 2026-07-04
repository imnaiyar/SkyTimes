package com.imnaiyar.skytimes.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.ui.AnimatedTimer
import com.imnaiyar.skytimes.ui.ClockDirection
import com.imnaiyar.skytimes.utils.TimeUtils
import com.imnaiyar.skytimes.utils.getShard
import com.imnaiyar.skytimes.utils.rememberTimeFormatter
import kotlin.math.abs
import kotlin.time.Instant

@Composable
fun ShardsScreen(modifier: Modifier, fabPad: PaddingValues) {
    val clockRepository = LocalAppContainer.current.clockRepository

    val now = clockRepository.now.collectAsState()
    val shardDate = clockRepository.shardDate.collectAsState()
    val shard = getShard(shardDate.value)
    if (shard == null) {
        Text("No Shard")
        return
    }
    val isRed = shard.isRed;
    val upcomingOrActive =
        shard.occurrences.find { occurrence -> occurrence.shardEnd > now.value }

    var timer: Long
    var timerHeader: String
    var timerSubtitle: String

    val timeUtils = rememberTimeFormatter()

    val formatShardTime = { time: Instant -> timeUtils.format(time) }

    //  get the timer and text to  display
    upcomingOrActive?.let {
        if (it.shardLand > now.value) {
            timer = (it.shardLand - now.value).inWholeMilliseconds
            timerHeader = "Starts in"
            timerSubtitle = "At: " + formatShardTime(it.shardLand)
        } else {
            timer = (it.shardEnd - now.value).inWholeMilliseconds
            timerHeader = "Ends in"
            timerSubtitle = "At: " + formatShardTime(it.shardEnd)
        }
    } ?: run {
        // means all shards has ended
        val lastShard = shard.occurrences.last()
        timer = (lastShard.shardEnd - now.value).inWholeMilliseconds
        timerHeader = "All shards ended"

        timerSubtitle = "Ago"
    }

    Column(modifier = modifier.padding(16.dp)) {
        // shard title
        Text(
            if (isRed) "Red Shard" else "Black Shard",
            style = MaterialTheme.typography.titleLargeEmphasized
        )
        // shard area
        Text(
            "at ${shard.area.displayName} in ${shard.realm.displayName}",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(Modifier.height(20.dp))

        // current upcoming shard countdown/info
        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                timerHeader,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall
            )

            AnimatedTimer(
                TimeUtils().formatMillis(abs(timer)),
                size = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.inversePrimary,
                direction = if (timer > 0) ClockDirection.DOWN else ClockDirection.UP
            )

            Text(timerSubtitle, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}