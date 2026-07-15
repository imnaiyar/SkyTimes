package com.imnaiyar.skytimes

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.imnaiyar.skytimes.reminder.AndroidContextHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Expose this Activity for runtime permission requests.
        AndroidContextHolder.activity = this

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (AndroidContextHolder.activity == this) {
            AndroidContextHolder.activity = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AndroidContextHolder.PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            AndroidContextHolder.permissionCallback?.invoke(granted)
            AndroidContextHolder.permissionCallback = null
        }
    }
}