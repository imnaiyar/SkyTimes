package com.imnaiyar.skytimes.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imnaiyar.skytimes.constants.GameTimeZone
import com.imnaiyar.skytimes.constants.SkyHelperCdn
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.ui.AnimatedTimer
import com.imnaiyar.skytimes.ui.Card
import com.imnaiyar.skytimes.ui.ClockDirection
import com.imnaiyar.skytimes.ui.DecoratedText
import com.imnaiyar.skytimes.ui.LiveIndicator
import com.imnaiyar.skytimes.ui.RemoteImage
import com.imnaiyar.skytimes.ui.SlidingIconToggle
import com.imnaiyar.skytimes.ui.Tooltip
import com.imnaiyar.skytimes.utils.ShardData
import com.imnaiyar.skytimes.utils.ShardOccurrence
import com.imnaiyar.skytimes.utils.TimeUtils
import com.imnaiyar.skytimes.utils.getShard
import com.imnaiyar.skytimes.utils.rememberTimeFormatter
import com.imnaiyar.skytimes.utils.toOrdinal
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.ac
import skytimes.shared.generated.resources.data
import skytimes.shared.generated.resources.map
import skytimes.shared.generated.resources.wax
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun ShardsScreen(modifier: Modifier, fabPad: PaddingValues) {
    val appCont = LocalAppContainer.current
    val clockRepository = appCont.clockRepository
    val now = clockRepository.now.collectAsState()
    val shardDate = clockRepository.shardDate.collectAsState()

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
                    clockRepository.setShardDate(date)
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
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShardsPage(date: LocalDate, now: Instant, fabPad: PaddingValues) {
    val shard = getShard(date)
    if (shard == null) {
        NoShardDisplay(Modifier.padding(fabPad), date)
        return
    }
    val upcomingOrActive =
        shard.occurrences.find { occurrence -> occurrence.shardEnd > now }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().padding(fabPad),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.widthIn(max = 600.dp)) {
            Spacer(Modifier.height(15.dp))
            ShardTitle(shard)
            ShardArea(shard)
            Spacer(Modifier.height(30.dp))
            ShardCountdown(upcomingOrActive, now, shard) { showSheet = true }
            Spacer(Modifier.height(30.dp))
            ShardInfographics(shard)
            Spacer(Modifier.height(30.dp))
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

@Composable
@ExperimentalMaterial3Api
private fun ShardBottomSheet(
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

    val activeIndex = shard.occurrences.indexOfFirst { it.shardLand < now && it.shardEnd > now }

    val scope = rememberCoroutineScope()

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
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("$title Shard")
                            if (index == page) {
                                LiveIndicator()
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
                contentPadding = PaddingValues(5.dp)
            ) {
                item {
                    ShardTimeline(
                        shard.occurrences[page],
                        now
                    )
                }
            }
        }
    }
}

@Composable
private fun NoShardDisplay(modifier: Modifier, date: LocalDate) {
    val isToday = date == Clock.System.now().toLocalDateTime(GameTimeZone).date

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "No shards ${
                if (isToday) "today" else "on ${
                    date.format(LocalDate.Format {
                        monthName(MonthNames.ENGLISH_ABBREVIATED)
                        char(' ')
                        day()
                        chars(", ")
                        year()
                    })
                }"
            }!", color = MaterialTheme.colorScheme.onErrorContainer
        )
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

    val timeUtils = rememberTimeFormatter()

    Text(
        "Shard Timelines",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(15.dp)
    )

    FlowRow() {
        repeat(4) { index ->
            val (title, dur) = list.elementAt(index)

            Card(
                modifier = Modifier.widthIn(min = 150.dp).weight(1f),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.labelMedium)


                    Spacer(Modifier.height(5.dp))
                    HorizontalDivider()

                    Spacer(Modifier.height(10.dp))

                    Tooltip(
                        "${
                            timeUtils.format(
                                dur,
                                GameTimeZone
                            )
                        } in Los Angeles (Game's Timezone)"
                    ) {
                        DecoratedText(
                            text = timeUtils.format(dur),
                            textDecoration =
                                if (now > dur) TextDecoration.LineThrough
                                else TextDecoration.None,
                            color = if (now > dur) LocalContentColor.current.copy(0.5f)
                            else LocalContentColor.current,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ShardTitle(shard: ShardData) {
    val iconId = "rewardIcon"

    Text(
        text = buildAnnotatedString {
            withStyle(
                MaterialTheme.typography.titleLargeEmphasized.toSpanStyle()
                    .copy(
                        if (shard.isRed) Color.Red else Color.Black,
                        shadow = Shadow(
                            LocalContentColor.current,
                            blurRadius = 1f,
                            offset = Offset.VisibilityThreshold
                        )
                    )
            ) {
                append(
                    if (shard.isRed) "Red Shard" else "Black Shard"
                )
            }
            append("  ")
            append("(Reward: ")
            withStyle(MaterialTheme.typography.bodySmall.toSpanStyle()) {
                append((if (shard.isRed) shard.reward!! else 200.0).toString())
            }
            appendInlineContent(iconId, "[icon]")
            append(")")
        },
        style = MaterialTheme.typography.bodySmall,
        inlineContent = mapOf(
            iconId to InlineTextContent(
                Placeholder(
                    18.sp,
                    18.sp,
                    PlaceholderVerticalAlign.Center
                )
            ) {
                Image(
                    painterResource(if (shard.isRed) Res.drawable.ac else Res.drawable.wax),
                    contentDescription = if (shard.isRed) "Ascended Candle" else "Wax",
                    modifier = Modifier.size(20.dp)
                )
            }
        )
    )
}


@Composable
private fun ShardArea(shard: ShardData) {

    Text(
        text = buildAnnotatedString {
            append("at ")
            withStyle(
                MaterialTheme.typography.labelMedium.toSpanStyle()
                    .copy(color = MaterialTheme.colorScheme.primary)
            ) { append(shard.area.displayName) }
            append(" in ")
            withStyle(
                MaterialTheme.typography.labelMedium.toSpanStyle()
                    .copy(color = MaterialTheme.colorScheme.primary)
            ) { append(shard.realm.displayName) }
        },
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
private fun ShardCountdown(
    occurrence: ShardOccurrence?,
    now: Instant,
    shard: ShardData,
    onClick: () -> Unit
) {
    var timer: Long
    var timerHeader: String
    var timerSubtitle: String

    val formatter = rememberTimeFormatter()



    occurrence?.let {
        val shardIndex = (shard.occurrences.indexOf(it) + 1).toOrdinal()

        if (it.shardLand > now) {
            timer = (it.shardLand - now).inWholeMilliseconds
            timerHeader = "$shardIndex Shard Lands in"
            timerSubtitle = "At: " + formatter.format(it.shardLand)
        } else {
            timer = (it.shardEnd - now).inWholeMilliseconds
            timerHeader = "$shardIndex Shard Ends in"
            timerSubtitle = "At: " + formatter.format(it.shardEnd)
        }
    } ?: run {
        // means all shards has ended
        val lastShard = shard.occurrences.last()
        timer = (lastShard.shardEnd - now).inWholeMilliseconds
        timerHeader = "All shards ended"

        timerSubtitle = "Ago"
    }

    Box(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                timerHeader,
                style = MaterialTheme.typography.labelLarge
            )

            AnimatedTimer(
                TimeUtils().formatMillis(abs(timer)),
                size = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.primary,
                direction = if (timer > 0) ClockDirection.DOWN else ClockDirection.UP
            )

            Text(timerSubtitle, style = MaterialTheme.typography.labelLarge)
        }
    }
}


@Composable
fun ShardInfographics(shard: ShardData) {
    // Internal state used only if the caller doesn't hoist it themselves.
    var isFlipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    val getInfographicsUrl =
        { type: String -> SkyHelperCdn + "/shards/${type.lowercase()}/${shard.area.key}.png" }

    val getImageDisplay = @Composable { type: String ->
        val url = getInfographicsUrl(type)
        RemoteImage(
            url,
            contentDescription = "Shard $type",
            modifier = Modifier.size(300.dp)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            buildAnnotatedString {
                append(if (!isFlipped) "Shard Location" else "Shard Data")
                append(" ")
                withStyle(
                    MaterialTheme.typography.labelMedium.toSpanStyle()
                        .copy(LocalContentColor.current.copy(0.5f))
                ) {
                    if (isFlipped) append("(By Gale)") else append("(By clement)")
                }
            },
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.animateContentSize()
        )

        SlidingIconToggle(
            icons = listOf(Res.drawable.map, Res.drawable.data),
            selectedIndex = if (isFlipped) 1 else 0,
            itemSize = 30.dp,
            usehaptics = true,
            roundedCornerIndicator = RoundedCornerShape(8.dp),
            onSelectedChange = { isFlipped = it == 1 }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            getImageDisplay("Location")
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                contentAlignment = Alignment.Center
            ) {
                getImageDisplay("Data")
            }
        }
    }
}