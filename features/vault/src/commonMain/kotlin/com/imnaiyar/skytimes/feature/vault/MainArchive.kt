package com.imnaiyar.skytimes.feature.vault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.imnaiyar.skytimes.core.data.SkyData
import com.imnaiyar.skytimes.core.navigation.navigateTo
import com.imnaiyar.skytimes.core.ui.BackScaffold
import com.imnaiyar.skytimes.feature.vault.common.SearchBar
import com.imnaiyar.skytimes.feature.vault.nav.CategoryList
import com.imnaiyar.skytimes.feature.vault.nav.ListRoute

@Composable
fun MainArchive(skyData: SkyData, onNavigateBack: () -> Unit, navStack: NavBackStack<NavKey>) {
    var query by remember { mutableStateOf("") }

    val activeItems = remember(skyData) {
        val seasons = skyData.seasons.items.filter { it.isActive() }
        val events = skyData.eventInstances.items.filter { it.isActive() }.map { it.event!! }
        val travelingSpirits = skyData.travelingSpirits.items.filter { it.isActive() }
        val specialVisits = skyData.specialVisits.items.filter { it.isActive() }

        listOf(
            seasons.map {
                CarouselItemType.CarouselSectionItems(
                    it.name,
                    it.shortName,
                    it.imageUrl
                )
            },
            events.map {
                CarouselItemType.CarouselSectionItems(
                    it.name,
                    it.shortName ?: it.name.replace("Days of ", ""),
                    it.imageUrl
                )
            },
            travelingSpirits.map {
                CarouselItemType.CarouselSectionItems(
                    (it.spirit?.name ?: "Unknown Spirit") + " (#${it.number})",
                    it.spirit?.name ?: "Unknown",
                    it.spirit?.imageUrl,
                )
            },
            specialVisits.map {
                CarouselItemType.CarouselSpecialVisit(it)
            }
        ).flatten()
    }


    val filteredSeasons = remember(query) {
        if (query.isBlank()) skyData.seasons.items
        else skyData.seasons.items.filter {
            it.name.contains(query, ignoreCase = true) || it.shortName.contains(
                query,
                ignoreCase = true
            )
        }
    }

    val filteredEvents = remember(query) {
        if (query.isBlank()) skyData.events.items
        else skyData.events.items.filter {
            it.name.contains(query, ignoreCase = true) || it.shortName?.contains(
                query,
                ignoreCase = true
            ) == true
        }
    }

    val filteredTravelingSpirits = remember(query) {
        if (query.isBlank()) skyData.travelingSpirits.items
        else skyData.travelingSpirits.items.filter {
            it.spirit?.name?.contains(query, ignoreCase = true) == true
        }
    }

    val filteredSpecialVisits = remember(query) {
        if (query.isBlank()) skyData.specialVisits.items
        else skyData.specialVisits.items.filter {
            it.name?.contains(query, ignoreCase = true) == true
        }
    }
    val isAllEmpty = filteredSeasons.isEmpty() && filteredEvents.isEmpty() &&
            filteredTravelingSpirits.isEmpty() && filteredSpecialVisits.isEmpty()

    Box(contentAlignment = Alignment.Center) {
        BackScaffold("Vault Archive", onNavigateBack) {
            LazyColumn(
                contentPadding = it,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { SearchBar(query) { q -> query = q } }

                if (isAllEmpty && query.isNotBlank()) return@LazyColumn item {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No results found", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                // TODO: use slideshow type of courousel for this one
                if (query.isBlank() && activeItems.isNotEmpty()) {
                    item {
                        CarouselSection("Currently Active", activeItems, true)
                    }
                }

                // Season
                item {
                    CarouselSection(
                        "Seasons",
                        filteredSeasons.reversed().map { data ->
                            CarouselItemType.CarouselSectionItems(
                                data.name,
                                data.shortName,
                                data.imageUrl,
                            )
                        }
                    ) { navStack.navigateTo(ListRoute(CategoryList.SeasonsList)) }
                }

                // events
                item {
                    CarouselSection("Events", filteredEvents.reversed().map { data ->
                        CarouselItemType.CarouselSectionItems(
                            data.name,
                            data.shortName ?: data.name.replace("Days of ", ""),
                            data.imageUrl
                        )
                    }) { navStack.navigateTo(ListRoute(CategoryList.EventsList)) }
                }

                // traveling spirit
                item {
                    CarouselSection(
                        "Traveling Spirits",
                        filteredTravelingSpirits.reversed().map { data ->
                            CarouselItemType.CarouselSectionItems(
                                (data.spirit?.name ?: "Unknown Spirit") + " (#${data.number})",
                                data.spirit?.name ?: "Unknown",
                                data.spirit?.imageUrl,
                                imageScale = ContentScale.Fit
                            )
                        }) { navStack.navigateTo(ListRoute(CategoryList.TravelingSpiritsList)) }
                }

                // Special visit
                item {
                    CarouselSection(
                        "Special Visits",
                        filteredSpecialVisits.reversed().map { data ->
                            CarouselItemType.CarouselSpecialVisit(data)
                        }) { navStack.navigateTo(ListRoute(CategoryList.SpecialVisitsList)) }
                }
            }
        }
    }
}