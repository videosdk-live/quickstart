package live.videosdk.rtc.android.quickstart.feature.meeting

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.listeners.MeetingEventListener
import live.videosdk.rtc.android.quickstart.MainApplication
import org.webrtc.ApplicationContextProvider

class MeetingViewModel : ViewModel() {

    var meeting: Meeting? = null
    private val micEnabled = mutableStateOf(true)
    private val webcamEnabled = mutableStateOf(true)
    val participants = mutableStateListOf<Participant>()

    var isMeetingLeft by mutableStateOf(false)
        private set


    fun reset() {
        meeting?.removeAllListeners()
        meeting = null
        micEnabled.value = true
        webcamEnabled.value = true
        participants.clear()
        isMeetingLeft = false
    }

    fun initMeeting(meetingId: String) {
        val context = ApplicationContextProvider.getApplicationContext() as MainApplication
        val token = context.sampleToken;
        VideoSDK.config(token)
        if (meeting == null) {
            meeting = VideoSDK.initMeeting(
                context, meetingId, "John Doe",
                micEnabled.value, webcamEnabled.value, null, null, true, null, null
            )
        }

        meeting!!.addEventListener(meetingEventListener)
        meeting!!.join()
    }

    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingJoined() {
            Log.d("#meeting", "onMeetingJoined()")
            meeting?.let {
                participants.remove(it.localParticipant)
                participants.add(it.localParticipant)
            }
        }

        override fun onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()")
            isMeetingLeft = true
        }

        override fun onParticipantJoined(participant: Participant) {
            participants.add(participant)
        }

        override fun onParticipantLeft(participant: Participant) {
            participants.remove(participant)
        }
    }

    fun toggleMic() {
        if (micEnabled.value) {
            meeting?.muteMic()
        } else {
            meeting?.unmuteMic()
        }
        micEnabled.value = !micEnabled.value
    }

    fun toggleWebcam() {
        if (webcamEnabled.value) {
            meeting?.disableWebcam()
        } else {
            meeting?.enableWebcam()
        }
        webcamEnabled.value = !webcamEnabled.value
    }

    fun leaveMeeting() {
        meeting?.leave()
    }
}