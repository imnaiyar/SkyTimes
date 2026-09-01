package com.imnaiyar.skytimes.feature.home

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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.imnaiyar.skytimes.core.navigation.AppTab
import com.imnaiyar.skytimes.core.navigation.LocalTutorialManager
import com.imnaiyar.skytimes.core.navigation.MainRoute
import com.imnaiyar.skytimes.core.ui.theme.titleTiny
import com.imnaiyar.skytimes.feature.home.generated.resources.Res
import com.imnaiyar.skytimes.feature.home.generated.resources.lightmend_lantern
import com.imnaiyar.skytimes.feature.home.shards.ShardScreen
import com.imnaiyar.skytimes.feature.home.skytimes.SkytimesScreen
import com.imnaiyar.skytimes.feature.quests.QuestsScreen
import com.imnaiyar.skytimes.feature.settings.LocalSettingsViewModel
import com.imnaiyar.skytimes.feature.settings.SettingsScreen
import com.imnaiyar.skytimes.feature.reminders.rememberReminderFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@ExperimentalMaterial3Api
@Composable
fun MainScreen(
    onOpenVault: () -> Unit,
    onOpenThemeSettings: () -> Unit,
    backStack: NavBackStack<NavKey>
) {
    val screens = remember { AppTab.entries }

    val settings = LocalSettingsViewModel.current.settings.collectAsState()
    val tutorialManager = LocalTutorialManager.current
    val tutorialState by tutorialManager.state.collectAsState()
    val reminderFlow = rememberReminderFlow()

    val defaultScreenIndex = screens.indexOf(settings.value.homeScreen)

    val pagerState = rememberPagerState(defaultScreenIndex) {
        screens.size
    }


    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    // The tutorial framework stays navigation-agnostic; this app mapping moves
    // the pager before a target on another page is registered and spotlighted.
    LaunchedEffect(tutorialState.currentStep) {
        val step = tutorialState.currentStep ?: return@LaunchedEffect
        val destination = screens.indexOf(step.screen)
        if (destination >= 0 && destination != pagerState.currentPage) {
            pagerState.animateScrollToPage(destination)
        }
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        // only enable if main route, and current tab is not preferred tab
        isBackEnabled = pagerState.currentPage != defaultScreenIndex && backStack.lastOrNull() is MainRoute
    ) {
        scope.launch {
            pagerState.scrollToPage(defaultScreenIndex)
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
                                pagerState.scrollToPage(index)
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
                            Text(screen.title, style = MaterialTheme.typography.titleTiny())
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
            ExtendedFloatingActionButton(
                onClick = onOpenVault,
                modifier = Modifier.onGloballyPositioned { fabHeight = it.size.height },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                expanded = bottomScroll.state.collapsedFraction < 0.5f,
                text = { Text("Vault Archive", style = MaterialTheme.typography.titleSmall) },
                icon = {
                    Image(
                        painterResource(Res.drawable.lightmend_lantern),
                        contentDescription = "Lightmending Lantern",
                        modifier = Modifier.size(30.dp)
                    )
                }
            )
        }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
        ) { page ->
            val tutorialTargetsEnabled = page == pagerState.settledPage

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
                        actions = {
                            screens[page].topBarActions(reminderFlow)?.invoke(this, tutorialTargetsEnabled)
                        }
                    )
                }
            ) { innerPadding ->
                val modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding)
                    .padding(horizontal = 10.dp)

                when (screens[page]) {
                    AppTab.SkyTimes -> SkytimesScreen(
                        modifier,
                        fabPad,
                        tutorialTargetsEnabled,
                        reminderFlow
                    )

                    AppTab.Quests -> QuestsScreen(modifier, fabPad, tutorialTargetsEnabled)
                    AppTab.Shards -> ShardScreen(
                        modifier,
                        fabPad,
                        tutorialTargetsEnabled,
                        tutorialState.currentStep
                    )

                    AppTab.Settings -> SettingsScreen(
                        modifier = modifier,
                        fabPad = fabPad,
                        onOpenThemeSettings = onOpenThemeSettings,
                        reminderFlow = reminderFlow
                    )
                }
            }
        }
        reminderFlow.RenderDialogs()
    }
}
