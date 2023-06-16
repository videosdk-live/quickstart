package live.videosdk.rtc.android.quickstart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.Participant;
import live.videosdk.rtc.android.Stream;
import live.videosdk.rtc.android.VideoView;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.listeners.ParticipantEventListener;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.PeerViewHolder> {

    private final List<Participant> participants = new ArrayList<>();

    public ParticipantAdapter(Meeting meeting) {
        // adding the local participant(You) to the list
        participants.add(meeting.getLocalParticipant());

        // adding Meeting Event listener to get the participant join/leave event in the meeting.
        meeting.addEventListener(new MeetingEventListener() {
            @Override
            public void onParticipantJoined(Participant participant) {
                // add participant to the list
                participants.add(participant);
                notifyItemInserted(participants.size() - 1);
            }

            @Override
            public void onParticipantLeft(Participant participant) {
                int pos = -1;
                for (int i = 0; i < participants.size(); i++) {
                    if (participants.get(i).getId().equals(participant.getId())) {
                        pos = i;
                        break;
                    }
                }
                // remove participant from the list
                participants.remove(participant);

                if (pos >= 0) {
                    notifyItemRemoved(pos);
                }
            }
        });
    }

    @NonNull
    @Override
    public PeerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PeerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote_peer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PeerViewHolder holder, int position) {
        Participant participant = participants.get(position);

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
        return participants.size();
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

    @Override
    public void onViewRecycled(@NonNull PeerViewHolder holder) {
        holder.participantView.releaseSurfaceViewRenderer();
        super.onViewRecycled(holder);
    }
}
