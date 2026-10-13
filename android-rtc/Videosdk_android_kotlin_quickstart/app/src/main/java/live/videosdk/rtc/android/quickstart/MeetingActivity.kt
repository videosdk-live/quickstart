package live.videosdk.rtc.android.quickstart

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.Room
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.lib.LeaveReason
import live.videosdk.rtc.android.lib.VideoSDKError
import live.videosdk.rtc.android.lib.VideoSDKException
import live.videosdk.rtc.android.listeners.RoomEventListener

class MeetingActivity : AppCompatActivity() {
    // declare the variables we will be using to handle the meeting
    private var room: Room? = null
    private var rvParticipants: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)
        val token = intent.getStringExtra("token").orEmpty()
        val meetingId = intent.getStringExtra("meetingId").orEmpty()
        val participantName = "John Doe"

        // 1. Create the Room with the meeting id and token
        val room = try {
            VideoSDK.createRoom(
                roomId = meetingId,
                token = token,
                participantName = participantName,
                micEnabled = true,
                camEnabled = true,
            )
        } catch (e: VideoSDKException) {
            Toast.makeText(this, "${e.code} ${e.name}: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        this.room = room

        // the SDK pauses the camera while this screen is in the background; tie that to this
        // activity, otherwise JoinActivity stopping turns the camera off during the join
        VideoSDK.setActivityForLifeCycle(this)

        // 2. Add event listener for listening upcoming events
        room.addEventListener(roomEventListener)

        (findViewById<View>(R.id.tvMeetingId) as TextView).text = meetingId

        // actions
        setActionListeners()
        rvParticipants = findViewById(R.id.rvParticipants)
        rvParticipants!!.layoutManager = GridLayoutManager(this, 2)
        rvParticipants!!.adapter = ParticipantAdapter(room)

        // 3. Join the Room
        lifecycleScope.launch {
            try {
                room.join()
            } catch (e: VideoSDKException) {
                Toast.makeText(this@MeetingActivity, "Join failed: ${e.code} ${e.name}", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    // creating the RoomEventListener
    private val roomEventListener: RoomEventListener = object : RoomEventListener() {
        override fun onRoomJoined(switchRoomId: String?) {
            Log.d("#meeting", "onRoomJoined()")
        }

        override fun onRoomLeft(reason: LeaveReason) {
            Log.d("#meeting", "onRoomLeft(): ${reason.message}")
            room = null
            if (!isDestroyed) finish()
        }

        override fun onParticipantJoined(participant: Participant) {
            Toast.makeText(
                this@MeetingActivity,
                participant.displayName + " joined",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onParticipantLeft(participant: Participant, reason: LeaveReason) {
            Toast.makeText(
                this@MeetingActivity,
                participant.displayName + " left",
                Toast.LENGTH_SHORT
            ).show()
        }

        // onError can arrive off the main thread
        override fun onError(error: VideoSDKError) {
            Log.e("#meeting", "onError(): ${error.code} ${error.name}: ${error.message}")
            runOnUiThread {
                Toast.makeText(this@MeetingActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setActionListeners() {
        // toggle mic
        findViewById<View>(R.id.btnMic).setOnClickListener {
            val room = room ?: return@setOnClickListener
            lifecycleScope.launch {
                try {
                    if (room.isMicEnabled) {
                        // this will mute the local participant's mic
                        room.muteMic()
                        Toast.makeText(this@MeetingActivity, "Mic Disabled", Toast.LENGTH_SHORT).show()
                    } else {
                        // this will unmute the local participant's mic
                        room.unmuteMic()
                        Toast.makeText(this@MeetingActivity, "Mic Enabled", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: VideoSDKException) {
                    Log.e("#meeting", "Mic toggle failed: ${e.code} ${e.name}")
                }
            }
        }

        // toggle webcam
        findViewById<View>(R.id.btnWebcam).setOnClickListener {
            val room = room ?: return@setOnClickListener
            lifecycleScope.launch {
                try {
                    if (room.isCamEnabled) {
                        // this will disable the local participant webcam
                        room.disableCam()
                        Toast.makeText(this@MeetingActivity, "Webcam Disabled", Toast.LENGTH_SHORT).show()
                    } else {
                        // this will enable the local participant webcam
                        room.enableCam()
                        Toast.makeText(this@MeetingActivity, "Webcam Enabled", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: VideoSDKException) {
                    Log.e("#meeting", "Webcam toggle failed: ${e.code} ${e.name}")
                }
            }
        }

        // leave meeting
        findViewById<View>(R.id.btnLeave).setOnClickListener { leaveRoom() }

        // the back button leaves the meeting too, so the next join starts clean
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = leaveRoom()
        })
    }

    private fun leaveRoom() {
        val room = room ?: return finish()
        lifecycleScope.launch {
            try {
                // this will make the local participant leave the meeting
                room.leave()
            } catch (e: VideoSDKException) {
                Log.w("#meeting", "Leave refused: ${e.code} ${e.name}")
            }
            finish()
        }
    }

    override fun onDestroy() {
        rvParticipants?.adapter = null
        super.onDestroy()
    }
}
