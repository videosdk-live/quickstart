package com.example.videosdk_android_modes_quickstart.model

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videosdk_android_modes_quickstart.StreamingMode
import kotlinx.coroutines.launch
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.Room
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.lib.LeaveReason
import live.videosdk.rtc.android.lib.Mode
import live.videosdk.rtc.android.lib.VideoSDKException
import live.videosdk.rtc.android.listeners.RoomEventListener

class StreamViewModel : ViewModel() {
    private var stream: Room? = null
    private var appContext: Context? = null
    var isConferenceMode by mutableStateOf(true)
    val currentParticipants = mutableStateListOf<Participant>()

    var localParticipantId by mutableStateOf("")
        private set

    var isStreamLeft by mutableStateOf(false)
        private set

    var currentMode by mutableStateOf(StreamingMode.SendAndReceive)
        private set

    var isJoined by mutableStateOf(false)
        private set

    fun initStream(context: Context, token: String, streamId: String, mode: StreamingMode) {
        appContext = context.applicationContext
        currentMode = mode
        if (stream == null) {
            stream = try {
                VideoSDK.createRoom(
                    roomId = streamId,
                    token = token,
                    participantName = "John Doe",
                    // an audience member who switches to host gets the mic and camera asked for here
                    micEnabled = true,
                    camEnabled = true,
                    mode = if (mode == StreamingMode.SendAndReceive) Mode.SEND_AND_RECV else Mode.RECV_ONLY,
                    multiStream = true,
                )
            } catch (e: VideoSDKException) {
                showError(e)
                return
            }
        }
        val stream = stream!!
        // onRoomJoined can fire before join() returns, so add the listener first
        stream.addEventListener(roomEventListener)
        isJoined = true
        viewModelScope.launch {
            try {
                stream.join()
            } catch (e: VideoSDKException) {
                showError(e)
                stream.removeAllEventListeners()
                this@StreamViewModel.stream = null
                isJoined = false
            }
        }
    }

    private val roomEventListener: RoomEventListener = object : RoomEventListener() {
        override fun onRoomJoined(switchRoomId: String?) {
            stream?.let {
                if (it.localParticipant.mode != Mode.RECV_ONLY) {
                    if (!currentParticipants.contains(it.localParticipant)) {
                        currentParticipants.add(it.localParticipant)
                    }
                }
                localParticipantId = it.localParticipant.id
            }
        }

        override fun onRoomLeft(reason: LeaveReason) {
            currentParticipants.clear()
            stream = null
            isStreamLeft = true
        }

        override fun onParticipantJoined(participant: Participant) {
            if (participant.mode != Mode.RECV_ONLY) {
                currentParticipants.add(participant)
            }
        }

        override fun onParticipantLeft(participant: Participant, reason: LeaveReason) {
            currentParticipants.removeAll { it.id == participant.id }
        }

        override fun onParticipantModeChanged(participantId: String, mode: Mode) {
            val participant = if (stream?.localParticipant?.id == participantId) {
                stream?.localParticipant
            } else {
                stream?.participants?.get(participantId)
            }

            participant?.let {
                when (mode) {
                    Mode.RECV_ONLY -> {
                        currentParticipants.remove(it)
                    }
                    Mode.SEND_AND_RECV -> {
                        if (!currentParticipants.contains(it)) {
                            currentParticipants.add(it)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun toggleMic() {
        val stream = stream ?: return
        viewModelScope.launch {
            try {
                if (stream.isMicEnabled) stream.muteMic() else stream.unmuteMic()
            } catch (e: VideoSDKException) {
                showError(e)
            }
        }
    }

    fun toggleWebcam() {
        val stream = stream ?: return
        viewModelScope.launch {
            try {
                if (stream.isCamEnabled) stream.disableCam() else stream.enableCam()
            } catch (e: VideoSDKException) {
                showError(e)
            }
        }
    }

    fun leaveStream() {
        currentParticipants.clear()
        stream?.let {
            it.removeAllEventListeners()
            viewModelScope.launch {
                try {
                    it.leave()
                } catch (e: VideoSDKException) {
                    // 3035: the join never completed, 3054: already left
                }
            }
        }
        stream = null
        isStreamLeft = true
        isJoined = false
    }

    fun toggleMode() {
        val stream = stream ?: return
        val newMode = if (currentMode == StreamingMode.SendAndReceive) {
            StreamingMode.ReceiveOnly
        } else {
            StreamingMode.SendAndReceive
        }
        viewModelScope.launch {
            try {
                // Going back to SEND_AND_RECV turns the mic and camera back on as they were,
                // so there is no enableCam() / unmuteMic() to call here.
                stream.changeMode(if (newMode == StreamingMode.SendAndReceive) Mode.SEND_AND_RECV else Mode.RECV_ONLY)
            } catch (e: VideoSDKException) {
                showError(e)
                return@launch
            }
            isConferenceMode = newMode == StreamingMode.SendAndReceive

            // Rebuild the list for the new mode
            currentParticipants.clear()
            // Add back all non-viewer participants
            stream.participants.values.forEach { participant ->
                if (participant.mode != Mode.RECV_ONLY) {
                    currentParticipants.add(participant)
                }
            }
            // Add local participant only if in SEND_AND_RECV mode
            if (newMode == StreamingMode.SendAndReceive) {
                currentParticipants.add(stream.localParticipant)
            }
            currentMode = newMode
        }
    }

    // SDK errors from a call this screen made; onError can arrive off the main thread, these do not
    private fun showError(e: VideoSDKException) {
        appContext?.let { Toast.makeText(it, "${e.code} ${e.name}: ${e.message}", Toast.LENGTH_SHORT).show() }
    }
}
