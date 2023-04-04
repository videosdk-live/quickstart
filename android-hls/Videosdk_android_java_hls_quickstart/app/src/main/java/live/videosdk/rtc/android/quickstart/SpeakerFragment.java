package live.videosdk.rtc.android.quickstart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.lib.JsonUtils;
import live.videosdk.rtc.android.listeners.MeetingEventListener;

public class SpeakerFragment extends Fragment {

    private static Activity mActivity;
    private static Context mContext;
    private static Meeting meeting;
    private boolean micEnabled = true;
    private boolean webcamEnabled = true;
    private boolean hlsEnabled = false;
    private Button btnMic, btnWebcam, btnHls, btnLeave;
    private TextView tvMeetingId, tvHlsState;

    public SpeakerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof Activity) {
            mActivity = (Activity) context;
            meeting = ((MeetingActivity) mActivity).getMeeting();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_speaker, container, false);
        btnMic = view.findViewById(R.id.btnMic);
        btnWebcam = view.findViewById(R.id.btnWebcam);
        btnHls = view.findViewById(R.id.btnHLS);
        btnLeave = view.findViewById(R.id.btnLeave);

        tvMeetingId = view.findViewById(R.id.tvMeetingId);
        tvHlsState = view.findViewById(R.id.tvHlsState);

        if (meeting != null) {
            tvMeetingId.setText("Meeting Id : " + meeting.getMeetingId());
            setActionListeners();
            // add Listener to the meeting
            meeting.addEventListener(meetingEventListener);
            // set SpeakerAdapter
            final RecyclerView rvParticipants = view.findViewById(R.id.rvParticipants);
            rvParticipants.setLayoutManager(new GridLayoutManager(mContext, 2));
            rvParticipants.setAdapter(new SpeakerAdapter(meeting));
        }
        return view;
    }

    private final MeetingEventListener meetingEventListener = new MeetingEventListener() {
        @Override
        public void onMeetingLeft() {
            //unpin local participant
            meeting.getLocalParticipant().unpin("SHARE_AND_CAM");
            if (isAdded()) {
                Intent intents = new Intent(mContext, JoinActivity.class);
                intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intents);
                mActivity.finish();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onHlsStateChanged(JSONObject HlsState) {
            if (HlsState.has("status")) {
                try {
                    tvHlsState.setText("Current HLS State : " + HlsState.getString("status"));
                    if (HlsState.getString("status").equals("HLS_STARTED")) {
                        hlsEnabled=true;
                        btnHls.setText("Stop HLS");
                    }
                    if (HlsState.getString("status").equals("HLS_STOPPED")) {
                        hlsEnabled = false;
                        btnHls.setText("Start HLS");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void setActionListeners() {
        btnMic.setOnClickListener(v -> {
            if (micEnabled) {
                meeting.muteMic();
                Toast.makeText(mContext,"Mic Muted",Toast.LENGTH_SHORT).show();
            } else {
                meeting.unmuteMic();
                Toast.makeText(mContext,"Mic Enabled",Toast.LENGTH_SHORT).show();
            }
            micEnabled=!micEnabled;
        });

        btnWebcam.setOnClickListener(v -> {
            if (webcamEnabled) {
                meeting.disableWebcam();
                Toast.makeText(mContext,"Webcam Disabled",Toast.LENGTH_SHORT).show();
            } else {
                meeting.enableWebcam();
                Toast.makeText(mContext,"Webcam Enabled",Toast.LENGTH_SHORT).show();
            }
            webcamEnabled=!webcamEnabled;
        });

        btnLeave.setOnClickListener(v -> meeting.leave());

        btnHls.setOnClickListener(v -> {
            if (!hlsEnabled) {
                JSONObject config = new JSONObject();
                JSONObject layout = new JSONObject();
                JsonUtils.jsonPut(layout, "type", "SPOTLIGHT");
                JsonUtils.jsonPut(layout, "priority", "PIN");
                JsonUtils.jsonPut(layout, "gridSize", 4);
                JsonUtils.jsonPut(config, "layout", layout);
                JsonUtils.jsonPut(config, "orientation", "portrait");
                JsonUtils.jsonPut(config, "theme", "DARK");
                JsonUtils.jsonPut(config, "quality", "high");
                meeting.startHls(config);
            } else {
                meeting.stopHls();
            }
        });

    }

    @Override
    public void onDestroy() {
        mContext = null;
        mActivity = null;
        if (meeting != null) {
            meeting.removeAllListeners();
            meeting = null;
        }
        super.onDestroy();
    }

}