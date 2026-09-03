package live.videosdk.rtc.android.quickstart

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                VideoSDKApp()
            }
        }
    }
}

@Composable
fun VideoSDKApp(viewModel: MeetingViewModel = viewModel()) {
    val context = LocalContext.current

    // Ask for camera and microphone as soon as the app opens.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
    }

    // Show SDK and network errors as a Toast.
    viewModel.errorMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.errorMessage = null
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val meetingId = viewModel.meetingId
        if (meetingId == null) {
            JoinScreen(viewModel, Modifier.padding(innerPadding))
        } else {
            MeetingScreen(meetingId, viewModel, Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun JoinScreen(viewModel: MeetingViewModel, modifier: Modifier = Modifier) {
    var input by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { viewModel.createMeeting() }) {
            Text("Create Meeting")
        }

        Text("OR", modifier = Modifier.padding(16.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Enter Meeting Id") }
        )

        Button(
            onClick = { if (input.isNotBlank()) viewModel.joinMeeting(input.trim()) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Join Meeting")
        }
    }
}

@Composable
fun MeetingScreen(meetingId: String, viewModel: MeetingViewModel, modifier: Modifier = Modifier) {
    // Back button leaves the meeting; onMeetingLeft brings the join screen back.
    BackHandler { viewModel.leaveMeeting() }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Meeting ID: $meetingId",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        ParticipantsGrid(viewModel.participants, Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.toggleMic() }) {
                Text(if (viewModel.micEnabled) "Mute Mic" else "Unmute Mic")
            }
            Button(onClick = { viewModel.toggleWebcam() }) {
                Text(if (viewModel.webcamEnabled) "Cam Off" else "Cam On")
            }
            Button(onClick = { viewModel.leaveMeeting() }) {
                Text("Leave")
            }
        }
    }
}
