package com.imnaiyar.skytimes.feature.quests

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel

/** The quests repository, provided by the app root. */
val LocalQuestRepository = staticCompositionLocalOf<QuestRepository> {
    error("No QuestRepository provided")
}

/** Creates (or reuses) the [QuestsViewModel] for the quests tab. */
@Composable
fun rememberQuestsViewModel(): QuestsViewModel {
    val repository = LocalQuestRepository.current
    return viewModel { QuestsViewModel(repository) }
}
