package com.imnaiyar.skytimes.feature.home.shards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.domain.getShard
import com.imnaiyar.skytimes.core.navigation.AppTutorialStep
import com.imnaiyar.skytimes.core.onboarding.TutorialTarget
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShardsPage(
    date: LocalDate,
    now: Instant,
    fabPad: PaddingValues,
    tutorialTargetsEnabled: Boolean,
    isSelectedPage: Boolean
) {
    val shard = getShard(date)
    if (shard == null) {
        NoShardDisplay(Modifier.padding(fabPad), date)
        return
    }
    val upcomingOrActive =
        shard.occurrences.find { occurrence -> occurrence.shardEnd > now }
    val sheetState =
        rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().padding(fabPad),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.widthIn(max = 600.dp)) {
            Spacer(Modifier.height(15.dp))
            ShardTitle(shard)
            Spacer(Modifier.height(30.dp))
            TutorialTarget(
                id = AppTutorialStep.ShardCountdown.targetId,
                enabled = tutorialTargetsEnabled && isSelectedPage
            ) {
                ShardCountdown(upcomingOrActive, now, shard) { showSheet = true }
            }
            Spacer(Modifier.height(30.dp))
            ShardInfographics(shard)
            Spacer(Modifier.height(30.dp))
        }

        // The pager itself fills the screen, which makes a poor coach-mark anchor.
        // A one-pixel center anchor keeps the swipe tutorial's tooltip on screen.
        TutorialTarget(
            id = AppTutorialStep.ShardDateSwipe.targetId,
            modifier = Modifier.size(1.dp),
            enabled = tutorialTargetsEnabled && isSelectedPage
        ) {
            Box(Modifier.size(1.dp))
        }
    }

    if (showSheet) {
        ShardBottomSheet(
            shard,
            shard.occurrences.indexOf(upcomingOrActive).let { if (it == -1) 0 else it },
            sheetState,
            now,
        ) {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    showSheet = false
                }
            }
        }
    }
}