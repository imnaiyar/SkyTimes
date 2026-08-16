package com.imnaiyar.skytimes.feature.reminders

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat

@Composable
actual fun rememberNotificationPermissionRequester(): ((Boolean) -> Unit) -> Unit {
    var pendingCallback by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        pendingCallback?.invoke(granted)
        pendingCallback = null
        if (!granted && activity != null) {
            // TODO: handle showing dialogue to redirect to app settings
            println(
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }

    return remember {
        { callback: (Boolean) -> Unit ->
            pendingCallback = callback
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
