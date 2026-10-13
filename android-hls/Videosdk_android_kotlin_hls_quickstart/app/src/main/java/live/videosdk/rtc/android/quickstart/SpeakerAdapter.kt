package live.videosdk.rtc.android.quickstart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import live.videosdk.rtc.android.MediaType
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.PinType
import live.videosdk.rtc.android.RTCStream
import live.videosdk.rtc.android.Room
import live.videosdk.rtc.android.VideoView
import live.videosdk.rtc.android.lib.LeaveReason
import live.videosdk.rtc.android.lib.Mode
import live.videosdk.rtc.android.lib.VideoSDKException
import live.videosdk.rtc.android.listeners.ParticipantEventListener
import live.videosdk.rtc.android.listeners.RoomEventListener
import videosdk.org.webrtc.VideoTrack

class SpeakerAdapter(private val room: Room, private val scope: CoroutineScope) :
    RecyclerView.Adapter<SpeakerAdapter.PeerViewHolder?>() {
    private var participantList: MutableList<Participant> = ArrayList()

    init {
        updateParticipantList()

        // adding Room Event listener to get the participant join/leave event in the meeting.
        room.addEventListener(object : RoomEventListener() {
            override fun onParticipantJoined(participant: Participant) {
                // check participant join as Host/Speaker or not
                if (participant.mode == Mode.SEND_AND_RECV) {
                    // pin the participant
                    pin(participant)
                    // add participant in participantList
                    participantList.add(participant)
                }
                notifyDataSetChanged()
            }

            override fun onParticipantLeft(participant: Participant, reason: LeaveReason) {
                val pos = participantList.indexOfFirst { it.id == participant.id }
                if (pos >= 0) {
                    // remove participant from participantList
                    participantList.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }
        })
    }

    private fun updateParticipantList() {
        // adding the local participant(You) to the list
        participantList.add(room.localParticipant)

        // adding participants who join as Host/Speaker
        for (participant in room.participants.values) {
            if (participant.mode == Mode.SEND_AND_RECV) {
                pin(participant)
                participantList.add(participant)
            }
        }
    }

    // pin() is a suspend function that throws VideoSDKException
    private fun pin(participant: Participant) {
        scope.launch {
            try {
                participant.pin(PinType.SHARE_AND_CAM)
            } catch (e: VideoSDKException) {
                // the participant left, or the room is reconnecting
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        return PeerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_remote_peer, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        val participant = participantList[position]
        holder.tvName.text = participant.displayName

        // add Listener to the participant which will update start or stop the video stream of that participant.
        // addEventListener also calls onStreamEnabled right away for the streams the participant already has.
        participant.addEventListener(object : ParticipantEventListener() {
            override fun onStreamEnabled(stream: RTCStream) {
                if (stream.kind == MediaType.VIDEO) {
                    holder.participantView.visibility = View.VISIBLE
                    val videoTrack = stream.track as VideoTrack
                    holder.participantView.addTrack(videoTrack)
                }
            }

            override fun onStreamDisabled(stream: RTCStream) {
                if (stream.kind == MediaType.VIDEO) {
                    holder.participantView.removeTrack()
                    holder.participantView.visibility = View.GONE
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return participantList.size
    }

    class PeerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 'VideoView' to show Video Stream
        var participantView: VideoView
        var tvName: TextView

        init {
            tvName = view.findViewById(R.id.tvName)
            participantView = view.findViewById(R.id.participantView)
        }
    }
}
