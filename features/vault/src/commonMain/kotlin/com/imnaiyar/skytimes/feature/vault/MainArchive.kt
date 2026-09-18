package com.imnaiyar.skytimes.feature.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.imnaiyar.skytimes.core.common.LocalSnackBarState
import com.imnaiyar.skytimes.core.data.SkyData
import com.imnaiyar.skytimes.core.navigation.navigateTo
import com.imnaiyar.skytimes.core.ui.BackScaffold
import com.imnaiyar.skytimes.core.ui.Callout
import com.imnaiyar.skytimes.core.ui.CalloutType
import com.imnaiyar.skytimes.core.ui.SnackBarHostLocal
import com.imnaiyar.skytimes.feature.vault.common.SearchBar
import com.imnaiyar.skytimes.feature.vault.nav.CategoryList
import com.imnaiyar.skytimes.feature.vault.nav.ListRoute
import kotlinx.coroutines.launch

@Composable
fun MainArchive(
    skyData: SkyData?,
    loadError: Throwable?,
    onRefresh: suspend () -> Result<*>,
    onNavigateBack: () -> Unit,
    navStack: NavBackStack<NavKey>
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val refresh: () -> Unit = {
        if (!isRefreshing) {
            scope.launch {
                isRefreshing = true
                try {
                    onRefresh()
                } finally {
                    isRefreshing = false
                }
            }
        }
    }

    if (skyData == null) {
        ArchiveFallback(loadError, isRefreshing, refresh, onNavigateBack)
        return
    }

    MainArchiveContent(skyData, loadError, isRefreshing, refresh, onNavigateBack, navStack)
}

@Composable
private fun ArchiveFallback(
    loadError: Throwable?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackScaffold(
        "Vault Archive",
        onNavigateBack,
        snackBarHost = { SnackBarHostLocal() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Archive data is unavailable",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.padding(4.dp))
            Text(
                loadError?.message ?: "SkyGame data could not be loaded.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.padding(8.dp))
            RefreshButton(isRefreshing, onRefresh)
            if (loadError != null) {
                ErrorDetails(loadError)
            }
        }
    }
}

@Composable
private fun MainArchiveContent(
    skyData: SkyData,
    loadError: Throwable?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    navStack: NavBackStack<NavKey>
) {
    var query by remember { mutableStateOf("") }

    val snackBarState = LocalSnackBarState.current
    val scope = rememberCoroutineScope()

    val inDevelopmentToast: () -> Unit =
        {
            scope.launch {
                snackBarState.showSnackbar(
                    "This feature is still in development.",
                    withDismissAction = true
                )
            }
        }

    val activeItems = remember(skyData) {
        val seasons = skyData.seasons.items.filter { it.isActive() }
        val events = skyData.eventInstances.items.filter { it.isActive() }
        val travelingSpirits = skyData.travelingSpirits.items.filter { it.isActive() }
        val specialVisit = skyData.specialVisits.items.filter { it.isActive() }
        listOf(
            seasons.map {
                HeroCarouselItem(
                    it.name,
                    "Season",
                    "Ends in ${it.remainingDays()} days",
                    it.imageUrl,
                    inDevelopmentToast
                )
            },

            events.map {
                HeroCarouselItem(
                    it.event!!.name,
                    "Event",
                    "Ends in ${it.remainingDays()} days",
                    it.event!!.imageUrl,
                    inDevelopmentToast
                )
            },
            travelingSpirits.map {
                HeroCarouselItem(
                    (it.spirit?.name ?: "Unknown Spirit") + " (#${it.number})",
                    "Traveling Spirit \u2022 TS #${it.number}",
                    "Ends in ${it.remainingDays()} days",
                    it.spirit?.imageUrl,
                    inDevelopmentToast,
                    isTSSection = true
                )
            },

            specialVisit.map {
                HeroCarouselItem(
                    it.name ?: "Unknown Visit",
                    "Special Visit",
                    "Ends in ${it.remainingDays()} days",
                    it.area?.imageUrl,
                    inDevelopmentToast,
                    it.spirits.map { s -> s.spirit?.imageUrl ?: "" }
                )
            }

        ).flatten()
    }

    val filteredSeasons = remember(query, skyData) {
        if (query.isBlank()) skyData.seasons.items
        else skyData.seasons.items.filter {
            it.name.contains(query, ignoreCase = true) || it.shortName.contains(
                query,
                ignoreCase = true
            )
        }
    }

    val filteredEvents = remember(query, skyData) {
        if (query.isBlank()) skyData.events.items
        else skyData.events.items.filter {
            it.name.contains(query, ignoreCase = true) || it.shortName?.contains(
                query,
                ignoreCase = true
            ) == true
        }
    }

    val filteredTravelingSpirits = remember(query, skyData) {
        if (query.isBlank()) skyData.travelingSpirits.items
        else skyData.travelingSpirits.items.filter {
            it.spirit?.name?.contains(query, ignoreCase = true) == true
        }
    }

    val filteredSpecialVisits = remember(query, skyData) {
        if (query.isBlank()) skyData.specialVisits.items
        else skyData.specialVisits.items.filter {
            it.name?.contains(query, ignoreCase = true) == true
        }
    }

    val isAllEmpty = filteredSeasons.isEmpty() && filteredEvents.isEmpty() &&
            filteredTravelingSpirits.isEmpty() && filteredSpecialVisits.isEmpty()

    Box(contentAlignment = Alignment.Center) {
        BackScaffold("Vault Archive", onNavigateBack, snackBarHost = { SnackBarHostLocal() }) {
            LazyColumn(
                contentPadding = it,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.imePadding()
            ) {
                if (loadError != null) {
                    item {
                        Callout(
                            text = loadError.message ?: "SkyGame data refresh failed.",
                            type = CalloutType.ERROR,
                            title = "Using cached archive data"
                        )
                        RefreshButton(isRefreshing, onRefresh)
                    }
                }
                item { SearchBar(query) { q -> query = q } }

                if (isAllEmpty && query.isNotBlank()) return@LazyColumn item {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No results found", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                if (query.isBlank() && activeItems.isNotEmpty()) {
                    item {
                        Text(
                            "Active Now (${activeItems.size})",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        HeroCarousel(activeItems)
                    }
                }

                // Season
                if (filteredSeasons.isNotEmpty()) {
                    item {
                        CarouselSection(
                            "Seasons",
                            filteredSeasons.reversed().map { data ->
                                CarouselItemType.CarouselSectionItems(
                                    data.name,
                                    data.shortName,
                                    data.imageUrl,
                                    inDevelopmentToast
                                )
                            }
                        ) { navStack.navigateTo(ListRoute(CategoryList.SeasonsList)) }
                    }
                }

                // events
                if (filteredEvents.isNotEmpty()) {
                    item {
                        CarouselSection("Events", filteredEvents.reversed().map { data ->
                            CarouselItemType.CarouselSectionItems(
                                data.name,
                                data.shortName ?: data.name.replace("Days of ", ""),
                                data.imageUrl,
                                inDevelopmentToast
                            )
                        }) { navStack.navigateTo(ListRoute(CategoryList.EventsList)) }
                    }
                }

                // traveling spirit
                if (filteredTravelingSpirits.isNotEmpty()) {
                    item {
                        CarouselSection(
                            "Traveling Spirits",
                            filteredTravelingSpirits.reversed().map { data ->
                                CarouselItemType.CarouselSectionItems(
                                    (data.spirit?.name ?: "Unknown Spirit") + " (#${data.number})",
                                    data.spirit?.name ?: "Unknown",
                                    data.spirit?.imageUrl,
                                    inDevelopmentToast,
                                    imageScale = ContentScale.Fit,
                                )
                            }) { navStack.navigateTo(ListRoute(CategoryList.TravelingSpiritsList)) }
                    }
                }

                // Special visit
                if (filteredSpecialVisits.isNotEmpty()) {
                    item {
                        CarouselSection(
                            "Special Visits",
                            filteredSpecialVisits.reversed().map { data ->
                                CarouselItemType.CarouselSpecialVisit(data, inDevelopmentToast)
                            }) { navStack.navigateTo(ListRoute(CategoryList.SpecialVisitsList)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RefreshButton(isRefreshing: Boolean, onRefresh: () -> Unit) {
    Button(onClick = onRefresh, enabled = !isRefreshing) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(if (isRefreshing) "Refreshing..." else "Refresh")
        }
    }
}

@Composable
private fun ErrorDetails(error: Throwable) {
    var showDetails by remember { mutableStateOf(false) }

    TextButton(onClick = { showDetails = !showDetails }) {
        Text(if (showDetails) "Hide error details" else "Show error details")
    }

    AnimatedVisibility(showDetails) {
        Text(
            error.stackTraceToString(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }
}