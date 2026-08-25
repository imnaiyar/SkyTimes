package com.imnaiyar.skytimes.feature.home.shards

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.domain.ShardData
import com.imnaiyar.skytimes.core.domain.SkyHelperCDN
import com.imnaiyar.skytimes.core.ui.RemoteImage
import com.imnaiyar.skytimes.core.ui.SlidingToggle
import com.imnaiyar.skytimes.feature.home.generated.resources.Res
import com.imnaiyar.skytimes.feature.home.generated.resources.data
import com.imnaiyar.skytimes.feature.home.generated.resources.map


@Composable
fun ShardInfographics(shard: ShardData) {
    // Internal state used only if the caller doesn't hoist it themselves.
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    val getInfographicsUrl =
        { type: String -> SkyHelperCDN + "/shards/${type.lowercase()}/${shard.area.key}.png" }

    val getImageDisplay = @Composable { type: String ->
        val url = getInfographicsUrl(type)
        RemoteImage(
            url,
            contentDescription = "Shard $type",
            modifier = Modifier.size(300.dp)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            buildAnnotatedString {
                append(if (!isFlipped) "Shard Location" else "Shard Data")
                append(" ")
                withStyle(
                    MaterialTheme.typography.labelMedium.toSpanStyle()
                        .copy(LocalContentColor.current.copy(0.5f))
                ) {
                    if (isFlipped) append("(By Gale)") else append("(By clement)")
                }
            },
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.animateContentSize()
        )

        SlidingToggle(
            icons = listOf(Res.drawable.map, Res.drawable.data),
            selectedIndex = if (isFlipped) 1 else 0,
            itemSize = 30.dp,
            useHaptics = true,
            roundedCornerIndicator = RoundedCornerShape(8.dp),
            onSelectedChange = { isFlipped = it == 1 }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            getImageDisplay("Location")
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                contentAlignment = Alignment.Center
            ) {
                getImageDisplay("Data")
            }
        }
    }
}