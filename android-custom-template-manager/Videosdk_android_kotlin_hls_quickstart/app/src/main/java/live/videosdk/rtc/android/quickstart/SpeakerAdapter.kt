package live.videosdk.rtc.android.quickstart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import live.videosdk.rtc.android.Meeting
import live.videosdk.rtc.android.Participant
import live.videosdk.rtc.android.Stream
import live.videosdk.rtc.android.VideoView
import live.videosdk.rtc.android.listeners.MeetingEventListener
import live.videosdk.rtc.android.listeners.ParticipantEventListener
import org.webrtc.VideoTrack

class SpeakerAdapter(private val meeting: Meeting) :
    RecyclerView.Adapter<SpeakerAdapter.PeerViewHolder?>() {
    private var participantList: MutableList<Participant> = ArrayList()

    init {
        updateParticipantList()

        // adding Meeting Event listener to get the participant join/leave event in the meeting.
        meeting.addEventListener(object : MeetingEventListener() {
            override fun onParticipantJoined(participant: Participant) {
                // check participant join as Host/Speaker or not
                if (participant.mode == "CONFERENCE") {
                    // pin the participant
                    participant.pin("SHARE_AND_CAM")
                    // add participant in participantList
                    participantList.add(participant)
                }
                notifyDataSetChanged()
            }

            override fun onParticipantLeft(participant: Participant) {
                var pos = -1
                for (i in participantList.indices) {
                    if (participantList[i].id == participant.id) {
                        pos = i
                        break
                    }
                }
                if (participantList.contains(participant)) {
                    // unpin participant who left the meeting
                    participant.unpin("SHARE_AND_CAM")
                    // remove participant from participantList
                    participantList.remove(participant)
                }
                if (pos >= 0) {
                    notifyItemRemoved(pos)
                }
            }
        })
    }

    private fun updateParticipantList() {
        // adding the local participant(You) to the list
        participantList.add(meeting.localParticipant)

        // adding participants who join as Host/Speaker
        val participants: Iterator<Participant> = meeting.participants.values.iterator()
        for (i in 0 until meeting.participants.size) {
            val participant = participants.next()
            if (participant.mode == "CONFERENCE") {
                participant.pin("SHARE_AND_CAM")
                participantList.add(participant)
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

        // adding the initial video stream for the participant into the 'VideoView'
        for ((_, stream) in participant.streams) {
            if (stream.kind.equals("video", ignoreCase = true)) {
                holder.participantView.visibility = View.VISIBLE
                val videoTrack = stream.track as VideoTrack
                holder.participantView.addTrack(videoTrack)
                break
            }
        }

        // add Listener to the participant which will update start or stop the video stream of that participant
        participant.addEventListener(object : ParticipantEventListener() {
            override fun onStreamEnabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
                    holder.participantView.visibility = View.VISIBLE
                    val videoTrack = stream.track as VideoTrack
                    holder.participantView.addTrack(videoTrack)
                }
            }

            override fun onStreamDisabled(stream: Stream) {
                if (stream.kind.equals("video", ignoreCase = true)) {
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