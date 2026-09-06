package com.imnaiyar.skytimes.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.imnaiyar.skytimes.core.ui.generated.resources.Res
import com.imnaiyar.skytimes.core.ui.generated.resources.chevron_right
import org.jetbrains.compose.resources.painterResource

@Composable
fun ScrollToTop(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.fillMaxWidth().then(modifier).height(60.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(250, easing = FastOutLinearInEasing)
            ) + fadeOut(tween(150)),
        ) {
            SmallFloatingActionButton(
                onClick = onClick,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Icon(
                    painterResource(Res.drawable.chevron_right),
                    modifier = Modifier.rotate(-90f),
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Scroll to top"
                )
            }
        }
    }
}


// extensions to get the positions the scroll button should be visible from in a scrollable composable
@Composable
fun LazyListState.showScrollToTop(threshold: Int = 300): Boolean {
    return remember {
        derivedStateOf {
            firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > threshold
        }
    }.value
}

@Composable
fun LazyGridState.showScrollToTop(threshold: Int = 300): Boolean {
    return remember {
        derivedStateOf {
            firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > threshold
        }
    }.value
}

@Composable
fun LazyStaggeredGridState.showScrollToTop(threshold: Int = 300): Boolean {
    return remember {
        derivedStateOf {
            firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > threshold
        }
    }.value
}

@Composable
fun ScrollState.showScrollToTop(threshold: Int = 300): Boolean {
    return remember {
        derivedStateOf { value > threshold }
    }.value
}