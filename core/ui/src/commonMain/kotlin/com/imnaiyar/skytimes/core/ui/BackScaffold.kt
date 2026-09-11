package com.imnaiyar.skytimes.core.ui

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
import com.imnaiyar.skytimes.core.ui.generated.resources.Res
import com.imnaiyar.skytimes.core.ui.generated.resources.chevron_right
import org.jetbrains.compose.resources.painterResource

/**
 * Scaffold with back icon action
 */
@Composable
fun BackScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = {},
    bottomBar: @Composable () -> Unit = {},
    snackBarHost: @Composable () -> Unit = {},
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
        snackbarHost = snackBarHost,
        bottomBar = bottomBar
    ) {
        content(it)
    }
}
