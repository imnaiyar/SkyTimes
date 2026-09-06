package com.imnaiyar.skytimes.feature.vault.spirits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.data.SpecialVisit
import com.imnaiyar.skytimes.core.ui.Card
import com.imnaiyar.skytimes.core.ui.DecoratedText
import com.imnaiyar.skytimes.core.ui.RemoteImage
import com.imnaiyar.skytimes.core.ui.RoundedCorner
import com.imnaiyar.skytimes.core.ui.RoundedCornerBottom
import com.imnaiyar.skytimes.core.ui.Tooltip
import com.imnaiyar.skytimes.feature.vault.common.DateFooterSection
import com.imnaiyar.skytimes.feature.vault.common.ListScaffold

@Composable
internal fun SpecialVisitList(
    visits: List<SpecialVisit>,
    onBack: () -> Unit,
    onSpiritClick: (String) -> Unit = {},
    onVisitClick: (String) -> Unit = {}
) {
    ListScaffold(visits, "Special Visits", onBack) { visit ->
        Box(
            Modifier.fillMaxWidth()
                .clip(RoundedCorner)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(onClick = { onVisitClick(visit.guid) })
        ) {
            // area image as bg
            RemoteImage(
                visit.area?.imageUrl ?: "",
                modifier = Modifier.matchParentSize(),
                allowFullScreen = false,
                contentScale = ContentScale.FillBounds
            )

            // scrim alpha
            Box(Modifier.matchParentSize().background(Color.Black.copy(0.8f), RoundedCorner))

            Column(Modifier.fillMaxWidth()) {// title area
                Column(Modifier.padding(10.dp)) {
                    Text(
                        visit.date.year.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        visit.name ?: "Special Visit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // spirits row
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visit.spirits.forEach { visitSpirit ->
                        val spirit = visitSpirit.spirit
                        Box(
                            modifier = Modifier
                                .requiredSize(80.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondaryFixed.copy(0.1f),
                                    RoundedCorner
                                )
                                .clickable { spirit?.guid?.let { onSpiritClick(it) } }
                        ) {
                            if (spirit?.imageUrl != null) {
                                Tooltip(spirit.name) {
                                    RemoteImage(
                                        spirit.imageUrl!!,
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        contentScale = ContentScale.Fit,
                                        allowFullScreen = false
                                    )
                                }
                            } else {
                                Text(
                                    "?",
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                Card(
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(0.8f),
                    border = null,
                    shape = RoundedCornerBottom
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        DecoratedText(
                            "Details",
                            Modifier.clickable(onClick = {}),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            "Area: ${visit.area?.name ?: "Unknown"}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            "Total Spirits: ${visit.spirits.size}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        DateFooterSection(visit.date, visit.endDate)
                    }
                }
            }
        }
    }
}
