package com.imnaiyar.skytimes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.ui.ClockDisplay
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.DrawableResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.compose_multiplatform

enum class Screen(
    val title: String,
    val icon: DrawableResource,
    val actions: @Composable (RowScope.() -> Unit)? = null
) {
    Clock("Clock", Res.drawable.compose_multiplatform, actions = {
        var timeZone by remember { mutableStateOf(TimeZone.currentSystemDefault().id) }
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(
                onClick = {
                    timeZone = if (timeZone == TimeZone.currentSystemDefault().id) {
                        "America/Los_Angeles"
                    } else {
                        TimeZone.currentSystemDefault().id
                    }
                }
                )) {
            ClockDisplay(gameZone = timeZone == "America/Los_Angeles")
            Text(
                text = if (timeZone == "America/Los_Angeles") "LA (Game) Time" else "Local Time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }),
    Quests("Quests", Res.drawable.compose_multiplatform),
    Shards("Shards", Res.drawable.compose_multiplatform),
    Settings("Settings", Res.drawable.compose_multiplatform)
}