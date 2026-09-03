package live.videosdk.rtc.android.quickstart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.Stream
import live.videosdk.rtc.android.VideoView
import live.videosdk.rtc.android.listeners.ParticipantEventListener
import org.webrtc.VideoTrack

@Composable
fun ParticipantsGrid(participants: List<Participant>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(8.dp)
    ) {
        // Keyed by participant id so a tile is never reused for someone else.
        items(participants, key = { it.id }) { participant ->
            ParticipantVideoView(participant)
        }
    }
}

@Composable
fun ParticipantVideoView(participant: Participant) {
    var videoTrack by remember(participant) { mutableStateOf(participant.videoTrack()) }

    // One listener per tile: follows the participant's camera on/off.
    DisposableEffect(participant) {
        val listener = object : ParticipantEventListener() {
            override fun onStreamEnabled(stream: Stream) {
                if (stream.kind == "video") videoTrack = stream.track as VideoTrack
            }

            override fun onStreamDisabled(stream: Stream) {
                if (stream.kind == "video") videoTrack = null
            }
        }
        participant.addEventListener(listener)
        onDispose { participant.removeEventListener(listener) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .background(Color.DarkGray)
    ) {
        // VideoView is a classic View, so it is wrapped in AndroidView.
        AndroidView(
            factory = { context -> VideoView(context) },
            update = { view ->
                // Re-attach only when the track actually changed.
                if (view.tag != videoTrack) {
                    view.removeTrack()
                    videoTrack?.let { view.addTrack(it) }
                    view.tag = videoTrack
                }
            },
            onRelease = { view ->
                view.removeTrack()  // must come first, release is a no-op while a track is attached
                view.releaseSurfaceViewRenderer()
            },
            modifier = Modifier.fillMaxSize()
        )

        if (videoTrack == null) {
            Text(
                text = "Camera off",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text(
            text = participant.displayName,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0x99000000))
                .padding(4.dp)
        )
    }
}

// The participant's current camera track, if the camera is on.
private fun Participant.videoTrack(): VideoTrack? =
    streams.values.firstOrNull { it.kind == "video" }?.track as? VideoTrack
