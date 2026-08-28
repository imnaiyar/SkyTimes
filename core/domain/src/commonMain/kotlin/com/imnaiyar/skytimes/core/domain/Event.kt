package com.imnaiyar.skytimes.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class EventData(
    val key: EventKey,
    val name: String,
    val category: EventCategory,
    val previewUrl: String? = null,
    val offset: Int = 0,
    val duration: Int? = null,
    val interval: Int? = null,
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
    DYE_EXCHANGE_SHOP,
    TWO_EMBERS_1,
    TWO_EMBERS_2,
    TWO_EMBERS_3,
    TWO_EMBERS_4
}

// TODO
enum class EventCategory {
    Wax,
    Resets,
    Activity,
    Concert,
    Others
}

private fun hours(value: Int) = value * 60

val events = listOf(
    EventData(
        key = EventKey.GEYSER,
        name = "Geyser",
        category = EventCategory.Wax,
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/b/bd/Wax-prairie-sanctuary-days-of-nature-2021.jpg",
        duration = 15,
        interval = hours(2),
        infographic = Infographic(
            by = "Clement",
            image = "$SkyHelperCDN/infographics/geyser.location.png"
        )
    ),
    EventData(
        key = EventKey.GRANDMA,
        name = "Grandma",
        category = EventCategory.Wax,
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/e/ea/Wax-social-light-grandma-dinner.jpg",
        offset = 30,
        duration = 15,
        interval = hours(2),
        infographic = Infographic(
            by = "Clement",
            image = "$SkyHelperCDN/infographics/grandma.location.png"
        )
    ),
    EventData(
        key = EventKey.TURTLE,
        name = "Turtle",
        category = EventCategory.Wax,
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/1/1d/Wax-social-light-prairie-sanctuary-turtle.png",
        offset = 50,
        duration = 10,
        interval = hours(2),
        infographic = Infographic(
            by = "Velvet",
            image = "$SkyHelperCDN/infographics/turtle.location.png"
        )
    ),
    EventData(
        key = EventKey.DAILY_RESET,
        name = "Daily Reset",
        category = EventCategory.Resets,
        interval = hours(24)
    ),
    EventData(
        key = EventKey.EDEN,
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/e/e5/Eden_2.png",
        category = EventCategory.Resets,
        name = "Eden/Weekly Reset",
        occursOn = OccursOn(
            weekDays = listOf(7)
        )
    ),
    EventData(
        key = EventKey.AURORA,
        name = "Aurora's Concert",
        category = EventCategory.Concert,
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/e/e8/Aurora-Homecoming-2025-promotion-The_Queen_Is_Returning_to_the_Realms.png",
        offset = 0,
        duration = 50,
        interval = hours(2),
        infographic = Infographic(
            by = "Clement",
            image = "$SkyHelperCDN/infographics/aurora.location.png"
        )
    ),
    EventData(
        key = EventKey.DREAM_SKATER,
        name = "Dream Skater",
        category = EventCategory.Wax,
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/a/a4/Wax-social-light-valley-village-dreams-skater.png",
        offset = hours(1),
        duration = 15,
        interval = hours(2),
        occursOn = OccursOn(
            weekDays = listOf(5, 6, 7)
        )
    ),
    EventData(
        key = EventKey.PASSAGE_QUESTS,
        category = EventCategory.Wax,
        name = "Passage Quests",
        previewUrl = "https://static.wikia.nocookie.net/sky-children-of-the-light/images/d/dd/Wax-forest-brook-passage-reliance.png",
        interval = 15,
    ),
    EventData(
        key = EventKey.NEST_SUNSET,
        category = EventCategory.Activity,
        name = "Nest Sunset",
        offset = 40,
        interval = hours(1),
    ),
    EventData(
        key = EventKey.FIREWORKS_FESTIVAL,
        category = EventCategory.Activity,
        name = "Fireworks Festival",
        duration = 10,
        interval = hours(4),
        occursOn = OccursOn(
            dayOfTheMonth = 1
        )
    ),
    EventData(
        key = EventKey.FAIRY_RING,
        category = EventCategory.Others,
        name = "Fairy Ring",
        offset = 50,
        interval = 60,
    ),
    EventData(
        key = EventKey.BROOK_RAINBOW,
        category = EventCategory.Others,
        name = "Forest Brook Rainbow",
        offset = hours(5),
        interval = hours(12),
    ),
    EventData(
        key = EventKey.WORKSHOP_RESET,
        category = EventCategory.Resets,
        name = "Nesting Workshop Rotation",
        occursOn = OccursOn(
            weekDays = listOf(5)
        ),
    ),
    EventData(
        key = EventKey.DYE_EXCHANGE_SHOP,
        category = EventCategory.Resets,
        name = "Dye Exchange Shop",
        occursOn = OccursOn(
            weekDays = listOf(5)
        ),
    ),
    EventData(
        key = EventKey.TWO_EMBERS_1,
        category = EventCategory.Concert,
        name = "Two Embers: Chapter 1",
        interval = 20,
    ),
    EventData(
        key = EventKey.TWO_EMBERS_2,
        category = EventCategory.Concert,
        name = "Two Embers: Chapter 2",
        interval = 20,
        offset = 5
    ),
    EventData(
        key = EventKey.TWO_EMBERS_3,
        category = EventCategory.Concert,
        name = "Two Embers: Chapter 3",
        interval = 20,
        offset = 10
    ),
    EventData(
        key = EventKey.TWO_EMBERS_4,
        category = EventCategory.Concert,
        name = "Two Embers: Chapter 4",
        interval = 20,
        offset = 15
    ),
)
