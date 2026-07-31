package com.imnaiyar.skytimes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.RoundedCorner
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import skytimes.core.generated.resources.Res
import skytimes.core.generated.resources.check_circle
import skytimes.core.generated.resources.error
import skytimes.core.generated.resources.info
import skytimes.core.generated.resources.warning

enum class CalloutType {
    INFO, SUCCESS, WARNING, ERROR
}

private data class CalloutColors(
    val background: Color,
    val text: Color
)

/**
 * Material3's ColorScheme has no native "success" or "warning" roles,
 * so we derive reasonable equivalents from the existing scheme.
 * Swap these mappings for your own custom color extension if you have one.
 */
@Composable
private fun colorsFor(type: CalloutType): CalloutColors {
    val scheme = MaterialTheme.colorScheme
    return when (type) {
        CalloutType.INFO -> CalloutColors(
            background = scheme.primary,
            text = scheme.onPrimary
        )

        CalloutType.SUCCESS -> CalloutColors(
            background = scheme.tertiary,
            text = scheme.onTertiary
        )

        CalloutType.WARNING -> CalloutColors(
            background = scheme.secondary,
            text = scheme.onSecondary
        )

        CalloutType.ERROR -> CalloutColors(
            background = scheme.error,
            text = scheme.onError
        )
    }
}

private fun iconFor(type: CalloutType): DrawableResource = when (type) {
    CalloutType.INFO -> Res.drawable.info
    CalloutType.SUCCESS -> Res.drawable.check_circle
    CalloutType.WARNING -> Res.drawable.warning
    CalloutType.ERROR -> Res.drawable.error
}

@Composable
fun Callout(
    text: String,
    modifier: Modifier = Modifier,
    type: CalloutType = CalloutType.INFO,
    title: String? = null
) {
    val colors = colorsFor(type)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.background, RoundedCorner)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(iconFor(type)),
            contentDescription = type.name,
            tint = colors.text,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (title != null) {
                Text(
                    text = title,
                    color = colors.text,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = text,
                color = colors.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}