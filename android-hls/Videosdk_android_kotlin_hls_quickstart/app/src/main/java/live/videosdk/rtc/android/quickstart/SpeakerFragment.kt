package live.videosdk.rtc.android.quickstart

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.lib.JsonUtils
import live.videosdk.rtc.android.listeners.MeetingEventListener
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
    private var tvMeetingId: TextView? = null
    private var tvHlsState: TextView? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is Activity) {
            mActivity = context
            meeting = (mActivity as MeetingActivity?)!!.meeting
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
        return view
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
                val config = JSONObject()
                val layout = JSONObject()
                JsonUtils.jsonPut(layout, "type", "SPOTLIGHT")
                JsonUtils.jsonPut(layout, "priority", "PIN")
                JsonUtils.jsonPut(layout, "gridSize", 4)
                JsonUtils.jsonPut(config, "layout", layout)
                JsonUtils.jsonPut(config, "orientation", "portrait")
                JsonUtils.jsonPut(config, "theme", "DARK")
                JsonUtils.jsonPut(config, "quality", "high")
                meeting!!.startHls(config)
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