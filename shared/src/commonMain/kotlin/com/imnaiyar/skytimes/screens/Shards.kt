package com.imnaiyar.skytimes.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.imnaiyar.skytimes.constants.GameTimeZone
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.ui.AnimatedTimer
import com.imnaiyar.skytimes.utils.TimeUtils
import com.imnaiyar.skytimes.utils.TimeValue
import com.imnaiyar.skytimes.utils.getShard
import kotlinx.datetime.toLocalDateTime

@Composable
fun ShardsScreen(modifier: Modifier) {
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

    val allEnded = upcomingOrActive == null;

    var timer: Long? = null
    val text: String
    if (!allEnded) {
        if (upcomingOrActive.shardLand > now.value) {
            timer = (upcomingOrActive.shardLand - now.value).inWholeMilliseconds
            text = "Starts in"
        } else {
            timer = (upcomingOrActive.shardEnd - now.value).inWholeMilliseconds
            text = "Ends in"
        }
    } else text = "All shards ended for the day"
    Column(modifier = modifier.padding(16.dp)) {

        if (timer != null) {
            val formatted = TimeUtils().formatTime(
                TimeValue.localTime(
                    upcomingOrActive!!.shardEnd.toLocalDateTime(
                        GameTimeZone
                    ).time
                ), false
            )
            Text(
                if (isRed) "Red Shard" else "Black Shard",
                style = MaterialTheme.typography.titleLargeEmphasized
            )
            Text(
                "at ${shard.area.displayName} in ${shard.realm.displayName}",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )

                AnimatedTimer(
                    TimeUtils().formatMillis(timer),
                    size = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.inversePrimary
                )

                Text("At: $formatted", modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        } else Text(
            text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}