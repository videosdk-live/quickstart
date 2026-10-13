package live.videosdk.rtc.android.quickstart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import live.videosdk.rtc.android.MediaType
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.RTCStream
import live.videosdk.rtc.android.Room
import live.videosdk.rtc.android.VideoView
import live.videosdk.rtc.android.lib.LeaveReason
import live.videosdk.rtc.android.listeners.ParticipantEventListener
import live.videosdk.rtc.android.listeners.RoomEventListener
import videosdk.org.webrtc.VideoTrack

class ParticipantAdapter(room: Room) :
    RecyclerView.Adapter<ParticipantAdapter.PeerViewHolder>() {
    private val participants: MutableList<Participant> = ArrayList()

    init {
        // adding Room Event listener to get the participant join/leave event in the meeting.
        room.addEventListener(object : RoomEventListener() {
            override fun onRoomJoined(switchRoomId: String?) {
                // adding the local participant(You) to the list once the join completes
                if (participants.none { it.isLocal }) {
                    participants.add(0, room.localParticipant)
                    notifyItemInserted(0)
                }
            }

            override fun onParticipantJoined(participant: Participant) {
                // add participant to the list
                participants.add(participant)
                notifyItemInserted(participants.size - 1)
            }

            override fun onParticipantLeft(participant: Participant, reason: LeaveReason) {
                val pos = participants.indexOfFirst { it.id == participant.id }
                // remove participant from the list
                if (pos >= 0) {
                    participants.removeAt(pos)
                    notifyItemRemoved(pos)
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerViewHolder {
        return PeerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_remote_peer, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PeerViewHolder, position: Int) {
        val participant = participants[position]
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
        return participants.size
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

    override fun onViewRecycled(holder: PeerViewHolder) {
        holder.participantView.releaseSurfaceViewRenderer();
        super.onViewRecycled(holder)
    }
}
