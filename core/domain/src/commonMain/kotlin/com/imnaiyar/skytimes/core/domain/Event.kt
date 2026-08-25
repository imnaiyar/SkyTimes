package com.imnaiyar.skytimes.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class EventData(
    val key: EventKey,
    val name: String,
    val previewUrl: String? = null,
    val index: Int,
    val offset: Int,
    val duration: Int? = null,
    val interval: Int? = null,
    val displayAllTimes: Boolean = false,
    val occursOn: OccursOn? = null,
    val infographic: Infographic? = null,
)

@Serializable
data class OccursOn(
    val weekDays: List<Int>? = null,
    val dayOfTheMonth: Int? = null,
)

@Serializable
data class Infographic(
    val by: String,
    val image: String,
)

enum class EventKey {
    GEYSER,
    GRANDMA,
    TURTLE,
    DAILY_RESET,
    EDEN,
    AURORA,
    DREAM_SKATER,
    PASSAGE_QUESTS,
    NEST_SUNSET,
    FIREWORKS_FESTIVAL,
    FAIRY_RING,
    BROOK_RAINBOW,
    WORKSHOP_RESET,
    DYE_EXCHANGE_SHOP
}

// TODO
enum class EventCategory {
    Pinned,
    Wax,
    Resets,
    Activity
}

private fun hours(value: Int) = value * 60

val events = listOf(
    EventData(
        key = EventKey.GEYSER,
        name = "Geyser",
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/b/bd/Wax-prairie-sanctuary-days-of-nature-2021.jpg",
        index = 0,
        offset = 0,
        duration = 15,
        interval = hours(2),
        displayAllTimes = true,
        infographic = Infographic(
            by = "Clement",
            image = "$SkyHelperCDN/infographics/geyser.location.png"
        )
    ),
    EventData(
        key = EventKey.GRANDMA,
        name = "Grandma",
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/e/ea/Wax-social-light-grandma-dinner.jpg",
        index = 1,
        offset = 30,
        duration = 15,
        interval = hours(2),
        displayAllTimes = true,
        infographic = Infographic(
            by = "Clement",
            image = "$SkyHelperCDN/infographics/grandma.location.png"
        )
    ),
    EventData(
        key = EventKey.TURTLE,
        name = "Turtle",
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/1/1d/Wax-social-light-prairie-sanctuary-turtle.png",
        index = 2,
        offset = 50,
        duration = 10,
        interval = hours(2),
        displayAllTimes = true,
        infographic = Infographic(
            by = "Velvet",
            image = "$SkyHelperCDN/infographics/turtle.location.png"
        )
    ),
    EventData(
        key = EventKey.DAILY_RESET,
        name = "Daily Reset",
        index = 3,
        offset = 0,
        interval = hours(24)
    ),
    EventData(
        key = EventKey.EDEN,
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/e/e5/Eden_2.png",
        name = "Eden/Weekly Reset",
        index = 4,
        offset = 0,
        occursOn = OccursOn(
            weekDays = listOf(7)
        )
    ),
    EventData(
        key = EventKey.AURORA,
        name = "Aurora's Concert",
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/e/e8/Aurora-Homecoming-2025-promotion-The_Queen_Is_Returning_to_the_Realms.png",
        index = 5,
        offset = 0,
        duration = 50,
        interval = hours(2),
        displayAllTimes = true,
        infographic = Infographic(
            by = "Clement",
            image = "$SkyHelperCDN/infographics/aurora.location.png"
        )
    ),
    EventData(
        key = EventKey.DREAM_SKATER,
        name = "Dream Skater",
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/a/a4/Wax-social-light-valley-village-dreams-skater.png",
        index = 6,
        offset = hours(1),
        duration = 15,
        interval = hours(2),
        displayAllTimes = true,
        occursOn = OccursOn(
            weekDays = listOf(5, 6, 7)
        )
    ),
    EventData(
        key = EventKey.PASSAGE_QUESTS,
        name = "Passage Quests",
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/d/dd/Wax-forest-brook-passage-reliance.png",
        index = 7,
        offset = 0,
        interval = 15,
        displayAllTimes = true
    ),
    EventData(
        key = EventKey.NEST_SUNSET,
        name = "Nest Sunset",
        index = 8,
        offset = 40,
        interval = hours(1),
        displayAllTimes = true
    ),
    EventData(
        key = EventKey.FIREWORKS_FESTIVAL,
        name = "Fireworks Festival",
        index = 9,
        offset = 0,
        duration = 10,
        interval = hours(4),
        displayAllTimes = true,
        occursOn = OccursOn(
            dayOfTheMonth = 1
        )
    ),
    EventData(
        key = EventKey.FAIRY_RING,
        name = "Fairy Ring",
        index = 11,
        offset = 50,
        interval = 60,
        displayAllTimes = true
    ),
    EventData(
        key = EventKey.BROOK_RAINBOW,
        name = "Forest Brook Rainbow",
        index = 12,
        offset = hours(5),
        interval = hours(12),
        displayAllTimes = true
    ),
    EventData(
        key = EventKey.WORKSHOP_RESET,
        name = "Nesting Workshop Rotation",
        index = 13,
        occursOn = OccursOn(
            weekDays = listOf(5)
        ),
        offset = 0
    ),
    EventData(
        key = EventKey.DYE_EXCHANGE_SHOP,
        name = "Dye Exchange Shop",
        index = 13,
        occursOn = OccursOn(
            weekDays = listOf(5)
        ),
        offset = 0
    )
)
