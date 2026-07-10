package com.imnaiyar.skytimes.onboarding

import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.screens.Screen

/** App-owned onboarding; the reusable tutorial package contains no screen knowledge. */
enum class AppTutorialStep(
    override val targetId: String,
    val screen: Screen
) : TutorialStep {
    HomeReorder("home_reorder", Screen.SkyTimes),
    HomeEventContextMenu("home_event_context_menu", Screen.SkyTimes),
    QuestPullToRefresh("quest_pull_to_refresh", Screen.Quests),
    ShardCountdown("shard_countdown", Screen.Shards),
    ShardDateSwipe("shard_date_swipe", Screen.Shards),
    ShardDatePicker("shard_date_picker", Screen.Shards)
}

const val FirstLaunchTutorialFlowId = "first_launch"

val FirstLaunchTutorialFlow = TutorialFlow(
    id = FirstLaunchTutorialFlowId,
    steps = listOf(
        TutorialDefinition(
            step = AppTutorialStep.HomeReorder,
            title = "Reorder events",
            description = "Tap this button to enter reorder mode, then drag events into the order you prefer.",
            preferredPlacement = TooltipPlacement.Below,
            spotlightPadding = 8.dp
        ),
        TutorialDefinition(
            step = AppTutorialStep.HomeEventContextMenu,
            title = "More event actions",
            description = "Long-press an event to open its context menu.",
            preferredPlacement = TooltipPlacement.Below,
            spotlightPadding = 8.dp
        ),
        TutorialDefinition(
            step = AppTutorialStep.QuestPullToRefresh,
            title = "Refresh quests",
            description = "Pull down from the top of this page to refresh the latest quest data.",
            preferredPlacement = TooltipPlacement.Below,
            gestureHint = TutorialGestureHint.Swipe(SwipeDirection.Down, "Pull down to refresh")
        ),
        TutorialDefinition(
            step = AppTutorialStep.ShardCountdown,
            title = "Shard timings",
            description = "Tap this countdown to open detailed shard timings.",
            preferredPlacement = TooltipPlacement.Below
        ),
        TutorialDefinition(
            step = AppTutorialStep.ShardDateSwipe,
            title = "Browse shard dates",
            description = "Swipe up or down to change the selected shard date.",
            preferredPlacement = TooltipPlacement.Below,
            gestureHint = TutorialGestureHint.Swipe(SwipeDirection.Vertical, "Swipe up or down")
        ),
        TutorialDefinition(
            step = AppTutorialStep.ShardDatePicker,
            title = "Choose a date",
            description = "You can also select a date manually from here.",
            preferredPlacement = TooltipPlacement.Below
        ),
        TutorialDefinition(
            step = AppTutorialStep.ShardDatePicker,
            title = "Choose a date",
            description = "You can also select a date manually from here.",
            preferredPlacement = TooltipPlacement.Below
        )
    )
)
