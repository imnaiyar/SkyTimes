package com.imnaiyar.skytimes.feature.home.shards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.imnaiyar.skytimes.core.domain.GameTimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun NoShardDisplay(modifier: Modifier, date: LocalDate) {
    val isToday = date == Clock.System.now().toLocalDateTime(GameTimeZone).date

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "No shards ${
                if (isToday) "today" else "on ${
                    date.format(LocalDate.Format {
                        monthName(MonthNames.ENGLISH_ABBREVIATED)
                        char(' ')
                        day()
                        chars(", ")
                        year()
                    })
                }"
            }!", color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}