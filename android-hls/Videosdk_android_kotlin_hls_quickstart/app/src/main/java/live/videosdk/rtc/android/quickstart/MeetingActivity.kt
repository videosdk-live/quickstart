package live.videosdk.rtc.android.quickstart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.listeners.MeetingEventListener

class MeetingActivity : AppCompatActivity() {
    var meeting: Meeting? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)

        val meetingId = intent.getStringExtra("meetingId")
        val token = intent.getStringExtra("token")
        val mode = intent.getStringExtra("mode")
        val localParticipantName = "John Doe"
        val streamEnable = mode == "CONFERENCE"

        // initialize VideoSDK
        VideoSDK.initialize(applicationContext)

        // Configuration VideoSDK with Token
        VideoSDK.config(token)

        // Initialize VideoSDK Meeting
        meeting = VideoSDK.initMeeting(
            this@MeetingActivity, meetingId, localParticipantName,
            streamEnable, streamEnable, null, mode, true,null
        )

        // join Meeting
        meeting!!.join()

        // if mode is CONFERENCE than replace mainLayout with SpeakerFragment otherwise with ViewerFragment
        meeting!!.addEventListener(object : MeetingEventListener() {
            override fun onMeetingJoined() {
                if (meeting != null) {
                    if (mode == "CONFERENCE") {
                        meeting!!.localParticipant.pin("SHARE_AND_CAM")
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.mainLayout, SpeakerFragment(), "MainFragment")
                            .commit()
                    } else if (mode == "VIEWER") {
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.mainLayout, ViewerFragment(), "viewerFragment")
                            .commit()
                    }
                }
            }
        })
    }
}