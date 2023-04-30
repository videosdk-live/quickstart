package live.videosdk.rtc.android.quickstart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoView;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;

public class SpeakerAdapter extends RecyclerView.Adapter<SpeakerAdapter.PeerViewHolder> {

    private List<Participant> participantList = new ArrayList<>();
    private final Meeting meeting;
    public SpeakerAdapter(Meeting meeting) {
        this.meeting = meeting;

        updateParticipantList();

        // adding Meeting Event listener to get the participant join/leave event in the meeting.
        meeting.addEventListener(new MeetingEventListener() {
            @Override
            public void onParticipantJoined(Participant participant) {
                // check participant join as Host/Speaker or not
                if (participant.getMode().equals("CONFERENCE")) {
                    // pin the participant
                    participant.pin("SHARE_AND_CAM");
                    // add participant in participantList
                    participantList.add(participant);
                }
                notifyDataSetChanged();
            }

            @Override
            public void onParticipantLeft(Participant participant) {
                int pos = -1;
                for (int i = 0; i < participantList.size(); i++) {
                    if (participantList.get(i).getId().equals(participant.getId())) {
                        pos = i;
                        break;
                    }
                }
                if(participantList.contains(participant)) {
                    // unpin participant who left the meeting
                    participant.unpin("SHARE_AND_CAM");
                    // remove participant from the list
                    participantList.remove(participant);
                }
                if (pos >= 0) {
                    notifyItemRemoved(pos);
                }
            }
        });
    }

    private void updateParticipantList() {
        participantList = new ArrayList<>();

        // adding the local participant(You) to the list
        participantList.add(meeting.getLocalParticipant());

        // adding participants who join as Host/Speaker
        Iterator<Participant> participants = meeting.getParticipants().values().iterator();
        for (int i = 0; i < meeting.getParticipants().size(); i++) {
            final Participant participant = participants.next();
            if (participant.getMode().equals("CONFERENCE")) {
                participant.pin("SHARE_AND_CAM");
                participantList.add(participant);
            }
        }
    }

    @NonNull
    @Override
    public PeerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PeerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote_peer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PeerViewHolder holder, int position) {
        Participant participant = participantList.get(position);

        holder.tvName.setText(participant.getDisplayName());

        // adding the initial video stream for the participant into the 'VideoView'
        for (Map.Entry<String, Stream> entry : participant.getStreams().entrySet()) {
            Stream stream = entry.getValue();
            if (stream.getKind().equalsIgnoreCase("video")) {
                holder.participantView.setVisibility(View.VISIBLE);
                VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                holder.participantView.addTrack(videoTrack);
                break;
            }
        }

        // add Listener to the participant which will update start or stop the video stream of that participant
        participant.addEventListener(new ParticipantEventListener() {
            @Override
            public void onStreamEnabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    holder.participantView.setVisibility(View.VISIBLE);
                    VideoTrack videoTrack = (VideoTrack) stream.getTrack();
                    holder.participantView.addTrack(videoTrack);
                }
            }

            @Override
            public void onStreamDisabled(Stream stream) {
                if (stream.getKind().equalsIgnoreCase("video")) {
                    holder.participantView.removeTrack();
                    holder.participantView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return participantList.size();
    }

    static class PeerViewHolder extends RecyclerView.ViewHolder {
        // 'VideoView' to show Video Stream
        public VideoView participantView;
        public TextView tvName;
        public View itemView;

        PeerViewHolder(@NonNull View view) {
            super(view);
            itemView = view;
            tvName = view.findViewById(R.id.tvName);
            participantView = view.findViewById(R.id.participantView);
        }
    }
}
