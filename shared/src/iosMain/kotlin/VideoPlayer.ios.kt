import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL

@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    controller: VideoPlayerController,
    autoPlay: Boolean
) {
    val player = remember {
        AVPlayer(uRL = NSURL.URLWithString(url)!!)
    }

    LaunchedEffect(player) {
        controller.playImpl = { player.play() }
        controller.pauseImpl = { player.pause() }
    }

    DisposableEffect(player) {
        if (autoPlay) {
            player.play()
        }
        onDispose {
            controller.playImpl = null
            controller.pauseImpl = null
            player.pause()
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            AVPlayerViewController().apply {
                this.player = player
            }.view
        }
    )
}