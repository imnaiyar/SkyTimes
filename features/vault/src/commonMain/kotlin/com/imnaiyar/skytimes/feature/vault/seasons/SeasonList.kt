package com.imnaiyar.skytimes.feature.vault.seasons

import androidx.compose.runtime.Composable
import com.imnaiyar.skytimes.core.data.Season
import com.imnaiyar.skytimes.core.ui.generated.resources.Res
import com.imnaiyar.skytimes.core.ui.generated.resources.person
import com.imnaiyar.skytimes.feature.vault.common.DateFooterSection
import com.imnaiyar.skytimes.feature.vault.common.DisplayCard
import com.imnaiyar.skytimes.feature.vault.common.FooterSection
import com.imnaiyar.skytimes.feature.vault.common.ListScaffold

@Composable
fun SeasonList(seasons: List<Season>, onBack: () -> Unit) {
    ListScaffold(seasons, "Seasons", onBack) {
        DisplayCard(
            it.name,
            it.date.year.toString(),
            it.imageUrl,
        ) {
            FooterSection("Spirits: ${it.spirits.size}", Res.drawable.person)

            DateFooterSection(it.date, it.endDate)
        }
    }
}

