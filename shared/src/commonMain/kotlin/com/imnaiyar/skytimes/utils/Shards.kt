package com.imnaiyar.skytimes.utils

import com.imnaiyar.skytimes.constants.GameTimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

enum class Realm(val displayName: String) {
    PRAIRIE("Daylight Prairie"),
    FOREST("Hidden Forest"),
    VALLEY("Valley of Triumph"),
    WASTELAND("Golden Wasteland"),
    VAULT("Vault of Knowledge")
}


enum class Areas(val displayName: String, val key: String) {
    PRAIRIE_BUTTERFLY("Butterfly Fields", "prairie.butterfly"),
    PRAIRIE_VILLAGE("Prairie Village", "prairie.village"),
    PRAIRIE_CAVE("Prairie Cave", "prairie.cave"),
    PRAIRIE_BIRD("Bird's Nest", "prairie.bird"),
    PRAIRIE_ISLAND("Sanctuary Island", "prairie.island"),
    FOREST_BROOK("Forest Brook", "forest.brook"),
    FOREST_BONEYARD("Boneyard", "forest.boneyard"),
    FOREST_END("Forest's End", "forest.end"),
    FOREST_TREE("Treehouse", "forest.tree"),
    FOREST_SUNNY("Sunny Forest", "forest.sunny"),
    VALLEY_RINK("Ice Rink", "valley.rink"),
    VALLEY_DREAMS("Village of Dreams", "valley.dreams"),
    VALLEY_HERMIT("Hermit's Valley", "valley.hermit"),
    WASTELAND_TEMPLE("Wasteland Temple", "wasteland.temple"),
    WASTELAND_BATTLEFIELD("Battlefield", "wasteland.battlefield"),
    WASTELAND_GRAVEYARD("Graveyard", "wasteland.graveyard"),
    WASTELAND_CRAB("Crab Fields", "wasteland.crab"),
    WASTELAND_ARK("Forgotten Ark", "wasteland.ark"),
    VAULT_STARLIGHT("Starlight Desert", "vault.starlight"),
    VAULT_JELLY("Jellyfish Cove", "vault.jelly")
}

data class ShardInfo(
    val offset: Duration,
    val noShardDays: List<Int>,
    val rewards: Double? = null,
    val areas: List<Areas>,
)

data class ShardMusic(
    val name: String,
    val spotifyLink: String
)

val shardInfos = listOf(
    // light red shard
    ShardInfo(
        offset = 7.hours + 40.minutes,
        noShardDays = listOf(1, 2),
        areas = listOf(
            Areas.PRAIRIE_CAVE,
            Areas.FOREST_END,
            Areas.VALLEY_DREAMS,
            Areas.WASTELAND_GRAVEYARD,
            Areas.VAULT_JELLY
        ),
        rewards = 2.0
    ),
    // medium
    ShardInfo(
        offset = 2.hours + 20.minutes,
        noShardDays = listOf(2, 3),
        areas = listOf(
            Areas.PRAIRIE_BIRD,
            Areas.FOREST_TREE,
            Areas.VALLEY_DREAMS,
            Areas.WASTELAND_CRAB,
            Areas.VAULT_JELLY
        ),
        rewards = 2.5
    ),
    // strong
    ShardInfo(
        offset = 3.hours + 30.minutes,
        noShardDays = listOf(3, 4),
        areas = listOf(
            Areas.PRAIRIE_ISLAND,
            Areas.FOREST_SUNNY,
            Areas.VALLEY_HERMIT,
            Areas.WASTELAND_ARK,
            Areas.VAULT_JELLY
        ),
        rewards = 3.5
    ),
    // black shards
    ShardInfo(
        offset = 2.hours + 10.minutes,
        noShardDays = listOf(7, 1),
        areas = listOf(
            Areas.PRAIRIE_VILLAGE,
            Areas.FOREST_BONEYARD,
            Areas.VALLEY_RINK,
            Areas.WASTELAND_BATTLEFIELD,
            Areas.VAULT_STARLIGHT
        ),
    ),
    ShardInfo(
        offset = 1.hours + 50.minutes,
        noShardDays = listOf(6, 7),
        areas = listOf(
            Areas.PRAIRIE_BUTTERFLY,
            Areas.FOREST_BROOK,
            Areas.VALLEY_RINK,
            Areas.WASTELAND_TEMPLE,
            Areas.VAULT_STARLIGHT
        ),
    ),
)


val blackShrdInterval = 8.hours
val redShrdInterval = 6.hours

val skyChangeOffset = (-32).minutes + (-10).seconds
val shardLandOffset = 8.minutes + 40.seconds
val shardEndOffset = 4.hours

private const val blackShardMusic = "An Abrupt Premonition"
private const val blackShardMusicLink =
    "https://open.spotify.com/track/11FRruXhXnDJtZUsQyLXjP?si=c3fa48ba2e2e4e6e"

private const val redShardMusic = "Lights Afar"
private const val redShardMusicLink =
    "https://open.spotify.com/track/7jiyGCWrxYnVeXJZihRGFf?si=743089ef433d4983"

private const val mediumShardMusic = "Of The Essence"
private const val mediumShardMusicLink =
    "https://open.spotify.com/track/5Xf6BwbnHfpUhSU7Z7Upqr?si=c58586809d23462f"

data class ShardOccurrence(
    val skyChange: Instant,
    val gateShard: Instant,
    val shardLand: Instant,
    val shardEnd: Instant,
)

data class ShardData(
    val isRed: Boolean,
    val reward: Double? = null,
    val area: Areas,
    val realm: Realm,
    val occurrences: List<ShardOccurrence>,
    val music: ShardMusic
)

val rewardsOverride = mapOf(
    Areas.FOREST_END to 2.0,
    Areas.VALLEY_DREAMS to 2.5,
    Areas.VAULT_JELLY to 3.5,
    Areas.FOREST_TREE to 3.5
)

fun getShard(date: LocalDate): ShardData? {
    val today = date.atStartOfDayIn(GameTimeZone)

    val day = date.day

    // red shards occur on odd days and black on even
    // @see https://github.com/PlutoyDev/sky-shards/blob/production/ShardPredictionRule.md
    // for detailed shard rule
    val isRedShard = (day % 2) == 1
    val realmIndex = (day - 1) % 5

    val infoIndex = if (isRedShard) ((day - 1) / 2) % 3
    else
        3 + ((day - 2) % 4) / 2

    val shard = shardInfos[infoIndex]

    if (shard.noShardDays.contains(date.dayOfWeek.isoDayNumber)) return null

    val area = shard.areas[realmIndex]

    val firstInstant = today + shard.offset

    val occurrences = List(3) { index ->
        // TODO: Remember to handle dst change, for now this is fine
        val offset = firstInstant +
                ((if (isRedShard)
                    redShrdInterval else
                    blackShrdInterval) * index)

        ShardOccurrence(
            skyChange = offset + skyChangeOffset,
            gateShard = offset,
            shardLand = offset + shardLandOffset,
            shardEnd = offset + shardEndOffset,
        )
    }

    return ShardData(
        isRed = isRedShard,
        reward = rewardsOverride[area] ?: shard.rewards,
        occurrences = occurrences,
        area = area,
        realm = Realm.entries[realmIndex],
        music = if (!isRedShard) ShardMusic(blackShardMusic, blackShardMusicLink) else
        // medium shard is at index 2 in info list
        // I agree not the best way to determine this nor is it future-proof, but alas
            if (infoIndex == 2) ShardMusic(mediumShardMusic, mediumShardMusicLink)
            else ShardMusic(redShardMusic, redShardMusicLink)
    )
}