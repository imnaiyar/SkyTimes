package com.imnaiyar.skytimes.screens

import VideoPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.onboarding.AppTutorialStep
import com.imnaiyar.skytimes.onboarding.TutorialTarget
import com.imnaiyar.skytimes.repositories.OutdatedQuestException
import com.imnaiyar.skytimes.ui.Card
import com.imnaiyar.skytimes.ui.Grid
import com.imnaiyar.skytimes.ui.LoadingSpinner
import com.imnaiyar.skytimes.ui.RemoteImage
import com.imnaiyar.skytimes.utils.Image
import com.imnaiyar.skytimes.utils.Quest
import com.imnaiyar.skytimes.utils.isTodayInGame
import com.imnaiyar.skytimes.utils.isoDateFormat
import com.imnaiyar.skytimes.views.QuestsUiState
import io.ktor.http.Url
import kotlinx.datetime.format
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestsScreen(
    modifier: Modifier,
    fabPad: PaddingValues,
    tutorialTargetsEnabled: Boolean
) {
    val appContainer = LocalAppContainer.current
    val viewModel = viewModel {
        appContainer.createQuestsViewModel()
    }
    val state by viewModel.state.collectAsState()

    val p2RState = rememberPullToRefreshState()
    val isRefreshing = (state as? QuestsUiState.Content)?.isRefreshing ?: false

    PullToRefreshBox(
        state = p2RState,
        modifier = modifier,
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refresh,
        indicator = {
            TutorialTarget(
                id = AppTutorialStep.QuestPullToRefresh.targetId,
                enabled = tutorialTargetsEnabled,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                PullToRefreshDefaults.LoadingIndicator(
                    state = p2RState,
                    isRefreshing = isRefreshing
                )
            }
        }
    ) {
        when (val current = state) {
            QuestsUiState.Loading -> LoadingSpinner(
                Modifier
                    .padding(fabPad)
                    .verticalScroll(rememberScrollState())
            )

            is QuestsUiState.Error -> QuestError(
                message = current.message,
                onRetry = viewModel::retry,
                modifier = Modifier
                    .padding(fabPad)
                    .verticalScroll(rememberScrollState())
            )

            is QuestsUiState.Content -> {
                if (current.response.quests.none { isTodayInGame(it.date) }) {
                    QuestError(
                        onRetry = viewModel::refresh,
                        message = OutdatedQuestException().message!!,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
                QuestContent(state = current, fabPad)
            }
        }
    }
}

@Composable
private fun QuestContent(
    state: QuestsUiState.Content,
    fabPad: PaddingValues
) {
    val response = state.response
    Grid(contentPadding = fabPad) {
        item {
            if (isTodayInGame(response.rotatingCandles.date)) QuestCard(
                title = "Rotating Candles",
                quest = response.rotatingCandles
            )
        }

        response.seasonalCandles?.let { seasonalCandles ->

            item {
                if (isTodayInGame(seasonalCandles.date)) QuestCard(
                    title = "Seasonal Candles",
                    quest = seasonalCandles
                )
            }
        }

        items(
            items = response.quests,
            key = { quest -> "${quest.date}:${quest.title}" },
        ) { quest ->
            if (isTodayInGame(quest.date)) QuestCard(
                title = quest.title,
                quest = quest,
                showTitleFromQuest = false
            )
        }
    }

}

@OptIn(ExperimentalFlexBoxApi::class)
@Composable
private fun QuestCard(
    title: String,
    quest: Quest,
    showTitleFromQuest: Boolean = true,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
            )

            if (showTitleFromQuest && quest.title != title) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Text(
                text = Instant.parse(quest.date).format(isoDateFormat),
                style = MaterialTheme.typography.labelMedium,
            )

            quest.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (quest.images.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                FlexBox(config = FlexBoxConfig { gap(4.dp) }) {
                    quest.images.forEachIndexed { index, image ->
                        QuestImageCredit(index, image)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestImageCredit(
    index: Int,
    image: Image,
) {
    val credit = remember(image) {
        buildList {
            add("Image ${index + 1}")
            image.by.takeIf { it.isNotBlank() }?.let { add("by $it") }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        credit.forEach { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (image.source != null) {
            val uriHandler = LocalUriHandler.current

            Text(
                text = "Source ↗",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = {
                    uriHandler.openUri(image.source)
                })
            )
        }
        Box(modifier = Modifier.size(120.dp)) {
            if (isVideo(image.url)) {
                VideoPlayer(
                    image.url,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    autoPlay = false
                )
            } else RemoteImage(imageUri = image.url)
        }
    }
}

@Composable
private fun QuestError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}


private val videoExtensions = setOf(
    "mp4", "webm", "mov", "avi", "mkv", "m4v", "3gp", "mpeg", "mpg"
)

private fun isVideo(url: String): Boolean {
    val extension = Url(url).encodedPath
        .substringAfterLast('.', "")
        .lowercase()
        .takeIf { it.isNotEmpty() }

    return extension in videoExtensions
}
