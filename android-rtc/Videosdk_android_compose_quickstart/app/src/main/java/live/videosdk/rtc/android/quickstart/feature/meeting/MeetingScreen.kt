package live.videosdk.rtc.android.quickstart.feature.meeting

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import live.videosdk.rtc.android.quickstart.core.components.MyAppButton
import live.videosdk.rtc.android.quickstart.core.components.MySpacer
import live.videosdk.rtc.android.quickstart.core.components.MyText
import live.videosdk.rtc.android.quickstart.core.components.ParticipantsGrid


@Composable
fun MeetingScreen(viewModel: MeetingViewModel, navController: NavController, meetingId: String)
{
    val isMeetingLeft = viewModel.isMeetingLeft

    BackHandler {
        viewModel.leaveMeeting()
    }

    LaunchedEffect(isMeetingLeft) {
        if (isMeetingLeft) {
            navController.navigate("join_screen")
            viewModel.reset()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),) {
        Header(meetingId)
        MySpacer()
        ParticipantsGrid(
            gridCells = GridCells.Fixed(2),
            viewModel.participants,
            Modifier.weight(1f)
        )
        MySpacer()
        MediaControlButtons(
            onJoinClick = {
                viewModel.initMeeting(meetingId)
            },
            onMicClick = { viewModel.toggleMic() },
            onCamClick = { viewModel.toggleWebcam() },
            onLeaveClick = {
                viewModel.leaveMeeting()
                navController.navigate("join_screen"){
                    popUpTo("join_screen"){
                        inclusive = true
                    }
                }
            }
        )
    }
}

@Composable
fun Header(meetingId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Row {
            MyText("MeetingID: ", 28.sp)
            MyText(meetingId, 25.sp)
        }
    }
}

@Composable
fun MediaControlButtons(
    onJoinClick: () -> Unit,
    onMicClick: () -> Unit,
    onCamClick: () -> Unit,
    onLeaveClick: () -> Unit,
    ) {
    var joinClicked by remember { mutableStateOf(false) }
    var micClicked by remember { mutableStateOf(false) }
    var camClicked by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            // Join Button
            MyAppButton(
                label = if (joinClicked) "Joined!" else "Join",
                enabled = !joinClicked
            ) {
                joinClicked = true
                onJoinClick()
            }

            // Leave Button
            MyAppButton("Leave") {
                onLeaveClick()
            }

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Cam Button
            MyAppButton(label = if (camClicked) "Toggle Cam On" else "Toggle Cam Off") {
                camClicked = !camClicked
                onCamClick()
            }

            // Mic Button
            MyAppButton(if (micClicked) "Toggle Mic On" else "Toggle Mic Off") {
                micClicked = !micClicked
                onMicClick()
            }

        }
    }
}