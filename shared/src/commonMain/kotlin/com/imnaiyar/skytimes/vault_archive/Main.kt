package com.imnaiyar.skytimes.vault_archive

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun MainArchive() {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = "This will be the Vault Archive in It's Glory",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}