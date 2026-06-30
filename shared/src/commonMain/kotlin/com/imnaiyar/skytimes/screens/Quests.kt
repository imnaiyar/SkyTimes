package com.imnaiyar.skytimes.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuestsScreen(
    modifier: Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        for (i in 1..20) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Quest $i")
                }
            }
        }
    }
}