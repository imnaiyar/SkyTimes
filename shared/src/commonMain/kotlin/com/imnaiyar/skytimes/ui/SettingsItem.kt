package com.imnaiyar.skytimes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.constants.RoundedCorner


/**
 * A composable that represents a settings item with a title, optional subtitle, and an action.
 *
 * @param title The title of the settings item.
 * @param subtitle An optional subtitle for the settings item.
 * @param action The action associated with the settings item, which can be a switch or a button.
 * @param modifier The modifier to be applied to the settings item.
 */
@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Unspecified,
        shape = RoundedCorner,
        onClick = onClick ?: {},
    ) {
        Row(
            modifier = Modifier
                .padding(all = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )

                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                content()
            }

            Spacer(Modifier.width(16.dp))

            if (action != null) action()
        }
    }
}