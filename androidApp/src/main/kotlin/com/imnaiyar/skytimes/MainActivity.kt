package com.imnaiyar.skytimes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.imnaiyar.skytimes.reminders.ContextHolder
import com.imnaiyar.skytimes.widget.SkyTimesWidget
import com.imnaiyar.skytimes.widget.WidgetUpdateWorker

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // initialize context for reminder manager
            ContextHolder.initialize(this)
            // Schedule periodic widget updates via WorkManager
            WidgetUpdateWorker.enqueuePeriodicUpdate(this)
            App()

            // update all widgets
            SkyTimesWidget.updateAllWidgets(this@MainActivity)
        }
    }
}


/*
@Preview
@Composable
fun AppAndroidPreview() {
    App()
}*/
