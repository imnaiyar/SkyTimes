package com.imnaiyar.skytimes.feature.vault.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.ui.RoundedCorner


@Composable
internal fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search") },
        placeholder = { Text("Search Archive") },
        shape = RoundedCorner,
        modifier = Modifier.fillMaxWidth().padding(5.dp)
    )
}
