package com.imnaiyar.skytimes.repositories

import com.imnaiyar.skytimes.constants.SkyHelperApi
import com.imnaiyar.skytimes.utils.QuestResponse
import com.imnaiyar.skytimes.utils.isTodayInGame
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class OutdatedQuestException :
    Exception("Quests are not updated yet, please retry after some time.");

class QuestRepository(
    private val storage: Settings = Settings(),
    private val client: HttpClient = HttpClient(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
    },
    private val clock: Clock = Clock.System,
) {
    private val refreshMutex = Mutex()
    private var lastRefreshAttempt: Instant? = null

    suspend fun loadQuests(): QuestLoadResult {
        readValidCache()?.let { cached ->
            return QuestLoadResult.Success(cached, fromCache = true)
        }

        return fetchFresh()
            .fold(
                onSuccess = { QuestLoadResult.Success(it, fromCache = false) },
                onFailure = { QuestLoadResult.Failure(it.userMessage()) }
            )
    }

    suspend fun refreshQuests(): QuestLoadResult {
        if (!refreshMutex.tryLock()) return QuestLoadResult.RefreshSkipped

        return try {
            val now = clock.now()
            val previousAttempt = lastRefreshAttempt
            if (previousAttempt != null && now - previousAttempt < RefreshCooldown) {
                return QuestLoadResult.RefreshSkipped
            }

            lastRefreshAttempt = now
            fetchFresh()
                .fold(
                    onSuccess = { QuestLoadResult.Success(it, fromCache = false) },
                    onFailure = {
                        QuestLoadResult.Failure(
                            message = it.userMessage(),
                            cached = readCachedResponse()
                        )
                    }
                )
        } finally {
            refreshMutex.unlock()
        }
    }

    private suspend fun fetchFresh(): Result<QuestResponse> {

        return runCatching {
            val body = client.get(SkyHelperApi + QuestApiPath).bodyAsText()
            val response = json.decodeFromString<QuestResponse>(body)
            if (!isTodayInGame(response.lastUpdated)) throw OutdatedQuestException()
            storage.putString(QuestCacheKey, body)
            response
        }
    }

    private fun readValidCache(): QuestResponse? {
        val response = readCachedResponse() ?: return null
        if (!isTodayInGame(response.lastUpdated)) {
            storage.remove(QuestCacheKey)
            return null
        }

        return response
    }

    private fun readCachedResponse(): QuestResponse? {
        return storage.getStringOrNull(QuestCacheKey)
            ?.let { cached ->
                runCatching { json.decodeFromString<QuestResponse>(cached) }.getOrNull()
            }
    }


    private fun Throwable.userMessage(): String {
        return when (this) {
            is OutdatedQuestException -> this.message!!
            is SerializationException -> "Quest data could not be read."
            else -> "Unable to load quests. Check your connection and try again."
        }
    }

    private companion object {
        const val QuestApiPath = "/update/quests"
        const val QuestCacheKey = "quests_response_json"
        val RefreshCooldown = 5.seconds
    }
}

sealed interface QuestLoadResult {
    data class Success(
        val response: QuestResponse,
        val fromCache: Boolean,
    ) : QuestLoadResult

    data class Failure(
        val message: String,
        val cached: QuestResponse? = null,
    ) : QuestLoadResult

    data object RefreshSkipped : QuestLoadResult
}
