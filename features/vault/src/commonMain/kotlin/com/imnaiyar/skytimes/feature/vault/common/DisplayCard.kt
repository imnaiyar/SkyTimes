package com.imnaiyar.skytimes.feature.vault.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.common.localDateToIso
import com.imnaiyar.skytimes.core.domain.GameTimeZone
import com.imnaiyar.skytimes.core.ui.BackScaffold
import com.imnaiyar.skytimes.core.ui.Grid
import com.imnaiyar.skytimes.core.ui.RemoteImage
import com.imnaiyar.skytimes.core.ui.RoundedCorner
import com.imnaiyar.skytimes.core.ui.generated.resources.Res
import com.imnaiyar.skytimes.core.ui.generated.resources.calendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock

@Composable
fun <T> ListScaffold(
    itemList: List<T>,
    title: String,
    onBack: () -> Unit,
    content: @Composable (T) -> Unit
) {
    BackScaffold(title, onBack) {
        Grid(contentPadding = it + PaddingValues(5.dp)) {
            items(itemList.size) { i ->
                val item = itemList[i]

                content(item)
            }
        }
    }
}

@Composable
internal fun DisplayCard(
    title: String,
    titleHeader: String? = null,
    imageUrl: String? = null,
    imageScale: ContentScale = ContentScale.FillBounds,
    scrimAlpha: Float = 1f,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    Box(
        Modifier.fillMaxWidth().height(250.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCorner)
    ) {
        RemoteImage(
            imageUrl ?: "",
            allowFullScreen = false,
            modifier = Modifier.matchParentSize(),
            contentScale = imageScale
        )
        Box(
            Modifier.matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(scrimAlpha), Color.Transparent)
                    ),
                    RoundedCorner
                ),
            contentAlignment = Alignment.TopStart
        ) {
            Column(Modifier.padding(10.dp)) {
                if (titleHeader != null) Text(
                    titleHeader,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (footer != null) Box(
            Modifier.align(Alignment.BottomStart).fillMaxWidth().background(
                MaterialTheme.colorScheme.surfaceContainer.copy(0.9f),
                RoundedCorner.copy(topStart = CornerSize(0f), topEnd = CornerSize(0f))
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(10.dp)
            ) { footer() }
        }
    }
}


@Composable
internal fun DateFooterSection(dateStart: LocalDate, dateEnd: LocalDate? = null) {
    var dateLabel = dateStart.format(localDateToIso)

    if (dateEnd != null) {
        dateLabel += " --> ${dateEnd.format(localDateToIso)}"

        val nowDate = Clock.System.now().toLocalDateTime(GameTimeZone).date

        dateLabel += if (nowDate > dateEnd) {
            " (ended)"
        } else {
            " (ends in ${nowDate.daysUntil(dateEnd)}d)"
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painterResource(Res.drawable.calendar),
            contentDescription = "Calendar",
            modifier = Modifier.size(12.dp)
        )

        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}