package live.videosdk.rtc.android.quickstart;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.textfield.TextInputLayout;
import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.lib.JsonUtils;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import live.videosdk.rtc.android.model.PubSubPublishOptions;
import org.json.JSONException;
import org.json.JSONObject;

public class SpeakerFragment extends Fragment {

  private static Activity mActivity;
  private static Context mContext;
  private static Meeting meeting;
  private static String token;
  private boolean micEnabled = true;
  private boolean webcamEnabled = true;
  private boolean hlsEnabled = false;
  private Button btnMic, btnWebcam, btnHls, btnLeave, btnSetting;
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
      token = ((MeetingActivity) mActivity).getToken();
    }
  }

  @Override
  public View onCreateView(
    LayoutInflater inflater,
    ViewGroup container,
    Bundle savedInstanceState
  ) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_speaker, container, false);
    btnMic = view.findViewById(R.id.btnMic);
    btnWebcam = view.findViewById(R.id.btnWebcam);
    btnHls = view.findViewById(R.id.btnHLS);
    btnLeave = view.findViewById(R.id.btnLeave);
    btnSetting = view.findViewById(R.id.btnSetting);

    tvMeetingId = view.findViewById(R.id.tvMeetingId);
    tvHlsState = view.findViewById(R.id.tvHlsState);

    if (meeting != null) {
      tvMeetingId.setText("Meeting Id : " + meeting.getMeetingId());
      setActionListeners();
      // add Listener to the meeting
      meeting.addEventListener(meetingEventListener);
      // set SpeakerAdapter
      final RecyclerView rvParticipants = view.findViewById(
        R.id.rvParticipants
      );
      rvParticipants.setLayoutManager(new GridLayoutManager(mContext, 2));
      rvParticipants.setAdapter(new SpeakerAdapter(meeting));
    }

    btnSetting.setOnClickListener(v -> showSettingDialog());
    return view;
  }

  private void showSettingDialog() {
    Dialog dialog = new Dialog(mContext);

    dialog.setContentView(R.layout.setting_dialog_layout);
    dialog
      .getWindow()
      .setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      );

    TextInputLayout et_bgColor = dialog.findViewById(R.id.et_bgColor);
    TextInputLayout et_message = dialog.findViewById(R.id.et_message);

    Button btnBgColor = dialog.findViewById(R.id.btnBgColor);
    Button btnNotify = dialog.findViewById(R.id.btnNotify);

    btnBgColor.setOnClickListener(v -> {
      PubSubPublishOptions publishOptions = new PubSubPublishOptions();
      publishOptions.setPersist(true);

      // Sending the Message using the publish method
      meeting.pubSub.publish(
        "CHANGE_BACKGROUND",
        et_bgColor.getEditText().getText().toString().trim(),
        publishOptions
      );
      dialog.dismiss();
      Toast
        .makeText(mContext, "LiveStream Background Changed", Toast.LENGTH_SHORT)
        .show();
    });

    btnNotify.setOnClickListener(v1 -> {
      PubSubPublishOptions publishOptions = new PubSubPublishOptions();
      publishOptions.setPersist(true);

      // Sending the Message using the publish method
      meeting.pubSub.publish(
        "VIEWER_MESSAGE",
        et_message.getEditText().getText().toString().trim(),
        publishOptions
      );

      dialog.dismiss();
      Toast.makeText(mContext, "Notified User", Toast.LENGTH_SHORT).show();
    });

    dialog.show();

    dialog.getWindow().setBackgroundDrawableResource(R.color.dark_grey);
  }

  private final MeetingEventListener meetingEventListener = new MeetingEventListener() {
    @Override
    public void onMeetingLeft() {
      //unpin local participant
      meeting.getLocalParticipant().unpin("SHARE_AND_CAM");
      if (isAdded()) {
        Intent intents = new Intent(mContext, JoinActivity.class);
        intents.addFlags(
          Intent.FLAG_ACTIVITY_NEW_TASK |
          Intent.FLAG_ACTIVITY_CLEAR_TOP |
          Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intents);
        mActivity.finish();
      }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onHlsStateChanged(JSONObject HlsState) {
      if (HlsState.has("status")) {
        try {
          tvHlsState.setText(
            "Current HLS State : " + HlsState.getString("status")
          );
          if (HlsState.getString("status").equals("HLS_STARTED")) {
            hlsEnabled = true;
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
        Toast.makeText(mContext, "Mic Muted", Toast.LENGTH_SHORT).show();
      } else {
        meeting.unmuteMic();
        Toast.makeText(mContext, "Mic Enabled", Toast.LENGTH_SHORT).show();
      }
      micEnabled = !micEnabled;
    });

    btnWebcam.setOnClickListener(v -> {
      if (webcamEnabled) {
        meeting.disableWebcam();
        Toast.makeText(mContext, "Webcam Disabled", Toast.LENGTH_SHORT).show();
      } else {
        meeting.enableWebcam();
        Toast.makeText(mContext, "Webcam Enabled", Toast.LENGTH_SHORT).show();
      }
      webcamEnabled = !webcamEnabled;
    });

    btnLeave.setOnClickListener(v -> meeting.leave());

    btnHls.setOnClickListener(v -> {
      if (!hlsEnabled) {
        //Update your Custom Template URL here if you have deployed your own
        final String templateUrl =
          "https://lab.videosdk.live/react-custom-template-demo?meetingId=" +
          meeting.getMeetingId() +
          "&token=" +
          token;

        JSONObject bodyJson = new JSONObject();
        JsonUtils.jsonPut(bodyJson, "roomId", meeting.getMeetingId());
        JsonUtils.jsonPut(bodyJson, "templateUrl", templateUrl);

        JSONObject config = new JSONObject();
        JsonUtils.jsonPut(config, "orientation", "portrait");
        JsonUtils.jsonPut(bodyJson, "config", config);

        AndroidNetworking
          .post("https://api.videosdk.live/v2/hls/start")
          .addHeaders("Authorization", token) //we will pass the token in the Headers
          .addJSONObjectBody(bodyJson)
          .build()
          .getAsJSONObject(
            new JSONObjectRequestListener() {
              @Override
              public void onResponse(JSONObject response) {
                try {
                  Log.d("TAG", "onResponse: " + response.toString()); // result will have playbackHlsUrl
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }

              @Override
              public void onError(ANError anError) {
                anError.printStackTrace();
                Toast
                  .makeText(mContext, anError.getMessage(), Toast.LENGTH_SHORT)
                  .show();
              }
            }
          );
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
