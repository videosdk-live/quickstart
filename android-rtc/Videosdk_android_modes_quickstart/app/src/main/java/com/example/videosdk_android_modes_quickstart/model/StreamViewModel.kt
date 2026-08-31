package com.example.videosdk_android_modes_quickstart.model

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.videosdk_android_modes_quickstart.StreamingMode
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.listeners.MeetingEventListener
import org.json.JSONException
import org.json.JSONObject

class StreamViewModel : ViewModel() {
    private var stream: Meeting? = null
    private var micEnabled by mutableStateOf(true)
    private var webcamEnabled by mutableStateOf(true)
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
        VideoSDK.config(token)
        currentMode = mode
        if (stream == null) {
            stream = if (mode.name == StreamingMode.SendAndReceive.name) {
                VideoSDK.initMeeting(
                    context,streamId, "John Doe",
                    micEnabled, webcamEnabled, null, null, true, null, null
                )
            } else {
                VideoSDK.initMeeting(
                    context, streamId, "John Doe",
                    micEnabled, webcamEnabled, null, "RECV_ONLY", true, null, null
                )
            }
        }
        stream!!.addEventListener(meetingEventListener)
        stream!!.join()
        isJoined = true
    }

    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingJoined() {
            stream?.let {
                if (it.localParticipant.mode != "RECV_ONLY") {
                    if (!currentParticipants.contains(it.localParticipant)) {
                        currentParticipants.add(it.localParticipant)
                    }
                }
                localParticipantId = it.localParticipant.id
            }
        }

        override fun onMeetingLeft() {
            currentParticipants.clear()
            stream = null
            isStreamLeft = true
        }

        override fun onParticipantJoined(participant: Participant) {
            if (participant.mode != "RECV_ONLY") {
                currentParticipants.add(participant)
            }
        }

        override fun onParticipantLeft(participant: Participant) {
            currentParticipants.remove(participant)
        }

        override fun onParticipantModeChanged(data: JSONObject?) {
            try {
                val participantId = data?.getString("peerId")
                val participant = if (stream?.localParticipant?.id == participantId) {
                    stream?.localParticipant
                } else {
                    stream?.participants?.get(participantId)
                }

                participant?.let {
                    when (it.mode) {
                        "RECV_ONLY" -> {
                            currentParticipants.remove(it)
                        }
                        "SEND_AND_RECV" -> {
                            if (!currentParticipants.contains(it)) {
                                currentParticipants.add(it)
                            } else { }
                        }
                        else -> {}
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun toggleMic() {
        if (micEnabled) stream?.muteMic() else stream?.unmuteMic()
        micEnabled = !micEnabled
    }

    fun toggleWebcam() {
        if (webcamEnabled) stream?.disableWebcam() else stream?.enableWebcam()
        webcamEnabled = !webcamEnabled
    }

    fun leaveStream() {
        currentParticipants.clear()
        stream?.leave()
        stream?.removeAllListeners()
        isStreamLeft = true
        isJoined = false
    }

    fun toggleMode() {
        val newMode = if (currentMode == StreamingMode.SendAndReceive) {
            stream?.disableWebcam()
            stream?.changeMode("RECV_ONLY")
            isConferenceMode = false
            StreamingMode.ReceiveOnly
        } else {
            stream?.changeMode("SEND_AND_RECV")
            isConferenceMode = true
            StreamingMode.SendAndReceive
        }

        // Clear all participants first
        currentParticipants.clear()

        // Wait briefly for mode change to complete
        stream?.let { it ->
            // Add back all non-viewer participants
            it.participants.values.forEach { participant ->
                if (participant.mode != "RECV_ONLY") {
                    currentParticipants.add(participant)
                }
            }

            // Add local participant only if in SEND_AND_RECV mode
            if (newMode == StreamingMode.SendAndReceive) {
                it.localParticipant?.let { localParticipant ->
                    if (!currentParticipants.contains(localParticipant)) {
                        currentParticipants.add(localParticipant)
                    }
                }
                // Re-enable webcam if it was enabled before
                if (webcamEnabled) {
                    it.enableWebcam() }
            }
        }
        currentMode = newMode
    }
}
