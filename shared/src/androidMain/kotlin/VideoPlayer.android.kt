import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    controller: VideoPlayerController,
    autoPlay: Boolean
) {
    val context = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = autoPlay
        }
    }

    LaunchedEffect(player) {
        controller.playImpl = { player.play() }
        controller.pauseImpl = { player.pause() }
    }


    DisposableEffect(player) {
        onDispose {
            controller.playImpl = null
            controller.pauseImpl = null
            player.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(it).apply {
                this.player = player
            }
        }
    )
}