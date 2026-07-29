package live.videosdk.rtc.android.quickstart.screens


import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import live.videosdk.rtc.android.quickstart.NetworkManager
import live.videosdk.rtc.android.quickstart.components.MyAppButton

@Composable
fun JoinScreen(
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            var input by rememberSaveable { mutableStateOf("") }

            CreateMeetingBtn(navController)

            Text(text = "OR")

            InputMeetingId(input) { updateInput ->
                input = updateInput
            }
            JoinMeetingBtn(navController, input)
        }
    }
}

@Composable
fun CreateMeetingBtn(navController: NavController) {
    MyAppButton("Create Meeting"){
        NetworkManager.createMeetingId { meetingId ->
            navController.navigate("meeting_screen?meetingId=$meetingId")
        }
    }
}


@Composable
fun InputMeetingId(input: String, onInputChange: (String) -> Unit) {
    OutlinedTextField(value = input,
        onValueChange = onInputChange,
        label = { Text(text = "Enter Meeting Id") })
}

@Composable
fun JoinMeetingBtn(navController: NavController, meetingId: String) {
    val context = LocalContext.current

    MyAppButton("Join Meeting"){
        val trimmedMeetingId = meetingId.trim()

        if (trimmedMeetingId.isEmpty()) {
            Toast.makeText(context, "Please enter the meeting ID", Toast.LENGTH_SHORT).show()
        } else {
            navController.navigate("meeting_screen?meetingId=$trimmedMeetingId")
        }
    }
}
