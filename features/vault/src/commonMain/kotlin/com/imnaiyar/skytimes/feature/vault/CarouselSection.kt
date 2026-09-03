package com.imnaiyar.skytimes.feature.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
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
import com.imnaiyar.skytimes.core.ui.RemoteImage
import com.imnaiyar.skytimes.core.ui.RoundedCorner
import com.imnaiyar.skytimes.core.ui.generated.resources.Res
import com.imnaiyar.skytimes.core.ui.generated.resources.chevron_right
import org.jetbrains.compose.resources.painterResource

data class CarouselSectionItems(
    val label: String,
    val shortName: String,
    val image: String?,
    val onClick: () -> Unit = {}
)

@Composable
fun CarouselSection(
    title: String,
    items: List<CarouselSectionItems>,
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

            Column(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (item.image == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().requiredHeight(150.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCorner
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.shortName,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else
                    RemoteImage(
                        item.image,
                        modifier = Modifier.requiredHeight(150.dp)
                            .clickable(onClick = item.onClick),
                        allowFullScreen = false,
                        contentScale = ContentScale.Crop
                    )

                Text(item.label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}