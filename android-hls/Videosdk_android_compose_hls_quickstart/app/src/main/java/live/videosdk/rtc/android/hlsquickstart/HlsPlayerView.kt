package live.videosdk.rtc.android.hlsquickstart

import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

/** Plays the HLS stream at [url]. A new url builds a new player; leaving the screen releases it. */
@Composable
fun HlsPlayerView(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    // A player holds a codec and a socket, so it must be released when this leaves the screen.
    DisposableEffect(player) {
        onDispose { player.release() }
    }

    AndroidView(
        // TextureView, not SurfaceView: a SurfaceView inside Compose gets no compositor layer here
        // and stays blank. TextureView draws inside the app's own window and always shows.
        factory = { TextureView(it) },
        update = { player.setVideoTextureView(it) },
        onRelease = { player.clearVideoSurface() },
        modifier = modifier
    )
}
