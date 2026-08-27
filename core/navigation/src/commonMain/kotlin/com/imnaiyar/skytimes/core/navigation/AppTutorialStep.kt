package com.imnaiyar.skytimes.core.navigation

import com.imnaiyar.skytimes.core.onboarding.TutorialStep

/** App-owned onboarding steps; the reusable tutorial framework contains no screen knowledge. */
enum class AppTutorialStep(
    override val targetId: String,
    val screen: AppTab
) : TutorialStep {
    QuestPullToRefresh("quest_pull_to_refresh", AppTab.Quests),
    ShardCountdown("shard_countdown", AppTab.Shards),
    ShardDateSwipe("shard_date_swipe", AppTab.Shards),
    ShardDatePicker("shard_date_picker", AppTab.Shards)
}
