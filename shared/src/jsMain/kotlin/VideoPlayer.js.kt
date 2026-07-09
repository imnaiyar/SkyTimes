import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.HtmlElementView
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    controller: VideoPlayerController,
    autoPlay: Boolean
) {
    val player = remember {
        (document.createElement("video") as HTMLVideoElement).apply {
            controls = true
            autoplay = autoPlay
            src = url
            setAttribute("playsinline", "true")

            style.width = "100%"
            style.height = "100%"
            style.objectFit = "contain"
            style.backgroundColor = "black"
        }
    }

    LaunchedEffect(player) {
        controller.playImpl = { player.play() }
        controller.pauseImpl = { player.pause() }
    }

    DisposableEffect(Unit) {
        document.body?.appendChild(player)
        onDispose {
            controller.playImpl = null
            controller.pauseImpl = null
            player.pause()
            player.removeAttribute("src")
            player.load()
            document.body?.removeChild(player)

        }
    }

    HtmlElementView(
        modifier = modifier,
        factory = {
            player
        },
        update = { video ->
            if (video.src != url) {
                video.src = url
            }

            video.autoplay = autoPlay
        }
    )
}