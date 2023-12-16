package live.videosdk.rtc.android.quickstart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import live.videosdk.rtc.android.Meeting;
import live.videosdk.rtc.android.listeners.MeetingEventListener;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewerFragment extends Fragment {

  private Meeting meeting;

  protected StyledPlayerView playerView;
  private TextView waitingLayout;

  @Nullable
  protected ExoPlayer player;

  private DefaultHttpDataSource.Factory dataSourceFactory;
  private boolean startAutoPlay = true;
  private String playbackHlsUrl = "";
  private static Activity mActivity;
  private static Context mContext;

  public ViewerFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
    LayoutInflater inflater,
    ViewGroup container,
    Bundle savedInstanceState
  ) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_viewer, container, false);

    playerView = view.findViewById(R.id.player_view);

    waitingLayout = view.findViewById(R.id.waitingLayout);
    if (meeting != null) {
      // set MeetingId to TextView
      ((TextView) view.findViewById(R.id.meetingId)).setText(
          "Meeting Id : " + meeting.getMeetingId()
        );
      // leave the meeting on btnLeave click
      ((Button) view.findViewById(R.id.btnLeave)).setOnClickListener(v ->
          meeting.leave()
        );
      // add listener to meeting
      meeting.addEventListener(meetingEventListener);
    }
    return view;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    mContext = context;
    if (context instanceof Activity) {
      mActivity = (Activity) context;
      // get meeting object from MeetingActivity
      meeting = ((MeetingActivity) mActivity).getMeeting();
    }
  }

  private final MeetingEventListener meetingEventListener = new MeetingEventListener() {
    @Override
    public void onMeetingLeft() {
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
          if (
            HlsState.getString("status").equals("HLS_PLAYABLE") &&
            HlsState.has("playbackHlsUrl")
          ) {
            playbackHlsUrl = HlsState.getString("playbackHlsUrl");
            waitingLayout.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            // initialize player
            initializePlayer();
          }
          if (HlsState.getString("status").equals("HLS_STOPPED")) {
            // release the player
            releasePlayer();
            playbackHlsUrl = null;
            waitingLayout.setText("Host has stopped \n the live streaming");
            waitingLayout.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }
  };

  protected void initializePlayer() {
    if (player == null) {
      dataSourceFactory = new DefaultHttpDataSource.Factory();
      HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.fromUri(Uri.parse(this.playbackHlsUrl)));

      ExoPlayer.Builder playerBuilder = new ExoPlayer.Builder(
        /* context= */mContext
      );
      player = playerBuilder.build();
      // auto play when player is ready
      player.setPlayWhenReady(startAutoPlay);
      player.setMediaSource(mediaSource);
      // if you want display setting for player then remove this line
      playerView
        .findViewById(com.google.android.exoplayer2.ui.R.id.exo_settings)
        .setVisibility(View.GONE);
      playerView.setPlayer(player);
    }
    player.prepare();
  }

  protected void releasePlayer() {
    if (player != null) {
      player.release();
      player = null;
      dataSourceFactory = null;
      playerView.setPlayer(/* player= */null);
    }
  }

  @Override
  public void onDestroy() {
    mContext = null;
    mActivity = null;
    playbackHlsUrl = null;
    releasePlayer();
    if (meeting != null) {
      meeting.removeAllListeners();
      meeting = null;
    }
    super.onDestroy();
  }
}
