package com.imnaiyar.skytimes.feature.vault.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.imnaiyar.skytimes.core.data.LocalSkyDataRepository
import com.imnaiyar.skytimes.feature.vault.MainArchive
import com.imnaiyar.skytimes.feature.vault.events.EventList
import com.imnaiyar.skytimes.feature.vault.seasons.SeasonList
import com.imnaiyar.skytimes.feature.vault.spirits.SpecialVisitList
import com.imnaiyar.skytimes.feature.vault.spirits.TravelingSpiritList
import kotlinx.serialization.Serializable

enum class CategoryList {
    SeasonsList,
    EventsList,
    TravelingSpiritsList,
    SpecialVisitsList
}

@Serializable
sealed interface VaultRoutes : NavKey

@Serializable
data object Archive : VaultRoutes

@Serializable
data class ListRoute(val category: CategoryList) : VaultRoutes


@Composable
fun EntryProviderScope<NavKey>.vaultEntries(backStack: NavBackStack<NavKey>) {
    val repository = LocalSkyDataRepository.current
    val data by repository.data.collectAsState()
    val error by repository.error.collectAsState()

    val onBack: () -> Unit = { backStack.removeLastOrNull() }
    val onRefresh: suspend () -> Result<*> = {
        repository.refresh(forceRefresh = true)
    }

    entry<Archive> {
        MainArchive(data, error, onRefresh, onBack, backStack)
    }

    entry<ListRoute> { cat ->
        data?.let { currentData ->
            when (cat.category) {
                CategoryList.SeasonsList -> SeasonList(currentData.seasons.items.reversed(), onBack)
                CategoryList.EventsList -> EventList(currentData.events.items.reversed(), onBack)
                CategoryList.TravelingSpiritsList -> TravelingSpiritList(
                    currentData.travelingSpirits.items.reversed(),
                    onBack
                )

                CategoryList.SpecialVisitsList -> SpecialVisitList(
                    currentData.specialVisits.items.reversed(),
                    onBack
                )
            }
        } ?: MainArchive(null, error, onRefresh, onBack, backStack)
    }
}
