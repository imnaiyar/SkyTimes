package com.imnaiyar.skytimes.constants

import kotlinx.serialization.Serializable

@Serializable
data class EventData(
    val key: EventKey,
    val name: String,
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
        index = 0,
        offset = 0,
        duration = 15,
        interval = hours(2),
        displayAllTimes = true,
        infographic = Infographic(
            by = "Clement",
            image = "https://media.discordapp.net/attachments/867638574571323424/1252998364941914243/Visit_Geyser_Clement.png?ex=66744129&is=6672efa9&hm=8d76d1767aca362d23547b1e3beb2b610f58e4fbec24b12af56fdc745f7074e8&"
        )
    ),
    EventData(
        key = EventKey.GRANDMA,
        name = "Grandma",
        index = 1,
        offset = 30,
        duration = 15,
        interval = hours(2),
        displayAllTimes = true,
        infographic = Infographic(
            by = "Clement",
            image = "https://media.discordapp.net/attachments/867638574571323424/1252998366288416849/Visit_Grandma_Clement.png?ex=6674412a&is=6672efaa&hm=7228b695ec7008204fede2f3d6b4864a06a7cfa25a14ab4d7572957ee940044c&"
        )
    ),
    EventData(
        key = EventKey.TURTLE,
        name = "Turtle",
        index = 2,
        offset = 50,
        duration = 10,
        interval = hours(2),
        displayAllTimes = true,
        infographic = Infographic(
            by = "Velvet",
            image = "https://media.discordapp.net/attachments/867638574571323424/1252998363205472316/Visit_Turtle_Velvet.jpg?ex=66744129&is=6672efa9&hm=8c189ff8501fc88810606b832addbea8a9a81eb7a7a6b17019ff1ced593e1ae8&"
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
        index = 5,
        offset = 0,
        duration = 50,
        interval = hours(2),
        displayAllTimes = true
    ),
    EventData(
        key = EventKey.DREAM_SKATER,
        name = "Dream Skater",
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
