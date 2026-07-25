package com.imnaiyar.skytimes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.imnaiyar.skytimes.reminders.ContextHolder
import com.imnaiyar.skytimes.widgets.WidgetPreviewGenerator
import com.imnaiyar.skytimes.widgets.WidgetUpdateWorker
import com.imnaiyar.skytimes.widgets.skytimes.SkyTimesWidget
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ContextHolder.initialize(this)

        setContent {
            App()
        }

        lifecycleScope.launch {
            SkyTimesWidget().updateAll(this@MainActivity)
        }

        // updates widgets at an 15 min interval
        WidgetUpdateWorker.enqueuePeriodicUpdate(this)
        // generates widgets preview at a 1 day interval
        WidgetPreviewGenerator.enqueue(this)

    }
}


/*
@Preview
@Composable
fun AppAndroidPreview() {
    App()
}*/
