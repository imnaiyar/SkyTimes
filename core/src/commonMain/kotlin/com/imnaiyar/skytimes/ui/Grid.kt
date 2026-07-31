package com.imnaiyar.skytimes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import com.imnaiyar.skytimes.constants.MinFlowRowWidth


enum class GridType {
    GRID,
    STAGGERED
}

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Adaptive(MinFlowRowWidth.dp),
    /** This is only here so that overload is uniques and can be distinguished */
    type: GridType,
    state: LazyGridState = rememberLazyGridState(),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = columns,
        state = state,
        contentPadding = contentPadding,
        userScrollEnabled = userScrollEnabled,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    columns: StaggeredGridCells = StaggeredGridCells.Adaptive(MinFlowRowWidth.dp),
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    userScrollEnabled: Boolean = true,
    content: LazyStaggeredGridScope.() -> Unit,
) {
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        columns = columns,
        state = state,
        contentPadding = contentPadding,
        userScrollEnabled = userScrollEnabled,
        horizontalArrangement = horizontalArrangement,
        verticalItemSpacing = verticalArrangement.spacing
    ) {
        content()
    }
}