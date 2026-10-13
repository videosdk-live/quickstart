package live.videosdk.rtc.android.quickstart

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import live.videosdk.rtc.android.PinType
import live.videosdk.rtc.android.Room
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.lib.HLSState
import live.videosdk.rtc.android.lib.Mode
import live.videosdk.rtc.android.lib.VideoSDKException
import live.videosdk.rtc.android.listeners.RoomEventListener
import live.videosdk.rtc.android.model.HLSUrl

class MeetingActivity : AppCompatActivity() {
    var room: Room? = null
        private set

    // the HLS playback url once the stream is playable. The server sends a running stream's state
    // right after the join, before ViewerFragment exists, so it is kept here for the fragment.
    var playbackHlsUrl: String? = null
        private set
    var token: String? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)

        val meetingId = intent.getStringExtra("meetingId").orEmpty()
        val token = intent.getStringExtra("token").orEmpty()
        this.token = token
        val mode = Mode.valueOf(intent.getStringExtra("mode") ?: Mode.SEND_AND_RECV.name)
        val localParticipantName = "John Doe"
        val streamEnable = mode == Mode.SEND_AND_RECV

        // initialize VideoSDK
        VideoSDK.initialize(applicationContext)

        // Create the Room with the meeting id and token
        val room = try {
            VideoSDK.createRoom(
                roomId = meetingId,
                token = token,
                participantName = localParticipantName,
                micEnabled = streamEnable,
                camEnabled = streamEnable,
                mode = mode,
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

        // if mode is SEND_AND_RECV than replace mainLayout with SpeakerFragment otherwise with ViewerFragment.
        // onRoomJoined can fire before join() returns, so add the listener first.
        room.addEventListener(object : RoomEventListener() {
            override fun onHlsStateChanged(state: HLSState, hlsUrl: HLSUrl?) {
                playbackHlsUrl = if (state == HLSState.HLS_PLAYABLE) hlsUrl?.playbackHlsUrl else null
            }

            override fun onRoomJoined(switchRoomId: String?) {
                if (this@MeetingActivity.room == null || supportFragmentManager.findFragmentById(R.id.mainLayout) != null) return
                if (mode == Mode.SEND_AND_RECV) {
                    lifecycleScope.launch {
                        try {
                            room.localParticipant.pin(PinType.SHARE_AND_CAM)
                        } catch (e: VideoSDKException) {
                            Toast.makeText(this@MeetingActivity, "Pin failed: ${e.code} ${e.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.mainLayout, SpeakerFragment(), "MainFragment")
                        .commit()
                } else {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.mainLayout, ViewerFragment(), "viewerFragment")
                        .commit()
                }
            }
        })

        // join Room
        lifecycleScope.launch {
            try {
                room.join()
            } catch (e: VideoSDKException) {
                Toast.makeText(this@MeetingActivity, "Join failed: ${e.code} ${e.name}", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        // the back button leaves the meeting too, so the next join starts clean
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = leaveRoom()
        })
    }

    // leaves the room; onRoomLeft in the fragment takes the user back to JoinActivity
    fun leaveRoom() {
        val room = room ?: return finish()
        lifecycleScope.launch {
            try {
                room.leave()
            } catch (e: VideoSDKException) {
                // 3035: the join never completed, 3054: already left
                finish()
            }
        }
    }
}
