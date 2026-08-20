package com.imnaiyar.skytimes.feature.home.shards

import androidx.compose.runtime.staticCompositionLocalOf
import com.imnaiyar.skytimes.core.domain.GameTimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * The selected shard date. Owned by the app root (provided through
 * [LocalShardDate]) so the selection survives navigating away from the
 * home screen.
 */
class ShardDateState {
    private val _shardDate =
        MutableStateFlow(Clock.System.now().toLocalDateTime(GameTimeZone).date)

    val shardDate: StateFlow<LocalDate> = _shardDate.asStateFlow()

    fun setShardDate(date: LocalDate) {
        _shardDate.value = date
    }
}

val LocalShardDate = staticCompositionLocalOf<ShardDateState> {
    error("No shard date state provided")
}