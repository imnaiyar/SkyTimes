package com.imnaiyar.skytimes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.imnaiyar.skytimes.nav.*
import com.imnaiyar.skytimes.theme.AppTheme

val NavController =
    staticCompositionLocalOf<NavHostController> {
        error("No NavController provided")
    }
@ExperimentalMaterial3Api
@Composable
fun App() {

    AppTheme() {
        val navController = rememberNavController()
        CompositionLocalProvider(
            NavController provides navController
        ) {
            NavHost(
                navController = navController,
                startDestination = SplashRoute
            ) {
                mainGraph(navController)
            }
        }
    }
}
