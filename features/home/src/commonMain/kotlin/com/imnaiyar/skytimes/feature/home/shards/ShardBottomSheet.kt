package com.imnaiyar.skytimes.feature.home.shards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.domain.ShardData
import com.imnaiyar.skytimes.core.domain.ShardOccurrence
import com.imnaiyar.skytimes.core.ui.Card
import com.imnaiyar.skytimes.core.ui.DecoratedText
import com.imnaiyar.skytimes.core.ui.TimeDisplay
import com.imnaiyar.skytimes.core.ui.Tooltip
import com.imnaiyar.skytimes.core.ui.animated.LiveIndicator
import com.imnaiyar.skytimes.core.ui.generated.resources.Res
import com.imnaiyar.skytimes.core.ui.generated.resources.open_in_browser
import com.imnaiyar.skytimes.core.ui.theme.titleTiny
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Instant

@Composable
@ExperimentalMaterial3Api
fun ShardBottomSheet(
    shard: ShardData,
    index: Int,
    sheetState: SheetState,
    now: Instant,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = index,
        pageCount = { 3 }
    )

    // get any shard that is currently active, meaning has landed but not ended yet
    val activeIndex = shard.occurrences.indexOfFirst { it.shardLand < now && it.shardEnd > now }

    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        tonalElevation = 5.dp,
        sheetState = sheetState,
    ) {
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.padding(5.dp),
            containerColor = Color.Unspecified
        ) {
            listOf("1st", "2nd", "3rd").forEachIndexed { page, title ->
                Tab(
                    selected = pagerState.currentPage == page,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    },
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("$title Shard")
                            if (activeIndex == page) {
                                LiveIndicator(size = 8.dp)
                            }
                        }
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            overscrollEffect = null
        ) { page ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                contentPadding = PaddingValues(15.dp)
            ) {
                // timeline
                item {
                    ShardTimeline(
                        shard.occurrences[page],
                        now
                    )
                }

                // music
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Shard Music: ", style = MaterialTheme.typography.titleTiny())
                        Tooltip(
                            "Open this in Spotify",
                            tooltipPosition = TooltipAnchorPosition.Above,
                            showOnClick = false
                        ) {
                            DecoratedText(
                                shard.music.name,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.clickable(onClick = {
                                    uriHandler.openUri(shard.music.spotifyLink)
                                })
                            )

                        }
                        Spacer(modifier = Modifier.size(5.dp))
                        Icon(
                            painterResource(Res.drawable.open_in_browser),
                            contentDescription = "Open in Spotify",
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalGridApi::class)
@Composable
private fun ShardTimeline(occurrence: ShardOccurrence, now: Instant) {
    val list = mapOf(
        "Early Sky Change" to occurrence.skyChange,
        "Gate Shard" to occurrence.gateShard,
        "Shard Lands" to occurrence.shardLand,
        "Shard Ends" to occurrence.shardEnd
    ).entries

    Text(
        "Shard Timelines",
        style = MaterialTheme.typography.titleTiny(),
    )

    FlowRow() {
        repeat(4) { index ->
            val (title, dur) = list.elementAt(index)

            Card(
                modifier = Modifier.padding(5.dp).widthIn(min = 150.dp).weight(1f),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelMedium,
                        // highlight shard landing and ending as they are the most relevant
                        color = if (index == 2 || index == 3)
                            MaterialTheme.colorScheme.primary
                        else LocalContentColor.current
                    )


                    Spacer(Modifier.height(5.dp))
                    HorizontalDivider()

                    Spacer(Modifier.height(10.dp))

                    TimeDisplay(dur, now)
                }
            }
        }
    }
}