package com.imnaiyar.skytimes.feature.vault.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.imnaiyar.skytimes.core.data.LocalSkyDataRepository
import com.imnaiyar.skytimes.feature.vault.events.EventList
import com.imnaiyar.skytimes.feature.vault.seasons.SeasonList
import com.imnaiyar.skytimes.feature.vault.spirits.TravelingSpiritList
import kotlinx.serialization.Serializable

enum class CategoryList {
    Seasons,
    Events,
    TravelingSpirit,
    SpecialVisit
}

@Serializable
sealed interface VaultRoutes : NavKey

@Serializable
data object SeasonsRoute : VaultRoutes

@Serializable
data object EventsRoute : VaultRoutes

@Serializable
data object TravelingSpiritsRoute : VaultRoutes

@Serializable
data object SpecialVisitsRoute : VaultRoutes


@Composable
fun EntryProviderScope<NavKey>.vaultEntries(backStack: NavBackStack<NavKey>) {
    val data by LocalSkyDataRepository.current.data.collectAsState()

    val onBack: () -> Unit = { backStack.removeLastOrNull() }
    entry<SeasonsRoute> {
        SeasonList(data!!.seasons.items.reversed(), onBack)
    }

    entry<EventsRoute> {
        EventList(data!!.events.items.reversed(), onBack)
    }

    entry<TravelingSpiritsRoute> {
        TravelingSpiritList(data!!.travelingSpirits.items.reversed(), onBack)
    }

    // TODO: this is temp to prevent crash, revert after implementing special visit lists
    entry<SpecialVisitsRoute> {
        TravelingSpiritList(data!!.travelingSpirits.items.reversed(), onBack)
    }
}
