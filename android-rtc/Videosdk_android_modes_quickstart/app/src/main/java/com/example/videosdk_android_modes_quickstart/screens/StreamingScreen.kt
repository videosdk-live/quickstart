package com.example.videosdk_android_modes_quickstart.screens


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.videosdk_android_modes_quickstart.MainApplication
import com.example.videosdk_android_modes_quickstart.R
import com.example.videosdk_android_modes_quickstart.StreamingMode
import com.example.videosdk_android_modes_quickstart.model.StreamViewModel
import com.example.videosdk_android_modes_quickstart.componenets.MyAppButton
import com.example.videosdk_android_modes_quickstart.componenets.MySpacer
import com.example.videosdk_android_modes_quickstart.componenets.MyText
import com.example.videosdk_android_modes_quickstart.componenets.ParticipantsGrid


@Composable
fun StreamingScreen(viewModel: StreamViewModel, navController: NavController,
    streamId: String,
    mode: StreamingMode,
    context: Context
) {
    val app = context.applicationContext as MainApplication
    val isStreamLeft = viewModel.isStreamLeft
    val currentMode = viewModel.currentMode
    val isJoined = viewModel.isJoined

    LaunchedEffect(isStreamLeft) {
        if (isStreamLeft) {
            navController.navigate("join_screen") }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(streamId, currentMode)
        MySpacer()
        ParticipantsGrid(
            participants = viewModel.currentParticipants,
            modifier = Modifier.weight(1f)
        )
        MySpacer()
        MediaControlButtons(
            onJoinClick = { viewModel.initStream(context, app.sampleToken, streamId, mode)},
            onMicClick = { viewModel.toggleMic() },
            onCamClick = { viewModel.toggleWebcam()},
            onModeToggleClick = { viewModel.toggleMode() },
            showMediaControls = currentMode == StreamingMode.SendAndReceive,
            currentMode = currentMode,
            onLeaveClick = { viewModel.leaveStream() },
            isJoined = isJoined )
    }
}
@Composable
fun Header(streamId: String, mode: StreamingMode) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Box(modifier = Modifier.fillMaxWidth()
                        .padding(8.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Row( modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MyText("StreamID: ", 28.sp)
                    MyText(streamId, 25.sp)
                    IconButton( onClick = {
                            val clip = ClipData.newPlainText("Stream ID", streamId)
                            clipboardManager.setPrimaryClip(clip)
                        })
                    {
                        //get baseline_content_copy_24 icon from Vector Asset
                        Icon( painter = painterResource(R.drawable.baseline_content_copy_24),
                            contentDescription = "Copy Stream ID",
                            modifier = Modifier.size(40.dp).padding(start = 8.dp)
                        )
                    }
                }
                Row {
                    MyText("Mode: ", 20.sp)
                    MyText(mode.name, 18.sp)
                }
            }
        }
    }
}

@Composable
fun MediaControlButtons(
    onJoinClick: () -> Unit,
    onMicClick: () -> Unit,
    onCamClick: () -> Unit,
    onModeToggleClick: () -> Unit,
    onLeaveClick: () -> Unit,
    showMediaControls: Boolean = true,
    currentMode: StreamingMode,
    isJoined: Boolean
) {
    Column( modifier = Modifier.fillMaxWidth()
                        .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row( modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isJoined) {
                MyAppButton(onJoinClick, "Join")
            }
            if (showMediaControls && isJoined) {  // Only show media controls if joined
                MyAppButton(onMicClick, "ToggleMic")
                MyAppButton(onCamClick, "ToggleCam")
            }
        }

        Row( modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isJoined) {
                MyAppButton(onLeaveClick, "Leave")
                MyAppButton(
                    task = onModeToggleClick,
                    buttonName = if (currentMode == StreamingMode.SendAndReceive)
                        "Switch to Audience"
                    else
                        "Switch to Host"
                )
            }
        }
    }
}