package com.imnaiyar.skytimes.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import com.imnaiyar.skytimes.core.common.LocalSnackBarState

@Composable
fun SnackBarHostLocal() {
    SnackbarHost(LocalSnackBarState.current) {
        Snackbar(
            it,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            dismissActionContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
