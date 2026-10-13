package live.videosdk.rtc.android.quickstart

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.Room
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.lib.LeaveReason
import live.videosdk.rtc.android.lib.VideoSDKError
import live.videosdk.rtc.android.lib.VideoSDKException
import live.videosdk.rtc.android.listeners.RoomEventListener
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MeetingViewModel(application: Application) : AndroidViewModel(application) {

    private val token = (application as? MainApplication)?.sampleToken
        ?: error("Add android:name=\".MainApplication\" to <application> in AndroidManifest.xml")
    private var room: Room? = null

    // Non-null while we are inside a meeting. MainActivity switches screens on it.
    var meetingId by mutableStateOf<String?>(null)
        private set

    val participants = mutableStateListOf<Participant>()

    var micEnabled by mutableStateOf(true)
        private set

    var webcamEnabled by mutableStateOf(true)
        private set

    // Set by the SDK (or a failed network call); MainActivity shows it as a Toast.
    var errorMessage by mutableStateOf<String?>(null)

    // Creates a room through the VideoSDK REST API, then joins it.
    fun createMeeting() {
        if (room != null) return  // already joining or joined
        viewModelScope.launch {
            try {
                val roomId = withContext(Dispatchers.IO) {
                    val request = Request.Builder()
                        .url("https://api.videosdk.live/v2/rooms")
                        .header("Authorization", token)
                        .post("".toRequestBody())
                        .build()
                    OkHttpClient().newCall(request).execute().use { response ->
                        val body = response.body?.string().orEmpty()
                        check(response.isSuccessful) { "HTTP ${response.code}: $body" }
                        JSONObject(body).getString("roomId")
                    }
                }
                joinMeeting(roomId)
            } catch (e: Exception) {
                errorMessage = "Could not create meeting: ${e.message}"
            }
        }
    }

    // Creates the Room object and joins it.
    fun joinMeeting(meetingId: String) {
        if (room != null) return  // already joining or joined
        val room = try {
            VideoSDK.createRoom(
                roomId = meetingId,
                token = token,
                participantName = "John Doe",
                micEnabled = micEnabled,
                camEnabled = webcamEnabled,
                multiStream = false,
            )
        } catch (e: VideoSDKException) {
            errorMessage = "${e.code} ${e.name}: ${e.message}"
            return
        }
        room.addEventListener(roomEventListener)
        // onRoomJoined can fire before join() returns, so keep the room before joining.
        this.room = room
        this.meetingId = meetingId
        viewModelScope.launch {
            try {
                room.join()
            } catch (e: VideoSDKException) {
                errorMessage = "Join failed: ${e.code} ${e.name}"
                if (this@MeetingViewModel.room === room) reset()
            }
        }
    }

    // Room callbacks arrive on the main thread, except onError.
    private val roomEventListener = object : RoomEventListener() {
        override fun onRoomJoined(switchRoomId: String?) {
            room?.let { addParticipant(it.localParticipant) }
        }

        override fun onParticipantJoined(participant: Participant) {
            addParticipant(participant)
        }

        override fun onParticipantLeft(participant: Participant, reason: LeaveReason) {
            participants.removeAll { it.id == participant.id }
        }

        // Also fired by the SDK when the server ends the call.
        override fun onRoomLeft(reason: LeaveReason) {
            reset()
        }

        override fun onError(error: VideoSDKError) {
            viewModelScope.launch { errorMessage = error.message }
        }
    }

    // The SDK replays join callbacks after a reconnect; never list the same participant twice.
    private fun addParticipant(participant: Participant) {
        if (participants.none { it.id == participant.id }) participants.add(participant)
    }

    fun toggleMic() {
        val room = room ?: return
        viewModelScope.launch {
            try {
                if (room.isMicEnabled) room.muteMic() else room.unmuteMic()
            } catch (e: VideoSDKException) {
                errorMessage = "${e.code} ${e.name}: ${e.message}"
            }
            // read the state back instead of flipping our own flag
            micEnabled = room.isMicEnabled
        }
    }

    fun toggleWebcam() {
        val room = room ?: return
        viewModelScope.launch {
            try {
                if (room.isCamEnabled) room.disableCam() else room.enableCam()
            } catch (e: VideoSDKException) {
                errorMessage = "${e.code} ${e.name}: ${e.message}"
            }
            webcamEnabled = room.isCamEnabled
        }
    }

    fun leaveMeeting() {
        room?.let {
            it.removeAllEventListeners()  // this Room is finished, ignore anything else it reports
            viewModelScope.launch {
                try {
                    it.leave()
                } catch (e: VideoSDKException) {
                    // 3035: the join never completed, 3054: already left
                }
            }
        }
        // Reset here instead of waiting for onRoomLeft, since the listener is already removed.
        reset()
    }

    // Back to the Join screen with clean state; setting meetingId to null switches the UI.
    private fun reset() {
        room = null
        participants.clear()
        micEnabled = true
        webcamEnabled = true
        meetingId = null
    }
}
