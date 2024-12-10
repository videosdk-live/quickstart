package live.videosdk.rtc.android.quickstart.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    participant: Participant,
    modifier: Modifier = Modifier
) {
    var isVideoEnabled by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(if (isVideoEnabled) Color.DarkGray else Color.Gray)
    ) {
        LaunchedEffect(participant) {
            isVideoEnabled = participant.streams.any { (_, stream) ->
                stream.kind.equals("video", ignoreCase = true) && stream.track != null
            }
        }

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
            onRelease = { videoView->
                videoView.releaseSurfaceViewRenderer()
            },
            modifier = Modifier.fillMaxSize()
        )
        if (!isVideoEnabled) {
            Box(
                modifier = Modifier.fillMaxSize()
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
    participants: List<Participant>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        items(participants.size) { index ->
            ParticipantVideoView(
                participant = participants[index],
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}