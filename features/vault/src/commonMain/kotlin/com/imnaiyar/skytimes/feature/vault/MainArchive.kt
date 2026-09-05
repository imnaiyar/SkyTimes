package com.imnaiyar.skytimes.feature.vault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.imnaiyar.skytimes.core.data.SkyData
import com.imnaiyar.skytimes.core.navigation.navigateTo
import com.imnaiyar.skytimes.core.ui.BackScaffold
import com.imnaiyar.skytimes.feature.vault.nav.CategoryList
import com.imnaiyar.skytimes.feature.vault.nav.ListRoute

@Composable
fun MainArchive(skyData: SkyData, onNavigateBack: () -> Unit, navStack: NavBackStack<NavKey>) {
    Box(contentAlignment = Alignment.Center) {

        if (skyData == null) return@Box Text(
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
                        skyData.seasons.items.reversed().map { data ->
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
                    CarouselSection("Events", skyData.events.items.reversed().map { data ->
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
                        skyData.travelingSpirits.items.reversed().map { data ->
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
                        skyData.specialVisits.items.reversed().map { data ->
                            CarouselItemType.CarouselSpecialVisit(data)
                        }) { navStack.navigateTo(ListRoute(CategoryList.SpecialVisitsList)) }
                }
            }
        }
    }
}