package com.imnaiyar.skytimes.feature.vault.seasons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.imnaiyar.skytimes.core.data.Season
import com.imnaiyar.skytimes.feature.vault.common.DateFooterSection
import com.imnaiyar.skytimes.feature.vault.common.DisplayCard
import com.imnaiyar.skytimes.feature.vault.common.ListScaffold

@Composable
fun SeasonList(seasons: List<Season>, onBack: () -> Unit) {
    ListScaffold(seasons, "Seasons", onBack) {
        DisplayCard(
            it.name,
            it.date.year.toString(),
            it.imageUrl,
        ) {
            Text(
                "Spirits: ${it.spirits.size}",
                style = MaterialTheme.typography.labelSmall
            )
            DateFooterSection(it.date, it.endDate)
        }
    }
}

