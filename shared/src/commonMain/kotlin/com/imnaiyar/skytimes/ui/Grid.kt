package com.imnaiyar.skytimes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


private val minWidth = 400.dp

enum class GridType {
    GRID,
    STAGGERED
}

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Adaptive(minWidth),
    type: GridType,
    state: LazyGridState = rememberLazyGridState(),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.End,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = columns,
        state = state,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    type: GridType = GridType.STAGGERED,
    columns: StaggeredGridCells = StaggeredGridCells.Adaptive(minWidth),
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.End,
    content: LazyStaggeredGridScope.() -> Unit,
) {
    LazyVerticalStaggeredGrid(
        modifier = modifier.fillMaxSize(),
        columns = columns,
        state = state,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}