package live.videosdk.rtc.android.quickstart

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import live.videosdk.rtc.android.Room
import live.videosdk.rtc.android.lib.HLSState
import live.videosdk.rtc.android.lib.LeaveReason
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

class SpeakerFragment : Fragment() {
    private var hlsEnabled = false
    private var btnMic: Button? = null
    private var btnWebcam: Button? = null
    private var btnHls: Button? = null
    private var btnLeave: Button? = null
    private var tvMeetingId: TextView? = null
    private var tvHlsState: TextView? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is Activity) {
            mActivity = context
            room = (mActivity as MeetingActivity?)!!.room
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_speaker, container, false)
        btnMic = view.findViewById(R.id.btnMic)
        btnWebcam = view.findViewById(R.id.btnWebcam)
        btnHls = view.findViewById(R.id.btnHLS)
        btnLeave = view.findViewById(R.id.btnLeave)
        tvMeetingId = view.findViewById(R.id.tvMeetingId)
        tvHlsState = view.findViewById(R.id.tvHlsState)
        if (room != null) {
            tvMeetingId!!.text = "Meeting Id : " + room!!.id
            setActionListeners()
            // add Listener to the room
            room!!.addEventListener(roomEventListener)
            // the stream may already be running: its state arrived before this fragment existed
            showHlsState(room!!.hlsState)
            // set speakerAdapter
            val rvParticipants = view.findViewById<RecyclerView>(R.id.rvParticipants)
            rvParticipants.layoutManager =
                GridLayoutManager(mContext, 2)
            rvParticipants.adapter = SpeakerAdapter(room!!, lifecycleScope)
        }
        return view
    }

    private val roomEventListener: RoomEventListener = object : RoomEventListener() {
        override fun onRoomLeft(reason: LeaveReason) {
            if (isAdded) {
                val intents = Intent(mContext, JoinActivity::class.java)
                intents.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
                startActivity(intents)
                mActivity!!.finish()
            }
        }

        override fun onHlsStateChanged(state: HLSState, hlsUrl: HLSUrl?) {
            showHlsState(state)
        }
    }

    private fun showHlsState(state: HLSState) {
        tvHlsState!!.text = "Current HLS State : $state"
        if (state == HLSState.HLS_STARTED || state == HLSState.HLS_PLAYABLE) {
            hlsEnabled = true
            btnHls!!.text = "Stop HLS"
        }
        if (state == HLSState.HLS_STOPPED) {
            hlsEnabled = false
            btnHls!!.text = "Start HLS"
        }
    }

    private fun setActionListeners() {
        btnMic!!.setOnClickListener {
            val room = room ?: return@setOnClickListener
            lifecycleScope.launch {
                try {
                    if (room.isMicEnabled) {
                        room.muteMic()
                        Toast.makeText(mContext, "Mic Muted", Toast.LENGTH_SHORT).show()
                    } else {
                        room.unmuteMic()
                        Toast.makeText(
                            mContext,
                            "Mic Enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: VideoSDKException) {
                    Toast.makeText(mContext, "${e.code} ${e.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        btnWebcam!!.setOnClickListener {
            val room = room ?: return@setOnClickListener
            lifecycleScope.launch {
                try {
                    if (room.isCamEnabled) {
                        room.disableCam()
                        Toast.makeText(
                            mContext,
                            "Webcam Disabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        room.enableCam()
                        Toast.makeText(
                            mContext,
                            "Webcam Enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: VideoSDKException) {
                    Toast.makeText(mContext, "${e.code} ${e.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        btnLeave!!.setOnClickListener { (mActivity as MeetingActivity?)?.leaveRoom() }
        btnHls!!.setOnClickListener {
            val room = room ?: return@setOnClickListener
            lifecycleScope.launch {
                try {
                    if (!hlsEnabled) {
                        // returns once the request is sent; onHlsStateChanged reports the progress
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
                    } else {
                        room.stopHLS()
                    }
                } catch (e: VideoSDKException) {
                    Toast.makeText(mContext, "${e.code} ${e.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        mContext = null
        mActivity = null
        if (room != null) {
            room!!.removeEventListener(roomEventListener)
            room = null
        }
        super.onDestroy()
    }

    companion object {
        private var mActivity: Activity? = null
        private var mContext: Context? = null
        private var room: Room? = null
    }
}
