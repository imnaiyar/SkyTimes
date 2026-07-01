package com.imnaiyar.skytimes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.rememberNavController
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.imnaiyar.skytimes.nav.VaultRoute
import com.imnaiyar.skytimes.screens.*
import com.imnaiyar.skytimes.settings.SettingsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@ExperimentalMaterial3Api
@Composable
fun MainScreen() {
    val screens = remember { Screen.entries }

    val pagerState = rememberPagerState {
        screens.size
    }

    val navController = NavController.current
    val scope = rememberCoroutineScope()

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = pagerState.currentPage != 0
    ) {
        scope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    val bottomScroll = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(bottomScroll.nestedScrollConnection),
        bottomBar = {
            BottomAppBar(scrollBehavior = bottomScroll) {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(screen.icon),
                                contentDescription = screen.title,
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        label = {
                            Text(screen.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    navController.navigate(VaultRoute)
                }
            ) {
                    Icon(
                        painter = painterResource(Screen.Clock.icon),
                        contentDescription = Screen.Clock.title,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Vault",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

        }
    ) { outerPadding ->

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(bottom = outerPadding.calculateBottomPadding())
                .fillMaxSize()
        ) { page ->


            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopAppBar(
                        contentPadding = PaddingValues(end = 10.dp),
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            scrolledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                        ),
                        title = {
                            Text(
                                text = screens[page].title,
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        actions = screens[page].actions ?: {}
                    )
                }
            ) { innerPadding ->

                when (screens[page]) {
                    Screen.Clock -> HomeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )

                    Screen.Quests -> QuestsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )

                    Screen.Shards -> ShardsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )

                    Screen.Settings -> SettingsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}


fun NavGraphBuilder.mainGraph() {

}
