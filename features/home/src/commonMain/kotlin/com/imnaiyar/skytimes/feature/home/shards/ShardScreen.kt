package com.imnaiyar.skytimes.feature.home.shards

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.imnaiyar.skytimes.core.data.LocalClockRepository
import com.imnaiyar.skytimes.core.domain.getShard
import com.imnaiyar.skytimes.core.navigation.AppTutorialStep
import kotlinx.datetime.LocalDate

@Composable
fun ShardScreen(
    modifier: Modifier,
    fabPad: PaddingValues,
    tutorialTargetsEnabled: Boolean,
    activeTutorialStep: AppTutorialStep?
) {
    val shardDateState = LocalShardDate.current
    val clockRepository = LocalClockRepository.current
    val now = clockRepository.now.collectAsState()
    val shardDate = shardDateState.shardDate.collectAsState()
    // TODO: This probably is not needed for swipe and datepicker action
    val isShardTutorialActive = activeTutorialStep == AppTutorialStep.ShardCountdown ||
            activeTutorialStep == AppTutorialStep.ShardDateSwipe
    var tutorialOriginalShardDate by remember { mutableStateOf<LocalDate?>(null) }

    // Countdown is unavailable on no shard day. Use the next
    // available date during the tour, then leave the user's original choice intact.
    LaunchedEffect(isShardTutorialActive) {
        if (isShardTutorialActive && getShard(shardDate.value) == null) {
            tutorialOriginalShardDate = shardDate.value
            val nextShardDate = (1..7)
                .asSequence()
                .map { offset -> LocalDate.fromEpochDays(shardDate.value.toEpochDays() + offset) }
                .firstOrNull { date -> getShard(date) != null }
            nextShardDate?.let(shardDateState::setShardDate)
        } else if (!isShardTutorialActive) {
            tutorialOriginalShardDate?.let(shardDateState::setShardDate)
            tutorialOriginalShardDate = null
        }
    }

    val centerPage = Int.MAX_VALUE / 2
    val anchorDate = remember { shardDate.value }

    fun dateForPage(page: Int): LocalDate =
        LocalDate.fromEpochDays(anchorDate.toEpochDays() + (page - centerPage))

    fun pageForDate(date: LocalDate): Int =
        centerPage + (date.toEpochDays() - anchorDate.toEpochDays()).toInt()

    val pagerState = rememberPagerState(initialPage = centerPage) { Int.MAX_VALUE }

    // update page when shard date changes (from header date picker)
    LaunchedEffect(shardDate.value) {
        val targetPage = pageForDate(shardDate.value)
        if (targetPage != pagerState.currentPage && targetPage != pagerState.targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // update date when page change
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                val date = dateForPage(page)
                if (date != shardDate.value) {
                    shardDateState.setShardDate(date)
                }
            }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
    ) { page ->
        ShardsPage(
            date = dateForPage(page),
            now = now.value,
            fabPad = fabPad,
            tutorialTargetsEnabled = tutorialTargetsEnabled,
            isSelectedPage = page == pagerState.settledPage
        )
    }
}