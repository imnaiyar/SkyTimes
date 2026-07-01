package com.imnaiyar.skytimes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.imnaiyar.skytimes.nav.*
import com.imnaiyar.skytimes.settings.SettingsViewModel
import com.imnaiyar.skytimes.theme.AppTheme

val NavController =
    staticCompositionLocalOf<NavHostController> {
        error("No NavController provided")
    }
    
val LocalViewModel =     
    staticCompositionLocalOf<SettingsViewModel> {
        error("No ViewModel provided")
    }
    
@ExperimentalMaterial3Api
@Composable
fun App() {
    val settingsViewModel = viewModel { SettingsViewModel() }
    val settings by settingsViewModel.settings.collectAsState()

    val navController = rememberNavController()
    
        CompositionLocalProvider(
            NavController provides navController,
            LocalViewModel provides settingsViewModel
        ) {
            AppTheme(themeMode = settings.themeMode) {
                NavHost(
                navController = navController,
                startDestination = SplashRoute
            ) {
                mainGraph(navController)
            }
        }
    }
}
