package live.videosdk.rtc.android.quickstart.model

import android.content.Context
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

class MeetingViewModel : ViewModel() {

    private var meeting: Meeting? = null
    private var micEnabled by mutableStateOf(true)
    private var webcamEnabled by mutableStateOf(true)
    val participants = mutableStateListOf<Participant>()

    var isMeetingLeft by mutableStateOf(false)
        private set

    fun initMeeting(context: Context, token: String, meetingId: String, ) {
        Log.d("TAG", "initMeeting: $meetingId")
            VideoSDK.config(token)
        if(meeting==null){
            meeting = VideoSDK.initMeeting(
                context, meetingId, "Dhiraj",
                micEnabled, webcamEnabled, null, null, true, null, null
            )
        }

        meeting!!.addEventListener(meetingEventListener)
        meeting!!.join()
    }

    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingJoined() {
            Log.d("#meeting", "onMeetingJoined()")
            meeting?.let { participants.add(it.localParticipant) }
        }

        override fun onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()")
            meeting = null
            isMeetingLeft = true
        }

        override fun onParticipantJoined(participant: Participant) {
            var existingParticipantIndex = -1

            for (i in participants.indices) {
                if (participants[i].id == participant.id) {
                    existingParticipantIndex = i
                    break
                }
            }

            if (existingParticipantIndex >= 0) {
                participants[existingParticipantIndex] = participant
            }
            else {
                participants.add(participant)
                Log.d("TAG", "participant added: ")
            }
        }

        override fun onParticipantLeft(participant: Participant) {
            participants.remove(participant)
        }
    }

    fun toggleMic() {
        if (micEnabled) {
            meeting?.muteMic()
        } else {
            meeting?.unmuteMic()
        }
        micEnabled = !micEnabled
    }

    fun toggleWebcam() {
        if (webcamEnabled) {
            meeting?.disableWebcam()
        } else {
            meeting?.enableWebcam()
        }
        webcamEnabled = !webcamEnabled
    }

    fun leaveMeeting() {
        meeting?.leave()
    }
}