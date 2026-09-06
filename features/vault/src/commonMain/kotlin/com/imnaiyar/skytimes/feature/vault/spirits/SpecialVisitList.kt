package com.imnaiyar.skytimes.feature.vault.spirits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.data.SpecialVisit
import com.imnaiyar.skytimes.core.ui.Card
import com.imnaiyar.skytimes.core.ui.DecoratedText
import com.imnaiyar.skytimes.core.ui.RemoteImage
import com.imnaiyar.skytimes.core.ui.RoundedCorner
import com.imnaiyar.skytimes.core.ui.RoundedCornerBottom
import com.imnaiyar.skytimes.core.ui.RoundedCornerTop
import com.imnaiyar.skytimes.core.ui.generated.resources.Res
import com.imnaiyar.skytimes.core.ui.generated.resources.map
import com.imnaiyar.skytimes.core.ui.generated.resources.person
import com.imnaiyar.skytimes.core.ui.theme.labelTiny
import com.imnaiyar.skytimes.feature.vault.common.DateFooterSection
import com.imnaiyar.skytimes.feature.vault.common.FooterSection
import com.imnaiyar.skytimes.feature.vault.common.ListScaffold

@Composable
internal fun SpecialVisitList(
    visits: List<SpecialVisit>,
    onBack: () -> Unit,
    onSpiritClick: (String) -> Unit = {},
    onVisitClick: (String) -> Unit = {},
    onAreaClick: (String) -> Unit = {}
) {
    ListScaffold(visits, "Special Visits", onBack) { visit ->
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCorner)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(onClick = { onVisitClick(visit.guid) })
        ) {
            Box(Modifier.fillMaxWidth()) {
                // area image as bg
                RemoteImage(
                    visit.area?.imageUrl ?: "",
                    modifier = Modifier.matchParentSize().blur(2.dp),
                    shape = RoundedCornerTop,
                    allowFullScreen = false,
                    contentScale = ContentScale.FillBounds
                )

                // scrim alpha
                Box(Modifier.matchParentSize().background(Color.Black.copy(0.6f)))

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
                            Column(
                                Modifier.requiredWidth(80.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .requiredSize(80.dp)
                                        .clip(RoundedCorner)
                                        .background(
                                            MaterialTheme.colorScheme.secondaryFixed.copy(
                                                0.2f
                                            )
                                        )
                                        .clickable { spirit?.guid?.let { onSpiritClick(it) } }
                                ) {
                                    RemoteImage(
                                        spirit?.imageUrl ?: "",
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        contentScale = ContentScale.Fit,
                                        allowFullScreen = false
                                    )
                                }

                                Text(
                                    spirit?.name ?: "Unknown Spirit",
                                    style = MaterialTheme.typography.labelTiny,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // info
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
                    if (visit.area != null) {
                        FooterSection(Res.drawable.map) {
                            DecoratedText(
                                "Area: ${visit.area!!.name}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.clickable(onClick = { onAreaClick(visit.area!!.guid) })
                            )
                        }
                    }

                    FooterSection("Total Spirits: ${visit.spirits.size}", Res.drawable.person)

                    DateFooterSection(visit.date, visit.endDate)
                }
            }
        }
    }
}
