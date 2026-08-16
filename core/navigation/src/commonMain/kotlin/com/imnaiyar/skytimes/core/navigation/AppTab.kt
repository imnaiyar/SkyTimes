package com.imnaiyar.skytimes.core.navigation

import com.imnaiyar.skytimes.core.navigation.generated.resources.Res
import com.imnaiyar.skytimes.core.navigation.generated.resources.clock_analogue
import com.imnaiyar.skytimes.core.navigation.generated.resources.cogwheel
import com.imnaiyar.skytimes.core.navigation.generated.resources.quest_icon
import com.imnaiyar.skytimes.core.navigation.generated.resources.shards_icon
import org.jetbrains.compose.resources.DrawableResource

/**
 * The main navigation tabs shown by the home pager.
 *
 * Enum names double as the persisted preference keys, so they must stay
 * stable across releases (they previously lived on `HomeScreens`).
 */
enum class AppTab(
    val title: String,
    val icon: DrawableResource,
) {
    SkyTimes("SkyClock", Res.drawable.clock_analogue),
    Quests("Quests", Res.drawable.quest_icon),
    Shards("Shards", Res.drawable.shards_icon),
    Settings("Settings", Res.drawable.cogwheel),
}
