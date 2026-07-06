package com.imnaiyar.skytimes.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.imnaiyar.skytimes.MainScreen
import com.imnaiyar.skytimes.screens.ThemePage
import com.imnaiyar.skytimes.vault_archive.MainArchive

@ExperimentalMaterial3Api
fun NavGraphBuilder.mainGraph() {

    composable<MainRoute> {
        MainScreen()
    }

    composable<VaultRoute>(
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left
            )
        }) {
        MainArchive()
    }
    composable<ThemeSettingsRoute>(
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right
            )
        },
    ) {
        ThemePage()
    }
}
