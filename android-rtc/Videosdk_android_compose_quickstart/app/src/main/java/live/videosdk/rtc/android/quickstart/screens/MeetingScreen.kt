package live.videosdk.rtc.android.quickstart.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import live.videosdk.rtc.android.quickstart.MainApplication
import live.videosdk.rtc.android.quickstart.components.*
import live.videosdk.rtc.android.quickstart.model.MeetingViewModel


@Composable
fun MeetingScreen(viewModel: MeetingViewModel, navController: NavController, meetingId: String, context: Context)
{
    val app = context.applicationContext as MainApplication
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

    Column(modifier = Modifier.fillMaxSize()) {
        Header(meetingId)
        MySpacer()
        ParticipantsGrid(gridCells = GridCells.Fixed(2),viewModel.participants, Modifier.weight(1f))
        MySpacer()
        MediaControlButtons(
            onJoinClick = {
                viewModel.initMeeting(context, app.sampleToken, meetingId)
            },
            onMicClick = { viewModel.toggleMic() },
            onCamClick = { viewModel.toggleWebcam() },
            onLeaveClick = {
                viewModel.leaveMeeting()
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            MyAppButton(
                onClick = { joinClicked = true
                    onJoinClick()
                },
                label = if (joinClicked) "Joined!" else "Join",
                enabled = !joinClicked
            )
            MyAppButton(onMicClick, "Toggle Mic")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MyAppButton(onCamClick, "Toggle Cam")
            MyAppButton(onLeaveClick, "Leave")
        }
    }
}