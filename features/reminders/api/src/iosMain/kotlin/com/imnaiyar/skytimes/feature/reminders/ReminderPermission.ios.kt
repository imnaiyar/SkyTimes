package com.imnaiyar.skytimes.feature.reminders

import androidx.compose.runtime.Composable

@Composable
actual fun rememberNotificationPermissionRequester(): ((Boolean) -> Unit) -> Unit = {}
