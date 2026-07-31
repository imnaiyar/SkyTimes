import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    controller: VideoPlayerController = VideoPlayerController(),
    autoPlay: Boolean = true
)

class VideoPlayerController {

    internal var playImpl: (() -> Unit)? = null
    internal var pauseImpl: (() -> Unit)? = null

    fun play() {
        playImpl?.invoke()
    }

    fun pause() {
        pauseImpl?.invoke()
    }
}