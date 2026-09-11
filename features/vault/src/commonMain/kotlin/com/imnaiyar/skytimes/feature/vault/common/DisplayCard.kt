package com.imnaiyar.skytimes.feature.vault.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.common.LocalSnackBarState
import com.imnaiyar.skytimes.core.common.localDateToIso
import com.imnaiyar.skytimes.core.domain.GameTimeZone
import com.imnaiyar.skytimes.core.ui.BackScaffold
import com.imnaiyar.skytimes.core.ui.Card
import com.imnaiyar.skytimes.core.ui.Grid
import com.imnaiyar.skytimes.core.ui.RemoteImage
import com.imnaiyar.skytimes.core.ui.RoundedCorner
import com.imnaiyar.skytimes.core.ui.RoundedCornerBottom
import com.imnaiyar.skytimes.core.ui.RoundedCornerTop
import com.imnaiyar.skytimes.core.ui.ScrollToTop
import com.imnaiyar.skytimes.core.ui.SnackBarHostLocal
import com.imnaiyar.skytimes.core.ui.generated.resources.Res
import com.imnaiyar.skytimes.core.ui.generated.resources.calendar
import com.imnaiyar.skytimes.core.ui.showScrollToTop
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock

// TODO: Remove snackbar once features are implemented
@Composable
fun <T> ListScaffold(
    itemList: List<T>,
    title: String,
    onBack: () -> Unit,
    content: @Composable (T) -> Unit
) {
    val state = rememberLazyStaggeredGridState()
    val scope = rememberCoroutineScope()

    BackScaffold(title, onBack, bottomBar = {
        ScrollToTop(
            state.showScrollToTop(),
            modifier = Modifier,
            onClick = { scope.launch { state.animateScrollToItem(0) } })
    }, snackBarHost = { SnackBarHostLocal() }) {
        Grid(state = state, contentPadding = it + PaddingValues(5.dp)) {
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
    scrimAlpha: Float = 0.9f,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    val imageHeight = 200.dp
    val scope = rememberCoroutineScope()
    val toast = LocalSnackBarState.current
    Box(
        Modifier.fillMaxWidth().clip(RoundedCorner)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = {
                scope.launch {
                    toast.showSnackbar(
                        "This feature is under development",
                        withDismissAction = true
                    )
                }
            })
    ) {

        Column {
            RemoteImage(
                imageUrl ?: "",
                allowFullScreen = false,
                modifier = Modifier.fillMaxWidth().height(imageHeight),
                contentScale = imageScale,
                shape = RoundedCornerTop
            )

            if (footer != null) Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerBottom,
                border = null
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(10.dp)
                ) { footer() }
            }
        }

        // title over a black scrim
        Box(
            Modifier.matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = scrimAlpha),
                            Color.Black.copy(0.2f),
                            Color.Transparent,
                            Color.Transparent, // fully faded by 35% of the height
                        )

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
    }
}


@Composable
internal fun FooterSection(text: String, icon: DrawableResource? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) Icon(
            painterResource(icon),
            contentDescription = "Icon",
            modifier = Modifier.size(12.dp)
        )
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
internal fun FooterSection(icon: DrawableResource? = null, content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) Icon(
            painterResource(icon),
            contentDescription = "Icon",
            modifier = Modifier.size(12.dp)
        )
        content()
    }
}

@Composable
internal fun DateFooterSection(dateStart: LocalDate, dateEnd: LocalDate? = null) {
    var dateLabel = dateStart.format(localDateToIso)

    if (dateEnd != null) {
        dateLabel += " \u279e ${dateEnd.format(localDateToIso)}"

        val nowDate = Clock.System.now().toLocalDateTime(GameTimeZone).date

        dateLabel += if (nowDate > dateEnd) {
            " (ended)"
        } else {
            " (ends in ${nowDate.daysUntil(dateEnd)}d)"
        }
    }

    FooterSection(dateLabel, Res.drawable.calendar)
}