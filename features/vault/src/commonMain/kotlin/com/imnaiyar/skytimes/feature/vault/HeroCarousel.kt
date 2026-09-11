package com.imnaiyar.skytimes.feature.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.ui.PageIndicator
import com.imnaiyar.skytimes.core.ui.RemoteImage
import com.imnaiyar.skytimes.core.ui.RoundedCorner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

data class HeroCarouselItem(
    val title: String,
    val headerTitle: String,
    val subtitle: String,
    val bgImage: String? = null,
    val onCLick: () -> Unit,
    /** This is for special visits only to get all images of returning spirits */
    val images: List<String>? = null,

    val isTSSection: Boolean = false
)

@Composable
fun HeroCarousel(items: List<HeroCarouselItem>) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()
    // auto slide
    LaunchedEffect(pagerState) {
        while (true) {
            delay(4.seconds)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % items.size)
        }
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val height = when (maxWidth) {
            in 0.dp..600.dp -> 250.dp
            in 600.dp..840.dp -> 350.dp
            else -> 400.dp
        }
        HorizontalPager(
            pagerState,
            modifier = Modifier.fillMaxWidth().height(height).padding(5.dp)
        ) { page ->
            val item = items[page]
            Box(
                Modifier.fillMaxSize()
                    .clip(RoundedCorner)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable(onClick = item.onCLick)
            ) {
                RemoteImage(
                    item.bgImage ?: "",
                    Modifier.matchParentSize(),
                    allowFullScreen = false,
                    contentScale = if (item.isTSSection) ContentScale.Fit else ContentScale.FillBounds,
                    shape = RoundedCorner
                )

                // special visits only ui
                if (item.images != null) {
                    Box(Modifier.matchParentSize().background(Color.Black.copy(0.8f)))

                    Row(
                        Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.images.forEach {
                            RemoteImage(it, Modifier.weight(1f), allowFullScreen = false)
                        }
                    }
                }

                // black gradient
                Box(
                    Modifier.matchParentSize().background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(0.2f),
                                Color.Black
                            )
                        )
                    )
                )

                Column(
                    Modifier.align(Alignment.BottomStart).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        item.headerTitle,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(item.title, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        item.subtitle,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    Box(Modifier.fillMaxWidth()) {
        PageIndicator(
            items.size,
            Modifier.align(Alignment.BottomCenter),
            pagerState.currentPage,
            onClick = { scope.launch { pagerState.animateScrollToPage(it) } }
        )
    }
}