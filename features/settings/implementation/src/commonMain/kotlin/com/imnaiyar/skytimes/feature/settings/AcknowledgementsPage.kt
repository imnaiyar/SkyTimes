package com.imnaiyar.skytimes.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.ui.BackScaffold
import com.imnaiyar.skytimes.core.ui.Card
import com.imnaiyar.skytimes.core.ui.Grid

@Composable
fun AcknowledgementsPage(onNavigateBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    BackScaffold(
        title = "Acknowledgements",
        onNavigateBack = onNavigateBack
    ) { padding ->
        Grid(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                    "SkyTimes is made possible by the community projects, guides, data, and resources below.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                CreditCard(
                    title = "Shard pattern",
                    description = "Shard are calculated using a pattern determined by Plutoy and community members Galerowfylery#1310, RandomZhii#4275, ChristianKingFu#8986, kion_Anzu#1021, Unmuted Hucker#6095, and LN🦇#5792.",
                    linkText = "ShardPredictionRule.md by Plutoy",
                    onLinkClick = {
                        uriHandler.openUri("https://github.com/PlutoyDev/sky-shards/blob/production/ShardPredictionRule.md")
                    }
                )
            }

            item {
                CreditCard(
                    title = "Shard guides and data",
                    description = "The shard location guide is provided by Clement, and shard data is provided by Gale. Shard music by ChristianKingFu."
                )
            }

            item {
                CreditCard(
                    title = "Quest guides",
                    description = "Quest guides are created by Clement, io, AL, and other creators, shared in the Sky: CoTL Infographics Database Discord server.",
                    linkText = "Sky: CoTL Infographics Database Discord",
                    onLinkClick = {
                        uriHandler.openUri("https://discord.gg/skyinfographicsdatabase")
                    }
                )
            }

            item {
                CreditCard(
                    title = "Vault Archive",
                    description = "The Vault Archive is powered by skygame-data by Silverfeelin.",
                    linkText = "View skygame-data on GitHub",
                    onLinkClick = {
                        uriHandler.openUri("https://github.com/silverfeelin/skygame-data")
                    }
                )
            }

            item {
                CreditCard(
                    title = "Images, icons, and other resources",
                    description = "Icons, images, and other resources in skygame-data are taken from SkyWiki.",
                    linkText = "Visit SkyWiki",
                    onLinkClick = {
                        uriHandler.openUri("https://sky-children-of-the-light.fandom.com/wiki/Sky:_Children_of_the_Light_Wiki")
                    }
                )
            }
        }
    }
}

@Composable
private fun CreditCard(
    title: String,
    description: String,
    linkText: String? = null,
    onLinkClick: (() -> Unit)? = null
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            if (linkText != null && onLinkClick != null) {
                Text(
                    text = linkText,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onLinkClick)
                )
            }
        }
    }
}