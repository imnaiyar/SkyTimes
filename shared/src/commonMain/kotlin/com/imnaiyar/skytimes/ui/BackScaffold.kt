package com.imnaiyar.skytimes.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import org.jetbrains.compose.resources.painterResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.chevron_right

/**
 * Scaffold with back icon action
 */
@Composable
fun BackScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (padding: PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = actions,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painterResource(Res.drawable.chevron_right),
                            modifier = Modifier.rotate(180f),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) {
        content(it)
    }
}
