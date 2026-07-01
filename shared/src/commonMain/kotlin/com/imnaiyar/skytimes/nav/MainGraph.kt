package com.imnaiyar.skytimes.nav

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.composable
import com.imnaiyar.skytimes.MainScreen
import com.imnaiyar.skytimes.SplashScreen
import com.imnaiyar.skytimes.settings.SettingsViewModel
import com.imnaiyar.skytimes.vault_archive.MainArchive

@ExperimentalMaterial3Api
fun NavGraphBuilder.mainGraph(
    navigator: NavHostController,
) {

    composable<MainRoute> {
        MainScreen()
    }

    composable<VaultRoute> {
        MainArchive()
    }

    composable<SplashRoute> {
        SplashScreen(
            onFinished = {
                navigator.navigate(MainRoute) {
                    popUpTo(SplashRoute) {
                        inclusive = true
                    }
                }
            }
        )
    }
}
