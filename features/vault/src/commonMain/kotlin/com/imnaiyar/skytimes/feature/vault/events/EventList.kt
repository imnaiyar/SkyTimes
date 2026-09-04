package com.imnaiyar.skytimes.feature.vault.events

import androidx.compose.runtime.Composable
import com.imnaiyar.skytimes.core.data.Event
import com.imnaiyar.skytimes.feature.vault.common.DateFooterSection
import com.imnaiyar.skytimes.feature.vault.common.DisplayCard
import com.imnaiyar.skytimes.feature.vault.common.ListScaffold

@Composable
internal fun EventList(events: List<Event>, onBack: () -> Unit) {

    val ordered = events.sortedByDescending { it.instances.reversed().first().date }
    ListScaffold(ordered, "Events", onBack) {
        val latestInstance = it.instances.reversed().first()

        DisplayCard(it.name, latestInstance.date.year.toString(), it.imageUrl) {
            DateFooterSection(latestInstance.date, latestInstance.endDate)
        }
    }
}