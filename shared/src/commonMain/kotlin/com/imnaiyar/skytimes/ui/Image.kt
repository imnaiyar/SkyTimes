package com.imnaiyar.skytimes.ui

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState
import com.imnaiyar.skytimes.constants.RoundedCorner
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun RemoteImage(
    imageUri: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    allowFullScreen: Boolean = true,
) {
    var isFullScreen by remember { mutableStateOf(false) }

    SubcomposeAsyncImage(
        model = imageUri,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
            .clickable { isFullScreen = true }
            .clip(RoundedCorner),
        loading = { LoadingSpinner() }
    )

    if (isFullScreen && allowFullScreen) {
        FullScreenImageOverlay(
            imageUri = imageUri,
            contentDescription = contentDescription,
            onDismiss = { isFullScreen = false },
        )
    }
}

@Composable
private fun FullScreenImageOverlay(
    imageUri: String,
    contentDescription: String?,
    onDismiss: () -> Unit,
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val alpha = (1f - (abs(offsetY) / 500f))
        .coerceIn(0.2f, 1f)
    val zoomState = rememberCoilZoomState()
    val canDrag = zoomState.zoomable.transform.scaleX <= 1.01f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Surface(
            color = Color.Black.copy(alpha = alpha),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .draggable(
                        enabled = canDrag,
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            offsetY += delta
                        },
                        onDragStopped = {
                            if (abs(offsetY) > 800f) {
                                onDismiss()
                            } else {
                                scope.launch {
                                    animate(
                                        initialValue = offsetY,
                                        targetValue = 0f
                                    ) { value, _ ->
                                        offsetY = value
                                    }
                                }
                            }
                        }
                    )) {
                CoilZoomAsyncImage(
                    model = imageUri,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Fit,
                    zoomState = zoomState,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(0, offsetY.roundToInt()) }
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.4f)),
                ) {
                    Text("✕", color = Color.White)
                }
            }
        }
    }
}