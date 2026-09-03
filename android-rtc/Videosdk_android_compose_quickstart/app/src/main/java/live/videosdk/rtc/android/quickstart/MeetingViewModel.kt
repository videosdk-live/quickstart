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
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.listeners.MeetingEventListener
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MeetingViewModel(application: Application) : AndroidViewModel(application) {

    private val token = (application as? MainApplication)?.sampleToken
        ?: error("Add android:name=\".MainApplication\" to <application> in AndroidManifest.xml")
    private var meeting: Meeting? = null

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
                joinMeeting(roomId)
            } catch (e: Exception) {
                errorMessage = "Could not create meeting: ${e.message}"
            }
        }
    }

    // Configures the SDK, creates the Meeting object and joins it.
    fun joinMeeting(meetingId: String) {
        if (meeting != null) return  // already joining or joined
        VideoSDK.config(token)
        val meeting = VideoSDK.initMeeting(
            getApplication<Application>(),        // context
            meetingId,                            // meetingId
            "John Doe",                           // participant name
            micEnabled,                           // micEnabled
            webcamEnabled,                        // webcamEnabled
            null,                                 // participantId, SDK generates one
            null,                                 // mode, defaults to SEND_AND_RECV
            false,                                // multiStream
            null,                                 // customTracks
            null,                                 // metaData
            VideoSDK.PreferredProtocol.UDP_OVER_TCP // preferredProtocol (the default)
        )
        meeting.addEventListener(meetingEventListener)
        meeting.join()
        this.meeting = meeting
        this.meetingId = meetingId
    }

    // All SDK callbacks arrive on the main thread.
    private val meetingEventListener = object : MeetingEventListener() {
        override fun onMeetingJoined() {
            meeting?.let { addParticipant(it.localParticipant) }
        }

        override fun onParticipantJoined(participant: Participant) {
            addParticipant(participant)
        }

        override fun onParticipantLeft(participant: Participant) {
            participants.removeAll { it.id == participant.id }
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
        meetingId = null
    }
}
