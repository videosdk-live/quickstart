package live.videosdk.rtc.android.quickstart.screens

import android.content.Context
import live.videosdk.rtc.android.quickstart.model.MeetingViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import live.videosdk.rtc.android.quickstart.MainApplication
import live.videosdk.rtc.android.quickstart.components.MyAppButton
import live.videosdk.rtc.android.quickstart.components.MySpacer
import live.videosdk.rtc.android.quickstart.components.MyText
import live.videosdk.rtc.android.quickstart.components.ParticipantsGrid

@Composable
fun MeetingScreen(viewModel: MeetingViewModel, navController: NavController, meetingId: String, context: Context) {

    val app = context.applicationContext as MainApplication
    val isMeetingLeft = viewModel.isMeetingLeft

    LaunchedEffect(isMeetingLeft) {
        if (isMeetingLeft) {
            navController.navigate("join_screen")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(meetingId)
        MySpacer()
        ParticipantsGrid(viewModel.participants, Modifier.weight(1f))
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
fun MediaControlButtons(onJoinClick:()->Unit,onMicClick: () -> Unit, onCamClick: () -> Unit, onLeaveClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        MyAppButton(onJoinClick,"Join")
        MyAppButton(onMicClick,"ToggleMic")
        MyAppButton(onCamClick,"ToggleCam")
        MyAppButton(onLeaveClick,"Leave")
    }
}