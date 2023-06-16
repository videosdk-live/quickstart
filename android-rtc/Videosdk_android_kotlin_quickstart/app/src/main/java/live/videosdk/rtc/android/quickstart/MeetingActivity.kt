package live.videosdk.rtc.android.quickstart

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.listeners.MeetingEventListener

class MeetingActivity : AppCompatActivity() {
    // declare the variables we will be using to handle the meeting
    private var meeting: Meeting? = null
    private var micEnabled = true
    private var webcamEnabled = true
    private var rvParticipants: RecyclerView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)
        val token = intent.getStringExtra("token")
        val meetingId = intent.getStringExtra("meetingId")
        val participantName = "John Doe"

        // 1. Configuration VideoSDK with Token
        VideoSDK.config(token)
        // 2. Initialize VideoSDK Meeting
        meeting = VideoSDK.initMeeting(
            this@MeetingActivity, meetingId, participantName,
            micEnabled, webcamEnabled, null, null, true, null
        )

        // 3. Add event listener for listening upcoming events
        meeting!!.addEventListener(meetingEventListener)

        //4. Join VideoSDK Meeting
        meeting!!.join()
        (findViewById<View>(R.id.tvMeetingId) as TextView).text = meetingId

        // actions
        setActionListeners()
        rvParticipants = findViewById(R.id.rvParticipants)
        rvParticipants!!.layoutManager = GridLayoutManager(this, 2)
        rvParticipants!!.adapter = ParticipantAdapter(meeting!!)
    }

    // creating the MeetingEventListener
    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingJoined() {
            Log.d("#meeting", "onMeetingJoined()")
        }

        override fun onMeetingLeft() {
            Log.d("#meeting", "onMeetingLeft()")
            meeting = null
            if (!isDestroyed) finish()
        }

        override fun onParticipantJoined(participant: Participant) {
            Toast.makeText(
                this@MeetingActivity,
                participant.displayName + " joined",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onParticipantLeft(participant: Participant) {
            Toast.makeText(
                this@MeetingActivity,
                participant.displayName + " left",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setActionListeners() {
        // toggle mic
        findViewById<View>(R.id.btnMic).setOnClickListener {
            if (micEnabled) {
                // this will mute the local participant's mic
                meeting!!.muteMic()
                Toast.makeText(this@MeetingActivity, "Mic Disabled", Toast.LENGTH_SHORT).show()
            } else {
                // this will unmute the local participant's mic
                meeting!!.unmuteMic()
                Toast.makeText(this@MeetingActivity, "Mic Enabled", Toast.LENGTH_SHORT).show()
            }
            micEnabled = !micEnabled
        }

        // toggle webcam
        findViewById<View>(R.id.btnWebcam).setOnClickListener {
            if (webcamEnabled) {
                // this will disable the local participant webcam
                meeting!!.disableWebcam()
                Toast.makeText(this@MeetingActivity, "Webcam Disabled", Toast.LENGTH_SHORT).show()
            } else {
                // this will enable the local participant webcam
                meeting!!.enableWebcam()
                Toast.makeText(this@MeetingActivity, "Webcam Enabled", Toast.LENGTH_SHORT).show()
            }
            webcamEnabled = !webcamEnabled
        }

        // leave meeting
        findViewById<View>(R.id.btnLeave).setOnClickListener {
            // this will make the local participant leave the meeting
            meeting!!.leave()
        }
    }

    override fun onDestroy() {
        rvParticipants!!.adapter = null
        super.onDestroy()
    }
}