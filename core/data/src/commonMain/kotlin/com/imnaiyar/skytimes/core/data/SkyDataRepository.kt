package com.imnaiyar.skytimes.core.data

import androidx.compose.runtime.staticCompositionLocalOf
import com.imnaiyar.skytimes.core.common.StartupTask
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant


val LocalSkyDataRepository = staticCompositionLocalOf<SkyDataRepository> {
    error("No SkyDataRepository provided")
}

class SkyDataRepository(
    private val storage: Settings = Settings(),
    private val client: HttpClient = HttpClient(),
    private val clock: Clock = Clock.System,
    private val resolver: SkyDataResolver = SkyDataResolver(),
) : StartupTask {
    override val name = "SkyGame data"
    override val critical = false

    private val refreshMutex = Mutex()
    private val _data = MutableStateFlow<SkyData?>(null)
    val data: StateFlow<SkyData?> = _data.asStateFlow()

    override suspend fun initialize() {
        refresh().getOrThrow()
    }

    suspend fun refresh(forceRefresh: Boolean = false): Result<SkyData> = refreshMutex.withLock {
        val now = clock.now()
        val cached = readCache()
        if (!forceRefresh && cached != null && now - cached.timestamp < CacheLifetime) {
            _data.value = cached.data
            return@withLock Result.success(cached.data)
        }

        val result = runCatching {
            val response = client.get(CdnUrl)
            if (!response.status.isSuccess()) {
                error("SkyGame data request failed with HTTP ${response.status.value}")
            }
            val body = response.bodyAsText()
            val resolved = resolver.resolve(body)
            storage.putString(CacheJsonKey, body)
            storage.putLong(CacheTimestampKey, now.toEpochMilliseconds())
            _data.value = resolved
            resolved
        }

        result.exceptionOrNull()?.let { failure ->
            cached?.let { _data.value = it.data }
        }
        return@withLock result
    }

    private fun readCache(): CachedData? {
        val body = storage.getStringOrNull(CacheJsonKey) ?: return null
        val timestamp = storage.getLong(CacheTimestampKey, 0L)
        if (timestamp <= 0L) return null
        return runCatching {
            CachedData(resolver.resolve(body), Instant.fromEpochMilliseconds(timestamp))
        }.getOrNull()
    }

    private data class CachedData(val data: SkyData, val timestamp: Instant)

    private companion object {
        const val CdnUrl = "https://unpkg.com/skygame-data@latest/assets/everything.json"
        const val CacheJsonKey = "skygame_data_raw_json"
        const val CacheTimestampKey = "skygame_data_fetched_at"
        val CacheLifetime = 1.days
    }
}
