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

data object Archive : VaultRoutes

@Serializable
data class ListRoute(val category: CategoryList) : VaultRoutes


@Composable
fun EntryProviderScope<NavKey>.vaultEntries(backStack: NavBackStack<NavKey>) {
    val data by LocalSkyDataRepository.current.data.collectAsState()

    val onBack: () -> Unit = { backStack.removeLastOrNull() }

    entry<Archive> {
        MainArchive(data!!, onBack, backStack)
    }

    entry<ListRoute> { cat ->
        when (cat.category) {
            CategoryList.SeasonsList -> SeasonList(data!!.seasons.items.reversed(), onBack)
            CategoryList.EventsList -> EventList(data!!.events.items.reversed(), onBack)
            CategoryList.TravelingSpiritsList -> TravelingSpiritList(
                data!!.travelingSpirits.items.reversed(),
                onBack
            )

            CategoryList.SpecialVisitsList -> TravelingSpiritList(
                data!!.travelingSpirits.items.reversed(),
                onBack
            )
        }
    }
}
