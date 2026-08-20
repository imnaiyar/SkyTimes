package com.imnaiyar.skytimes.feature.home.shards

import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imnaiyar.skytimes.core.domain.ShardData
import com.imnaiyar.skytimes.feature.home.generated.resources.Res
import com.imnaiyar.skytimes.feature.home.generated.resources.ac
import com.imnaiyar.skytimes.feature.home.generated.resources.wax
import org.jetbrains.compose.resources.painterResource

@Composable
fun ShardTitle(shard: ShardData) {
    val iconId = "rewardIcon"

    Text(
        text = buildAnnotatedString {
            withStyle(
                MaterialTheme.typography.titleLargeEmphasized.toSpanStyle()
                    .copy(
                        if (shard.isRed) Color.Red else Color.Black,
                        shadow = Shadow(
                            LocalContentColor.current,
                            blurRadius = 1f,
                            offset = Offset.VisibilityThreshold
                        )
                    )
            ) {
                append(
                    if (shard.isRed) "Red Shard" else "Black Shard"
                )
            }
            append("  (Reward: ")
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
    ShardArea(shard)
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