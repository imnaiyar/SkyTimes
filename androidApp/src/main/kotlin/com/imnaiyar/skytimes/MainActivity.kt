package com.imnaiyar.skytimes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.imnaiyar.skytimes.di.AppContainer
import com.imnaiyar.skytimes.reminders.AndroidReminderScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appContainer = remember {
                AppContainer { settingsRepository, reminderRepository, scope ->
                    AndroidReminderScheduler(
                        context = applicationContext,
                        settingsRepository = settingsRepository,
                        reminderRepository = reminderRepository,
                        scope = scope
                    )
                }
            }
            App(appContainer)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}