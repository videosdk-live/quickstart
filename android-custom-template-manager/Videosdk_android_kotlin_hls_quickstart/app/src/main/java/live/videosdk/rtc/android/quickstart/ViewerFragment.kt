package live.videosdk.rtc.android.quickstart

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.listeners.MeetingEventListener
import org.json.JSONException
import org.json.JSONObject

class ViewerFragment : Fragment() {
    private var meeting: Meeting? = null
    private var playerView: StyledPlayerView? = null
    private var waitingLayout: TextView? = null
    private var player: ExoPlayer? = null
    private var dataSourceFactory: DefaultHttpDataSource.Factory? = null
    private val startAutoPlay = true
    private var playbackHlsUrl: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_viewer, container, false)
        playerView = view.findViewById(R.id.player_view)
        waitingLayout = view.findViewById(R.id.waitingLayout)
        if (meeting != null) {
            // set MeetingId to TextView
            (view.findViewById<View>(R.id.meetingId) as TextView).text =
                "Meeting Id : " + meeting!!.meetingId
            // leave the meeting on btnLeave click
            (view.findViewById<View>(R.id.btnLeave) as Button).setOnClickListener { meeting!!.leave() }
            // add listener to meeting
            meeting!!.addEventListener(meetingEventListener)
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is Activity) {
            mActivity = context
            // get meeting object from MeetingActivity
            meeting = (mActivity as MeetingActivity?)!!.meeting
        }
    }

    private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingLeft() {
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
                    if (HlsState.getString("status") == "HLS_PLAYABLE" && HlsState.has("playbackHlsUrl")) {
                        playbackHlsUrl = HlsState.getString("playbackHlsUrl")
                        waitingLayout!!.visibility = View.GONE
                        playerView!!.visibility = View.VISIBLE
                        // initialize player
                        initializePlayer()
                    }
                    if (HlsState.getString("status") == "HLS_STOPPED") {
                        // release the player
                        releasePlayer()
                        playbackHlsUrl = null
                        waitingLayout!!.text = "Host has stopped \n the live streaming"
                        waitingLayout!!.visibility = View.VISIBLE
                        playerView!!.visibility = View.GONE
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initializePlayer() {
        if (player == null) {
            dataSourceFactory = DefaultHttpDataSource.Factory()
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory!!).createMediaSource(
                MediaItem.fromUri(Uri.parse(playbackHlsUrl))
            )
            val playerBuilder = ExoPlayer.Builder( /* context = */mContext!!)
            player = playerBuilder.build()
            // auto play when player is ready
            player!!.playWhenReady = startAutoPlay
            player!!.setMediaSource(mediaSource)
            // if you want display setting for player then remove this line
            playerView!!.findViewById<View>(com.google.android.exoplayer2.ui.R.id.exo_settings).visibility =
                View.GONE
            playerView!!.player = player
        }
        player!!.prepare()
    }

    private fun releasePlayer() {
        if (player != null) {
            player!!.release()
            player = null
            dataSourceFactory = null
            playerView!!.player = null
        }
    }

    override fun onDestroy() {
        mContext = null
        mActivity = null
        playbackHlsUrl = null
        releasePlayer()
        if (meeting != null) {
            meeting!!.removeAllListeners()
            meeting = null
        }
        super.onDestroy()
    }

    companion object {
        private var mActivity: Activity? = null
        private var mContext: Context? = null
    }
}