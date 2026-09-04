package com.imnaiyar.skytimes.feature.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.data.SpecialVisit
import com.imnaiyar.skytimes.core.ui.RemoteImage
import com.imnaiyar.skytimes.core.ui.RoundedCorner
import com.imnaiyar.skytimes.core.ui.Tooltip
import com.imnaiyar.skytimes.core.ui.generated.resources.Res
import com.imnaiyar.skytimes.core.ui.generated.resources.chevron_right
import com.imnaiyar.skytimes.core.ui.theme.labelTiny
import org.jetbrains.compose.resources.painterResource


sealed interface CarouselItemType {
    val onClick: () -> Unit

    data class CarouselSectionItems(
        val label: String,
        val shortName: String,
        val image: String?,
        override val onClick: () -> Unit = {},
        val imageScale: ContentScale = ContentScale.Crop
    ) : CarouselItemType

    data class CarouselSpecialVisit(
        val visit: SpecialVisit,
        override val onClick: () -> Unit = {}
    ) :
        CarouselItemType
}

@Composable
internal fun CarouselSection(
    title: String,
    items: List<CarouselItemType>,
    onCategoryClick: () -> Unit = {}
) {
    val state = rememberCarouselState { items.size }
    Column {
        Row(
            modifier = Modifier.clickable(onClick = onCategoryClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 12.dp)
            )

            Icon(
                painterResource(Res.drawable.chevron_right),
                modifier = Modifier.size(15.dp),
                contentDescription = "Chevron"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalMultiBrowseCarousel(
            state,
            150.dp,
            flingBehavior = CarouselDefaults.noSnapFlingBehavior(),
            itemSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { i ->
            val item = items[i]

            val label = when (item) {
                is CarouselItemType.CarouselSectionItems -> item.label
                is CarouselItemType.CarouselSpecialVisit -> item.visit.name ?: "Unknown Visit"
            }

            Column(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().requiredHeight(150.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCorner
                        )
                        .clickable(onClick = item.onClick),
                    contentAlignment = Alignment.Center
                ) {
                    when (item) {
                        is CarouselItemType.CarouselSectionItems ->
                            if (item.image == null) Text(
                                item.shortName,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.labelSmall
                            )
                            else
                                RemoteImage(
                                    item.image,
                                    allowFullScreen = false,
                                    contentScale = item.imageScale
                                )

                        is CarouselItemType.CarouselSpecialVisit -> SpecialVisitSection(item.visit)
                    }
                }


                Text(label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun SpecialVisitSection(visit: SpecialVisit) {

    FlowRow(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(visit.spirits.size) { index ->
            val spirit = visit.spirits[index]
            val boxSize = if (visit.spirits.size > 4) 40.dp else 54.dp
            Box(
                modifier = Modifier.requiredSize(boxSize)
                    .background(
                        MaterialTheme.colorScheme.secondaryFixed.copy(0.3f),
                        RoundedCorner
                    )
            ) {
                if (spirit.spirit?.imageUrl !== null) Tooltip(spirit.spirit!!.name) {
                    RemoteImage(
                        spirit.spirit!!.imageUrl!!,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit,
                        allowFullScreen = false
                    )
                }
                else Text("?", style = MaterialTheme.typography.labelTiny)
            }
        }
    }
}