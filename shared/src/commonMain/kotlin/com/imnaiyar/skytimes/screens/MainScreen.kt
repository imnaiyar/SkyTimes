package com.imnaiyar.skytimes.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.imnaiyar.skytimes.NavController
import com.imnaiyar.skytimes.di.LocalSettingsViewModel
import com.imnaiyar.skytimes.nav.VaultRoute
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.lightmend_lantern

@ExperimentalMaterial3Api
@Composable
fun MainScreen() {
    val screens = remember { Screen.entries }

    val settings = LocalSettingsViewModel.current.settings.collectAsState()

    val defaultScreenIndex = screens.indexOf(settings.value.homeScreen)

    val pagerState = rememberPagerState(defaultScreenIndex) {
        screens.size
    }


    var showFab by remember { mutableStateOf(true) }

    val navController = NavController.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = pagerState.currentPage != defaultScreenIndex
    ) {
        scope.launch {
            pagerState.animateScrollToPage(defaultScreenIndex)
        }
    }

    val bottomScroll = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    var fabHeight by remember {
        mutableStateOf(0)
    }


    val heightInDp = with(LocalDensity.current) { fabHeight.toDp() }
    val fabPad = PaddingValues(bottom = heightInDp + 16.dp, top = 11.dp)

    Scaffold(
        modifier = Modifier.nestedScroll(bottomScroll.nestedScrollConnection),
        bottomBar = {
            BottomAppBar(scrollBehavior = bottomScroll) {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
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
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(showFab) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(VaultRoute) },
                    modifier = Modifier.onGloballyPositioned { fabHeight = it.size.height },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    expanded = bottomScroll.state.collapsedFraction < 0.5f,
                    text = { Text("Vault Archive") },
                    icon = {
                        Image(
                            painterResource(Res.drawable.lightmend_lantern),
                            contentDescription = "Lightmending Lantern",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                )
            }
        }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize(),
            beyondViewportPageCount = screens.size - 1
        ) { page ->

            Scaffold(
                topBar = {
                    TopAppBar(
                        contentPadding = PaddingValues(end = 10.dp),
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = BottomAppBarDefaults.containerColor,
                            scrolledContainerColor = BottomAppBarDefaults.containerColor,
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
                val modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding)
                    // horizontal padding to align FAB
                    .padding(horizontal = 16.dp)

                when (screens[page]) {
                    Screen.SkyTimes -> HomeScreen(
                        modifier,
                        setFabVisible = { value -> showFab = value },
                        fabPad
                    )

                    Screen.Quests -> QuestsScreen(modifier, fabPad)
                    Screen.Shards -> ShardsScreen(modifier, fabPad)
                    Screen.Settings -> SettingsScreen(modifier, fabPad)
                }
            }
        }
    }
}
