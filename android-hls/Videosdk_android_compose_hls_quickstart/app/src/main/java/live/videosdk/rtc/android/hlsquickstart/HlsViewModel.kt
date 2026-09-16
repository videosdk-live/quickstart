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
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.listeners.MeetingEventListener
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/** Host publishes video and controls the stream; Viewer only watches the HLS feed. */
enum class Role { HOST, VIEWER }

class HlsViewModel(application: Application) : AndroidViewModel(application) {

    private val token = (application as? MainApplication)?.sampleToken
        ?: error("Add android:name=\".MainApplication\" to <application> in AndroidManifest.xml")
    private var meeting: Meeting? = null

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
        get() = hlsState == "HLS_STARTED" || hlsState == "HLS_PLAYABLE"

    // Creates a room through the VideoSDK REST API, then joins it as host.
    fun createMeeting() {
        if (meeting != null) return  // already joining or joined
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

    // Configures the SDK, creates the Meeting object and joins it.
    fun joinMeeting(meetingId: String, role: Role) {
        if (meeting != null) return  // already joining or joined
        this.role = role
        val host = role == Role.HOST
        VideoSDK.config(token)
        val meeting = VideoSDK.initMeeting(
            getApplication<Application>(),        // context
            meetingId,                            // meetingId
            if (host) "Host" else "Viewer",       // participant name
            host,                                 // micEnabled
            host,                                 // webcamEnabled
            null,                                 // participantId, SDK generates one
            // A viewer watches the HLS feed, so it needs no WebRTC media at all.
            if (host) "SEND_AND_RECV" else "SIGNALLING_ONLY",
            false,                                // multiStream
            null,                                 // customTracks
            null,                                 // metaData
            VideoSDK.PreferredProtocol.UDP_OVER_TCP // preferredProtocol (the default)
        )
        meeting.addEventListener(meetingEventListener)
        meeting.join()
        this.meeting = meeting
        this.meetingId = meetingId
        this.micEnabled = host
        this.webcamEnabled = host
    }

    // All SDK callbacks arrive on the main thread.
    private val meetingEventListener = object : MeetingEventListener() {
        override fun onMeetingJoined() {
            meeting?.let { if (role == Role.HOST) addParticipant(it.localParticipant) }
        }

        override fun onParticipantJoined(participant: Participant) {
            if (role == Role.HOST) addParticipant(participant)
        }

        override fun onParticipantLeft(participant: Participant) {
            participants.removeAll { it.id == participant.id }
        }

        // The only HLS callback that carries the playback address; onHlsStarted does not.
        override fun onHlsStateChanged(state: JSONObject) {
            hlsState = state.optString("status")
            // The server needs a few seconds after HLS_STARTED before a stream is playable.
            playbackUrl = if (hlsState == "HLS_PLAYABLE") {
                state.optString("playbackHlsUrl").ifBlank { null }
            } else {
                null
            }
        }

        // Also fired by the SDK when the server ends the call.
        override fun onMeetingLeft() {
            reset()
        }

        override fun onError(error: JSONObject) {
            errorMessage = error.optString("message")
        }
    }

    // The SDK replays join callbacks after a reconnect; never list the same participant twice.
    private fun addParticipant(participant: Participant) {
        if (participants.none { it.id == participant.id }) participants.add(participant)
    }

    fun toggleMic() {
        if (micEnabled) meeting?.muteMic() else meeting?.unmuteMic()
        micEnabled = !micEnabled
    }

    fun toggleWebcam() {
        if (webcamEnabled) meeting?.disableWebcam() else meeting?.enableWebcam()
        webcamEnabled = !webcamEnabled
    }

    fun toggleHls() {
        if (isLive) {
            meeting?.stopHls()
        } else {
            // Layout, theme and quality of the composed stream the server produces.
            val layout = JSONObject()
                .put("type", "SPOTLIGHT")
                .put("priority", "PIN")
                .put("gridSize", 4)
            val config = JSONObject()
                .put("layout", layout)
                .put("orientation", "portrait")
                .put("theme", "DARK")
                .put("quality", "high")
            meeting?.startHls(config, null)
        }
    }

    fun leaveMeeting() {
        meeting?.let {
            it.removeAllListeners()  // this Meeting is finished, ignore anything else it reports
            it.leave()
        }
        // Reset here instead of waiting for a callback: onMeetingLeft never fires if the join itself failed.
        reset()
    }

    // Back to the Join screen with clean state; setting meetingId to null switches the UI.
    private fun reset() {
        meeting = null
        participants.clear()
        micEnabled = true
        webcamEnabled = true
        hlsState = "NOT_STARTED"
        playbackUrl = null
        meetingId = null
    }
}
