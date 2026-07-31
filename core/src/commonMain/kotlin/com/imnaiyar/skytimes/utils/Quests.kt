package com.imnaiyar.skytimes.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestResponse(
    val quests: List<Quest>,
    @SerialName("last_updated")
    val lastUpdated: String,
    @SerialName("last_message")
    val lastMessage: String? = null,
    @SerialName("rotating_candles")
    val rotatingCandles: Quest,
    @SerialName("seasonal_candles")
    val seasonalCandles: Quest? = null,
)

@Serializable
data class Quest(
    val title: String,
    val date: String,
    val description: String? = null,
    val images: List<Image>,
)

@Serializable
data class Image(
    val url: String,
    val by: String,
    val source: String? = null,
)