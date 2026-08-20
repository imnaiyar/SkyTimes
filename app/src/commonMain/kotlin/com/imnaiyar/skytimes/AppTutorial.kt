package com.imnaiyar.skytimes

import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.navigation.AppTutorialStep
import com.imnaiyar.skytimes.core.onboarding.SwipeDirection
import com.imnaiyar.skytimes.core.onboarding.TooltipPlacement
import com.imnaiyar.skytimes.core.onboarding.TutorialDefinition
import com.imnaiyar.skytimes.core.onboarding.TutorialFlow
import com.imnaiyar.skytimes.core.onboarding.TutorialGestureHint

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
        )
    )
)
