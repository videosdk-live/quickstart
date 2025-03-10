package live.videosdk.rtc.android.quickstart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
fun ParticipantVideoView(
    participant: Participant
) {
    var isVideoEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(participant) {
        isVideoEnabled = participant.streams.any { (_, stream) ->
            stream.kind.equals("video", ignoreCase = true) && stream.track != null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(if (isVideoEnabled) Color.DarkGray else Color.Gray)
    ) {
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    for ((_, stream) in participant.streams) {
                        if (stream.kind.equals("video", ignoreCase = true)) {
                            val videoTrack = stream.track as VideoTrack
                            addTrack(videoTrack)
                            isVideoEnabled = true
                        }
                    }
                }
            },
            update = { videoView ->
                participant.addEventListener(object : ParticipantEventListener() {
                    override fun onStreamEnabled(stream: Stream) {
                        if (stream.kind.equals("video", ignoreCase = true)) {
                            val videoTrack = stream.track as VideoTrack
                            videoView.addTrack(videoTrack)
                            isVideoEnabled = true
                        }
                    }

                    override fun onStreamDisabled(stream: Stream) {
                        if (stream.kind.equals("video", ignoreCase = true)) {
                            videoView.removeTrack()
                            isVideoEnabled = false
                        }
                    }
                })
            },
            onRelease = { videoView ->
                videoView.releaseSurfaceViewRenderer()
            },
            modifier = Modifier.fillMaxSize()
        )


        if (!isVideoEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera Off",
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0x99000000))
                .padding(4.dp)
        ) {
            Text(
                text = participant.displayName,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ParticipantsGrid(
    gridCells: GridCells,
    participants: List<Participant>,
    modifier: Modifier = Modifier
) {
    key(participants) {
        LazyVerticalGrid(
            columns = gridCells,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            items(participants.size) { index ->
                ParticipantVideoView(
                    participant = participants[index],
                )
            }
        }
    }
}