package com.imnaiyar.skytimes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Adaptive(minSize = 360.dp),
    state: LazyGridState = rememberLazyGridState(),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.End,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = columns,
        state = state,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}