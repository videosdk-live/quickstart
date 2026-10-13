package live.videosdk.rtc.android.hlsquickstart

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
import live.videosdk.rtc.android.lib.HLSState
import live.videosdk.rtc.android.lib.LeaveReason
import live.videosdk.rtc.android.lib.Mode
import live.videosdk.rtc.android.lib.VideoSDKError
import live.videosdk.rtc.android.lib.VideoSDKException
import live.videosdk.rtc.android.listeners.RoomEventListener
import live.videosdk.rtc.android.model.ConfigLayout
import live.videosdk.rtc.android.model.ConfigLayoutGrid
import live.videosdk.rtc.android.model.ConfigLayoutPriority
import live.videosdk.rtc.android.model.ConfigOrientation
import live.videosdk.rtc.android.model.ConfigQuality
import live.videosdk.rtc.android.model.ConfigTheme
import live.videosdk.rtc.android.model.HLSConfig
import live.videosdk.rtc.android.model.HLSUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/** Host publishes video and controls the stream; Viewer only watches the HLS feed. */
enum class Role { HOST, VIEWER }

class HlsViewModel(application: Application) : AndroidViewModel(application) {

    private val token = (application as? MainApplication)?.sampleToken
        ?: error("Add android:name=\".MainApplication\" to <application> in AndroidManifest.xml")
    private var room: Room? = null

    // Non-null while we are inside a meeting. MainActivity switches screens on it.
    var meetingId by mutableStateOf<String?>(null)
        private set

    var role by mutableStateOf(Role.HOST)
        private set

    val participants = mutableStateListOf<Participant>()

    var micEnabled by mutableStateOf(true)
        private set

    var webcamEnabled by mutableStateOf(true)
        private set

    // Server-reported stream state, shown on the host screen. Drives the Start/Stop button label.
    var hlsState by mutableStateOf("NOT_STARTED")
        private set

    // Set once the server has a stream the viewer can actually play.
    var playbackUrl by mutableStateOf<String?>(null)
        private set

    // Set by the SDK (or a failed network call); MainActivity shows it as a Toast.
    var errorMessage by mutableStateOf<String?>(null)

    val isLive: Boolean
        get() = hlsState == HLSState.HLS_STARTED.name || hlsState == HLSState.HLS_PLAYABLE.name

    // Creates a room through the VideoSDK REST API, then joins it as host.
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
                joinMeeting(roomId, Role.HOST)
            } catch (e: Exception) {
                errorMessage = "Could not create meeting: ${e.message}"
            }
        }
    }

    // Creates the Room object and joins it.
    fun joinMeeting(meetingId: String, role: Role) {
        if (room != null) return  // already joining or joined
        this.role = role
        val host = role == Role.HOST
        val room = try {
            VideoSDK.createRoom(
                roomId = meetingId,
                token = token,
                participantName = if (host) "Host" else "Viewer",
                micEnabled = host,
                camEnabled = host,
                // A viewer watches the HLS feed, so it needs no WebRTC media at all.
                mode = if (host) Mode.SEND_AND_RECV else Mode.SIGNALLING_ONLY,
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
        this.micEnabled = host
        this.webcamEnabled = host
        viewModelScope.launch {
            try {
                room.join()
            } catch (e: VideoSDKException) {
                errorMessage = "Join failed: ${e.code} ${e.name}"
                if (this@HlsViewModel.room === room) reset()
            }
        }
    }

    // Room callbacks arrive on the main thread, except onError.
    private val roomEventListener = object : RoomEventListener() {
        override fun onRoomJoined(switchRoomId: String?) {
            room?.let { if (role == Role.HOST) addParticipant(it.localParticipant) }
        }

        override fun onParticipantJoined(participant: Participant) {
            if (role == Role.HOST) addParticipant(participant)
        }

        override fun onParticipantLeft(participant: Participant, reason: LeaveReason) {
            participants.removeAll { it.id == participant.id }
        }

        // The only HLS callback that carries the playback address.
        override fun onHlsStateChanged(state: HLSState, hlsUrl: HLSUrl?) {
            hlsState = state.name
            // The server needs a few seconds after HLS_STARTED before a stream is playable.
            playbackUrl = if (state == HLSState.HLS_PLAYABLE) {
                hlsUrl?.playbackHlsUrl?.ifBlank { null }
            } else {
                null
            }
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

    fun toggleHls() {
        val room = room ?: return
        viewModelScope.launch {
            try {
                if (isLive) {
                    room.stopHLS()
                } else {
                    // Layout, theme and quality of the composed stream the server produces.
                    room.startHLS(
                        HLSConfig(
                            layout = ConfigLayout(
                                type = ConfigLayoutGrid.SPOTLIGHT,
                                priority = ConfigLayoutPriority.PIN,
                                gridSize = 4,
                            ),
                            orientation = ConfigOrientation.PORTRAIT,
                            theme = ConfigTheme.DARK,
                            quality = ConfigQuality.HIGH,
                        )
                    )
                }
            } catch (e: VideoSDKException) {
                errorMessage = "${e.code} ${e.name}: ${e.message}"
            }
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
        hlsState = "NOT_STARTED"
        playbackUrl = null
        meetingId = null
    }
}
