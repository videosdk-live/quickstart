package live.videosdk.rtc.android.quickstart

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import live.videosdk.rtc.android.Room
import live.videosdk.rtc.android.lib.HLSState
import live.videosdk.rtc.android.lib.LeaveReason
import live.videosdk.rtc.android.listeners.RoomEventListener
import live.videosdk.rtc.android.model.HLSUrl

class ViewerFragment : Fragment() {
    private var room: Room? = null
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
        if (room != null) {
            // set MeetingId to TextView
            (view.findViewById<View>(R.id.meetingId) as TextView).text =
                "Meeting Id : " + room!!.id
            // leave the meeting on btnLeave click
            (view.findViewById<View>(R.id.btnLeave) as Button).setOnClickListener { (mActivity as MeetingActivity?)?.leaveRoom() }
            // add listener to room
            room!!.addEventListener(roomEventListener)
            // the stream may already be playable: its state arrived before this fragment existed
            (mActivity as MeetingActivity?)?.playbackHlsUrl?.let { play(it) }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is Activity) {
            mActivity = context
            // get room object from MeetingActivity
            room = (mActivity as MeetingActivity?)!!.room
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
            if (state == HLSState.HLS_PLAYABLE && hlsUrl?.playbackHlsUrl != null) {
                play(hlsUrl.playbackHlsUrl!!)
            }
            if (state == HLSState.HLS_STOPPED) {
                // release the player
                releasePlayer()
                playbackHlsUrl = null
                waitingLayout!!.text = "Host has stopped \n the live streaming"
                waitingLayout!!.visibility = View.VISIBLE
                playerView!!.visibility = View.GONE
            }
        }
    }

    private fun play(url: String) {
        playbackHlsUrl = url
        waitingLayout!!.visibility = View.GONE
        playerView!!.visibility = View.VISIBLE
        // initialize player
        initializePlayer()
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
        if (room != null) {
            room!!.removeAllEventListeners()
            room = null
        }
        super.onDestroy()
    }

    companion object {
        private var mActivity: Activity? = null
        private var mContext: Context? = null
    }
}