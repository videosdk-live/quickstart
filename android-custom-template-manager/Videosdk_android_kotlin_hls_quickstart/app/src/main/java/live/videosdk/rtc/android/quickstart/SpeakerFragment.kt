package live.videosdk.rtc.android.quickstart

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.textfield.TextInputLayout
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.lib.JsonUtils
import live.videosdk.rtc.android.listeners.MeetingEventListener
import live.videosdk.rtc.android.model.PubSubPublishOptions
import org.json.JSONException
import org.json.JSONObject

class SpeakerFragment : Fragment() {
    private var micEnabled = true
    private var webcamEnabled = true
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
            meeting = (mActivity as MeetingActivity?)!!.meeting
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
        if (meeting != null) {
            tvMeetingId!!.text = "Meeting Id : " + meeting!!.meetingId
            setActionListeners()
            // add Listener to the meeting
            meeting!!.addEventListener(meetingEventListener)
            // set speakerAdapter
            val rvParticipants = view.findViewById<RecyclerView>(R.id.rvParticipants)
            rvParticipants.layoutManager =
                GridLayoutManager(mContext, 2)
            rvParticipants.adapter = SpeakerAdapter(meeting!!)
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
            val publishOptions = PubSubPublishOptions()
            publishOptions.isPersist = true

            // Sending the Message using the publish method
            meeting!!.pubSub.publish(
                "CHANGE_BACKGROUND",
                et_bgColor.editText!!.text.toString().trim { it <= ' ' },
                publishOptions
            )
            dialog.dismiss()
            Toast.makeText(
                mContext,
                "LiveStream Background Changed",
                Toast.LENGTH_SHORT
            ).show()
        }
        btnNotify.setOnClickListener {
            val publishOptions = PubSubPublishOptions()
            publishOptions.isPersist = true

            // Sending the Message using the publish method
            meeting!!.pubSub.publish(
                "VIEWER_MESSAGE",
                et_message.editText!!.text.toString().trim { it <= ' ' },
                publishOptions
            )
            dialog.dismiss()
            Toast.makeText(mContext, "Notified User", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
        dialog.window!!.setBackgroundDrawableResource(R.color.dark_grey)
    }

    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingLeft() {
            // unpin the local participant
            meeting!!.localParticipant.unpin("SHARE_AND_CAM")
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

        @RequiresApi(api = Build.VERSION_CODES.P)
        override fun onHlsStateChanged(HlsState: JSONObject) {
            if (HlsState.has("status")) {
                try {
                    tvHlsState!!.text = "Current HLS State : " + HlsState.getString("status")
                    if (HlsState.getString("status") == "HLS_STARTED") {
                        hlsEnabled = true
                        btnHls!!.text = "Stop HLS"
                    }
                    if (HlsState.getString("status") == "HLS_STOPPED") {
                        hlsEnabled = false
                        btnHls!!.text = "Start HLS"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setActionListeners() {
        btnMic!!.setOnClickListener {
            if (micEnabled) {
                meeting!!.muteMic()
                Toast.makeText(mContext, "Mic Muted", Toast.LENGTH_SHORT).show()
            } else {
                meeting!!.unmuteMic()
                Toast.makeText(
                    mContext,
                    "Mic Enabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
            micEnabled = !micEnabled
        }
        btnWebcam!!.setOnClickListener {
            if (webcamEnabled) {
                meeting!!.disableWebcam()
                Toast.makeText(
                    mContext,
                    "Webcam Disabled",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                meeting!!.enableWebcam()
                Toast.makeText(
                    mContext,
                    "Webcam Enabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
            webcamEnabled = !webcamEnabled
        }
        btnLeave!!.setOnClickListener { meeting!!.leave() }
        btnHls!!.setOnClickListener {
            if (!hlsEnabled) {
                //Update your Custom Template URL here if you have deployed your own
                val templateUrl =
                    "https://lab.videosdk.live/react-custom-template-demo?meetingId=" + meeting!!.meetingId + "&token=" + token

                val bodyJson = JSONObject()
                JsonUtils.jsonPut(bodyJson, "roomId", meeting!!.meetingId)
                JsonUtils.jsonPut(bodyJson, "templateUrl", templateUrl)

                val config = JSONObject()
                JsonUtils.jsonPut(config, "orientation", "portrait")
                JsonUtils.jsonPut(bodyJson, "config", config)

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
                meeting!!.stopHls()
            }
        }

    }

    override fun onDestroy() {
        mContext = null
        mActivity = null
        if (meeting != null) {
            meeting!!.removeAllListeners()
            meeting = null
        }
        super.onDestroy()
    }

    companion object {
        private var mActivity: Activity? = null
        private var mContext: Context? = null
        private var meeting: Meeting? = null
    }
}