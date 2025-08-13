package com.example.videosdk_android_modes_quickstart.screens


import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.videosdk_android_modes_quickstart.MainApplication
import com.example.videosdk_android_modes_quickstart.StreamingMode
import com.example.videosdk_android_modes_quickstart.componenets.MyAppButton
import com.example.videosdk_android_modes_quickstart.model.NetworkManager


@Composable
fun JoinScreen(
    navController: NavController, context: Context
) {
    val app = context.applicationContext as MainApplication
    val token = app.sampleToken
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            var input by rememberSaveable { mutableStateOf("") }
            CreateStreamBtn(navController, token)
            Text(text = "OR")
            InputStreamId(input) { updateInput -> input = updateInput }
            JoinHostBtn(navController, input)
            JoinAudienceBtn(navController, input)
        }
    }
}


@Composable
fun JoinHostBtn(navController: NavController, streamId: String) {
    MyAppButton({
        if (streamId.isNotEmpty()) {
            navController.navigate("stream_screen?streamId=$streamId&mode=${StreamingMode.SendAndReceive.name}")
        }
    }, "Join as Host")
}

@Composable
fun JoinAudienceBtn(navController: NavController, streamId: String) {
    MyAppButton({
        if (streamId.isNotEmpty()) {
            navController.navigate("stream_screen?streamId=$streamId&mode=${StreamingMode.ReceiveOnly.name}")
        }
    }, "Join as Audience")
}

@Composable
fun CreateStreamBtn(navController: NavController, token: String) {
    MyAppButton({
        NetworkManager.createStreamId(token) { streamId ->
            navController.navigate("stream_screen?streamId=$streamId&mode=${StreamingMode.SendAndReceive.name}")
        }
    }, "Create Stream")
}

@Composable
fun InputStreamId(input: String, onInputChange: (String) -> Unit) {
    OutlinedTextField(value = input,
        onValueChange = onInputChange,
        label = { Text(text = "Enter Stream Id") })
}
