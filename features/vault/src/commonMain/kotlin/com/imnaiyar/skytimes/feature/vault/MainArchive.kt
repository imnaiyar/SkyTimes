package com.imnaiyar.skytimes.feature.vault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.data.LocalSkyDataRepository
import com.imnaiyar.skytimes.core.ui.BackScaffold

@Composable
fun MainArchive(onNavigateBack: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        val skydata by LocalSkyDataRepository.current.data.collectAsState()

        if (skydata == null) return@Box Text(
            "Loading...",
            style = MaterialTheme.typography.displayLarge
        )


        BackScaffold("Vault Archive", onNavigateBack) {
            LazyColumn(
                contentPadding = it,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Season
                item {
                    CarouselSection(
                        "Seasons",
                        skydata!!.seasons.items.reversed().map { data ->
                            CarouselSectionItems(
                                data.name,
                                data.shortName,
                                data.imageUrl
                            )
                        })
                }

                // events
                item {
                    CarouselSection("Events", skydata!!.events.items.reversed().map { data ->
                        CarouselSectionItems(
                            data.name,
                            data.shortName ?: data.name.replace("Days of ", ""),
                            data.imageUrl
                        )
                    })
                }

                // traveling spirit
                item {
                    CarouselSection(
                        "Traveling Spirits",
                        skydata!!.travelingSpirits.items.reversed().map { data ->
                            CarouselSectionItems(
                                (data.spirit?.name ?: "Unknown Spirit") + " (#${data.number})",
                                data.spirit?.name ?: "Unknown",
                                data.spirit?.imageUrl
                            )
                        })
                }

                // Special visit
                // TODO: this placed like other for now, but this should show all sv spirits, in a card
                item {
                    CarouselSection(
                        "Traveling Spirits",
                        skydata!!.specialVisits.items.reversed().map { data ->
                            CarouselSectionItems(
                                data.name ?: "Special Visit",
                                "SV",
                                null
                            )
                        })
                }
            }
        }
    }
}