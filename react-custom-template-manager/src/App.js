import "./App.css";
import React, { useEffect, useMemo, useRef, useState } from "react";
import {
  MeetingProvider,
  MeetingConsumer,
  useMeeting,
  useParticipant,
  Constants,
  usePubSub,
  VideoPlayer
} from "@videosdk.live/react-sdk";
import Hls from "hls.js";

import { authToken, createMeeting } from "./API";

function JoinScreen({ getMeetingAndToken, setMode }) {
  const [meetingId, setMeetingId] = useState(null);
  const onClick = async (mode) => {
    setMode(mode);
    await getMeetingAndToken(meetingId);
  };
  return (
    <div className="container">
      <button onClick={() => onClick("SEND_AND_RECV")}>Create Meeting</button>
      <br />
      <br />
      {" or "}
      <br />
      <br />
      <input
        type="text"
        placeholder="Enter Meeting Id"
        onChange={(e) => {
          setMeetingId(e.target.value);
        }}
      />
      <br />
      <br />
      <button onClick={() => onClick("SEND_AND_RECV")}>Join as Host</button>
      {" | "}
      <button onClick={() => onClick("SIGNALLING_ONLY")}>Join as Viewer</button>
    </div>
  );
}

function ParticipantView(props) {
  const micRef = useRef(null);
  const { micStream, webcamOn, micOn, isLocal, displayName } =
    useParticipant(props.participantId);


  useEffect(() => {
    if (micRef.current) {
      if (micOn && micStream) {
        const mediaStream = new MediaStream();
        mediaStream.addTrack(micStream.track);

        micRef.current.srcObject = mediaStream;
        micRef.current
          .play()
          .catch((error) =>
            console.error("videoElem.current.play() failed", error)
          );
      } else {
        micRef.current.srcObject = null;
      }
    }
  }, [micStream, micOn]);

  return (
    <div key={props.participantId}>
      <p>
        Participant: {displayName} | Webcam: {webcamOn ? "ON" : "OFF"} | Mic:{" "}
        {micOn ? "ON" : "OFF"}
      </p>
      <audio ref={micRef} autoPlay muted={isLocal} />
      {webcamOn && (
        <VideoPlayer
          participantId={props.participantId} // Required
          type="video" // "video" or "share"
          containerStyle={{
            height: "200px",
            width: "300px",
          }}
          className="h-full"
          classNameVideo="h-full"
          videoStyle={{}}
        />
      )}
    </div>
  );
}

function TemplateManager() {
  const [bgColor, setBgColor] = useState(null);
  const [viewerMessage, setViewerMessage] = useState(null);

  const { publish: publishBgColor } = usePubSub("CHANGE_BACKGROUND");
  const { publish: publishViewerMessage } = usePubSub("VIEWER_MESSAGE");

  function handleChangeLivestreamBackground() {
    publishBgColor(bgColor, { persist: true });
    setBgColor(null);
  }

  function handleViewerMessage() {
    publishViewerMessage(viewerMessage, { persist: true });
    setViewerMessage(null);
  }

  return (
    <div>
      <div>
        <input
          type="text"
          placeholder="Enter Background Color"
          onChange={(e) => {
            setBgColor(e.target.value);
          }}
          value={bgColor}
        />
        <button onClick={handleChangeLivestreamBackground}>
          Change Livestream Background
        </button>
      </div>
      <div>
        <input
          type="text"
          placeholder="Enter Message for Viewers"
          onChange={(e) => {
            setViewerMessage(e.target.value);
          }}
        />
        <button onClick={handleViewerMessage}>Notify Viewers</button>
      </div>
    </div>
  );
}

function Controls() {
  const { leave, toggleMic, toggleWebcam, stopHls, meetingId } = useMeeting();
  return (
    <div>
      <button onClick={() => leave()}>Leave</button>
      &emsp;|&emsp;
      <button onClick={() => toggleMic()}>toggleMic</button>
      <button onClick={() => toggleWebcam()}>toggleWebcam</button>
      &emsp;|&emsp;
      <button
        onClick={async () => {
          const url = `https://api.videosdk.live/v2/hls/start`;
          const templateUrl = `https://lab.videosdk.live/react-custom-template-demo?meetingId=${meetingId}&token=${authToken}`;
          const options = {
            method: "POST",
            headers: {
              Authorization: authToken,
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              roomId: meetingId,
              templateUrl: templateUrl,
            }),
          };

          const result = await fetch(url, options)
            .then((response) => response.json()) //result will have meeting id
            .catch((error) => console.error("error", error));
        }}
      >
        Start HLS
      </button>
      <button onClick={() => stopHls()}>Stop HLS</button>
    </div>
  );
}

function SpeakerView() {
  const { participants, hlsState } = useMeeting();
  const speakers = [...participants.values()].filter((participant) => {
    return participant.mode == Constants.modes.SEND_AND_RECV;
  });
  return (
    <div>
      <p>Current HLS State: {hlsState}</p>
      <Controls />
      <br />
      <TemplateManager />
      {speakers.map((participant) => (
        <ParticipantView participantId={participant.id} key={participant.id} />
      ))}
      <ViewerList />
    </div>
  );
}

function ViewerList() {
  const { participants } = useMeeting();

  //Filtering only viewer participant
  const viewers = [...participants.values()].filter((participant) => {
    return participant.mode == Constants.modes.SIGNALLING_ONLY;
  });

  return (
    <div>
      <p>Viewer list: </p>
      {viewers.map((participant) => {
        return <ViewerListItem participantId={participant.id} />;
      })}
    </div>
  );
}

function ViewerListItem({ participantId }) {
  const { displayName } = useParticipant(participantId);
  const { publish } = usePubSub(`CHANGE_MODE_${participantId}`);
  const onClickRequestJoinLiveStream = () => {
    publish("SEND_AND_RECV");
  };
  return (
    <div>
      {displayName}{" "}
      <button
        onClick={() => {
          onClickRequestJoinLiveStream();
        }}
      >
        Request to join Livestream
      </button>
    </div>
  );
}

function ViewerView() {
  // States to store playbackHls url and current HLS state
  const playerRef = useRef(null);
  const { hlsUrls, hlsState } = useMeeting();

  useEffect(() => {
    if (hlsUrls.playbackHlsUrl && hlsState == "HLS_PLAYABLE") {
      if (Hls.isSupported()) {
        const hls = new Hls({
          maxLoadingDelay: 1, // max video loading delay used in automatic start level selection
          defaultAudioCodec: "mp4a.40.2", // default audio codec
          maxBufferLength: 0, // If buffer length is/become less than this value, a new fragment will be loaded
          maxMaxBufferLength: 1, // Hls.js will never exceed this value
          startLevel: 0, // Start playback at the lowest quality level
          startPosition: -1, // set -1 playback will start from intialtime = 0
          maxBufferHole: 0.001, // 'Maximum' inter-fragment buffer hole tolerance that hls.js can cope with when searching for the next fragment to load.
          highBufferWatchdogPeriod: 0, // if media element is expected to play and if currentTime has not moved for more than highBufferWatchdogPeriod and if there are more than maxBufferHole seconds buffered upfront, hls.js will jump buffer gaps, or try to nudge playhead to recover playback.
          nudgeOffset: 0.05, // In case playback continues to stall after first playhead nudging, currentTime will be nudged evenmore following nudgeOffset to try to restore playback. media.currentTime += (nb nudge retry -1)*nudgeOffset
          nudgeMaxRetry: 1, // Max nb of nudge retries before hls.js raise a fatal BUFFER_STALLED_ERROR
          maxFragLookUpTolerance: 0.1, // This tolerance factor is used during fragment lookup.
          liveSyncDurationCount: 1, // if set to 3, playback will start from fragment N-3, N being the last fragment of the live playlist
          abrEwmaFastLive: 1, // Fast bitrate Exponential moving average half-life, used to compute average bitrate for Live streams.
          abrEwmaSlowLive: 3, // Slow bitrate Exponential moving average half-life, used to compute average bitrate for Live streams.
          abrEwmaFastVoD: 1, // Fast bitrate Exponential moving average half-life, used to compute average bitrate for VoD streams
          abrEwmaSlowVoD: 3, // Slow bitrate Exponential moving average half-life, used to compute average bitrate for VoD streams
          maxStarvationDelay: 1, // ABR algorithm will always try to choose a quality level that should avoid rebuffering
        });

        let player = document.querySelector("#hlsPlayer");

        hls.loadSource(hlsUrls.playbackHlsUrl);
        hls.attachMedia(player);
      } else {
        if (typeof playerRef.current?.play === "function") {
          playerRef.current.src = hlsUrls.playbackHlsUrl;
          playerRef.current.play();
        }
      }
    }
  }, [hlsUrls, hlsState]);

  return (
    <div>
      {hlsState != "HLS_PLAYABLE" ? (
        <div>
          <p>HLS has not started yet or is stopped</p>
        </div>
      ) : (
        hlsState == "HLS_PLAYABLE" && (
          <div>
            <video
              ref={playerRef}
              id="hlsPlayer"
              autoPlay={true}
              controls
              style={{ width: "100%", height: "100%" }}
              playsinline
              playsInline
              muted={true}
              playing
              onError={(err) => {
                console.log(err, "hls video error");
              }}
            ></video>
          </div>
        )
      )}
    </div>
  );
}

function Container(props) {
  const [joined, setJoined] = useState(null);
  const { join, localParticipant, changeMode } = useMeeting();
  const mMeeting = useMeeting({
    onMeetingJoined: () => {
      if (mMeetingRef.current.localParticipant.mode == "SEND_AND_RECV") {
        mMeetingRef.current.localParticipant.pin();
      }
      setJoined("JOINED");
    },
    onMeetingLeft: () => {
      props.onMeetingLeave();
    },
    onParticipantModeChanged: (data) => {
      const localParticipant = mMeetingRef.current.localParticipant;
      if (data.participantId == localParticipant.id) {
        if (data.mode == Constants.modes.SEND_AND_RECV) {
          localParticipant.pin();
        } else {
          localParticipant.unpin();
        }
      }
    },
    onError: (error) => {
      alert(error.message);
    },
    onHlsStateChanged: (data) => {
      console.log("HLS State Changed", data);
    },
  });
  const joinMeeting = () => {
    setJoined("JOINING");
    join();
  };

  const mMeetingRef = useRef(mMeeting);
  useEffect(() => {
    mMeetingRef.current = mMeeting;
  }, [mMeeting]);

  const [joinLivestreamRequest, setJoinLivestreamRequest] = useState();

  const pubsub = usePubSub(`CHANGE_MODE_${localParticipant?.id}`, {
    onMessageReceived: (pubSubMessage) => {
      setJoinLivestreamRequest(pubSubMessage);
    },
  });

  return (
    <div className="container">
      <h3>Meeting Id: {props.meetingId}</h3>
      {joined && joined == "JOINED" ? (
        mMeeting.localParticipant.mode == Constants.modes.SEND_AND_RECV ? (
          <SpeakerView />
        ) : mMeeting.localParticipant.mode == Constants.modes.SIGNALLING_ONLY ? (
          <>
            {joinLivestreamRequest && (
              <div>
                {joinLivestreamRequest.senderName} requested you to join
                Livestream
                <button
                  onClick={() => {
                    changeMode(joinLivestreamRequest.message);
                    setJoinLivestreamRequest(null);
                  }}
                >
                  Accept
                </button>
                <button
                  onClick={() => {
                    setJoinLivestreamRequest(null);
                  }}
                >
                  Reject
                </button>
              </div>
            )}
            <ViewerView />
          </>
        ) : null
      ) : joined && joined == "JOINING" ? (
        <p>Joining the meeting...</p>
      ) : (
        <button onClick={joinMeeting}>Join</button>
      )}
    </div>
  );
}

function App() {
  const [meetingId, setMeetingId] = useState(null);
  const [mode, setMode] = useState("SEND_AND_RECV");
  const getMeetingAndToken = async (id) => {
    const meetingId =
      id == null ? await createMeeting({ token: authToken }) : id;
    setMeetingId(meetingId);
  };

  const onMeetingLeave = () => {
    setMeetingId(null);
  };

  return authToken && meetingId ? (
    <MeetingProvider
      config={{
        meetingId,
        micEnabled: true,
        webcamEnabled: true,
        name: "C.V. Raman",
        mode: mode,
      }}
      token={authToken}
    >
      <MeetingConsumer>
        {() => (
          <Container meetingId={meetingId} onMeetingLeave={onMeetingLeave} />
        )}
      </MeetingConsumer>
    </MeetingProvider>
  ) : (
    <JoinScreen getMeetingAndToken={getMeetingAndToken} setMode={setMode} />
  );
}

export default App;
