package com.imnaiyar.skytimes.repositories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.imnaiyar.skytimes.constants.GameTimeZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

class ClockRepository(
    scope: CoroutineScope
) {
    private val _now = MutableStateFlow(Clock.System.now())
    private val _shardDate = MutableStateFlow(Clock.System.now().toLocalDateTime(GameTimeZone).date)
    val shardDate = _shardDate
    val now: StateFlow<Instant> = _now

    init {
        scope.launch {
            while (isActive) {
                val instant = Clock.System.now()
                _now.value = instant

                delay(
                    timeMillis = 1000 - (instant.toEpochMilliseconds() % 1000)
                )
            }
        }
    }

    /**
     * Lets you observe the current ticker with custom transformation
     * This is useful when suppose we need ticker where it is updated every minute or day
     * instead of every second
     */
    fun <T> observe(
        transform: (Instant) -> T
    ): Flow<T> =
        now
            .map(transform)
            .distinctUntilChanged()

    @Composable
    fun observeEveryMinute(): State<Instant> =
        observe {
            Instant.fromEpochMilliseconds(
                (it.toEpochMilliseconds() / 60_000) * 60_000
            )
        }.collectAsState(Clock.System.now())

    @Composable
    fun observeDate(gameTimeZone: Boolean = true): State<LocalDate> {
        val timeZone = if (gameTimeZone) GameTimeZone else TimeZone.currentSystemDefault()
        return observe {
            it.toLocalDateTime(timeZone).date
        }.collectAsState(Clock.System.now().toLocalDateTime(timeZone).date)
    }

    fun setShardDate(date: LocalDate) {
        _shardDate.value = date
    }
}