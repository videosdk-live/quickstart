package live.videosdk.rtc.android.quickstart

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import live.videosdk.rtc.android.Room
import live.videosdk.rtc.android.lib.HLSState
import live.videosdk.rtc.android.lib.LeaveReason
import live.videosdk.rtc.android.lib.VideoSDKException
import live.videosdk.rtc.android.listeners.RoomEventListener
import live.videosdk.rtc.android.model.HLSUrl
import live.videosdk.rtc.android.model.PubSubPublishOptions
import org.json.JSONObject

class SpeakerFragment : Fragment() {
    private var hlsEnabled = false
    private var btnMic: Button? = null
    private var btnWebcam: Button? = null
    private var btnHls: Button? = null
    private var btnLeave: Button? = null
    private var btnSetting: Button? = null
    private var tvMeetingId: TextView? = null
    private var tvHlsState: TextView? = null
    private var token: String? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is Activity) {
            mActivity = context
            room = (mActivity as MeetingActivity?)!!.room
            token = (mActivity as MeetingActivity?)!!.token
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
        btnSetting = view.findViewById(R.id.btnSetting)
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

        btnSetting!!.setOnClickListener { showSettingDialog() }

        return view
    }

    private fun showSettingDialog() {
        val dialog = Dialog(mContext!!)
        dialog.setContentView(R.layout.setting_dialog_layout)
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val et_bgColor = dialog.findViewById<TextInputLayout>(R.id.et_bgColor)
        val et_message = dialog.findViewById<TextInputLayout>(R.id.et_message)
        val btnBgColor = dialog.findViewById<Button>(R.id.btnBgColor)
        val btnNotify = dialog.findViewById<Button>(R.id.btnNotify)
        btnBgColor.setOnClickListener { _: View? ->
            // Sending the Message using the publish method
            publish(
                "CHANGE_BACKGROUND",
                et_bgColor.editText!!.text.toString().trim { it <= ' ' },
                "LiveStream Background Changed"
            )
            dialog.dismiss()
        }
        btnNotify.setOnClickListener {
            // Sending the Message using the publish method
            publish(
                "VIEWER_MESSAGE",
                et_message.editText!!.text.toString().trim { it <= ' ' },
                "Notified User"
            )
            dialog.dismiss()
        }
        dialog.show()
        dialog.window!!.setBackgroundDrawableResource(R.color.dark_grey)
    }

    // publish() is a suspend function that throws VideoSDKException
    private fun publish(topic: String, message: String, doneText: String) {
        val room = room ?: return
        lifecycleScope.launch {
            try {
                room.pubSub.publish(topic, message, PubSubPublishOptions(persist = true))
                Toast.makeText(mContext, doneText, Toast.LENGTH_SHORT).show()
            } catch (e: VideoSDKException) {
                Toast.makeText(mContext, "${e.code} ${e.name}", Toast.LENGTH_SHORT).show()
            }
        }
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
            if (!hlsEnabled) {
                //Update your Custom Template URL here if you have deployed your own
                val templateUrl =
                    "https://lab.videosdk.live/react-custom-template-demo?meetingId=" + room.id + "&token=" + token

                val bodyJson = JSONObject()
                bodyJson.put("roomId", room.id)
                bodyJson.put("templateUrl", templateUrl)

                val config = JSONObject()
                config.put("orientation", "portrait")
                bodyJson.put("config", config)

                AndroidNetworking.post("https://api.videosdk.live/v2/hls/start")
                    .addHeaders(
                        "Authorization",
                        token
                    ) //we will pass the token in the Headers
                    .addJSONObjectBody(bodyJson)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            try {
                                Log.d(
                                    "TAG",
                                    "onResponse: $response"
                                ) // result will have playbackHlsUrl
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        override fun onError(anError: ANError) {
                            anError.printStackTrace()
                            Toast.makeText(
                                mContext,
                                anError.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            } else {
                lifecycleScope.launch {
                    try {
                        room.stopHLS()
                    } catch (e: VideoSDKException) {
                        Toast.makeText(mContext, "${e.code} ${e.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    override fun onDestroy() {
        mContext = null
        mActivity = null
        if (room != null) {
            room!!.removeAllEventListeners()
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
