package com.imnaiyar.skytimes.feature.vault.spirits

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import com.imnaiyar.skytimes.core.data.TravelingSpirit
import com.imnaiyar.skytimes.feature.vault.common.DateFooterSection
import com.imnaiyar.skytimes.feature.vault.common.DisplayCard
import com.imnaiyar.skytimes.feature.vault.common.ListScaffold

@Composable
internal fun TravelingSpiritList(spirits: List<TravelingSpirit>, onBack: () -> Unit) {
    val ordered = spirits.sortedByDescending { it.date }

    ListScaffold(ordered, "Traveling Spirits", onBack) {
        DisplayCard(
            it.spirit!!.name,
            it.date.year.toString() + " \u2022 " + "TS #${it.number}",
            it.spirit!!.imageUrl,
            ContentScale.Fit,
            0.5f
        ) {
            Text("Total Visits: ${it.visit}", style = MaterialTheme.typography.labelSmall)
            DateFooterSection(it.date, it.endDate)
        }
    }
}
