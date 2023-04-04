import "./App.css";
import React, { useEffect, useMemo, useRef, useState } from "react";
import {
  MeetingProvider,
  MeetingConsumer,
  useMeeting,
  useParticipant,
  Constants,
  usePubSub,
} from "@videosdk.live/react-sdk";
import Hls from "hls.js";

import { authToken, createMeeting } from "./API";
import ReactPlayer from "react-player";
import FlyingEmojisOverlay from "./FlyingEmojisOverlay";

function JoinScreen({ getMeetingAndToken, setMode }) {
  const [meetingId, setMeetingId] = useState(null);
  const onClick = async (mode) => {
    setMode(mode);
    await getMeetingAndToken(meetingId);
  };
  return (
    <div className="container">
      <button onClick={() => onClick("CONFERENCE")}>Create Meeting</button>
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
      <button onClick={() => onClick("CONFERENCE")}>Join as Host</button>
      {" | "}
      <button onClick={() => onClick("VIEWER")}>Join as Viewer</button>
    </div>
  );
}

function ParticipantView(props) {
  const micRef = useRef(null);
  const { webcamStream, micStream, webcamOn, micOn, isLocal, displayName } =
    useParticipant(props.participantId);

  const videoStream = useMemo(() => {
    if (webcamOn && webcamStream) {
      const mediaStream = new MediaStream();
      mediaStream.addTrack(webcamStream.track);
      return mediaStream;
    }
  }, [webcamStream, webcamOn]);

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
        <ReactPlayer
          //
          playsinline // very very imp prop
          pip={false}
          light={false}
          controls={false}
          muted={true}
          playing={true}
          //
          url={videoStream}
          //
          height={"200px"}
          width={"300px"}
          onError={(err) => {
            console.log(err, "participant video error");
          }}
        />
      )}
    </div>
  );
}

function Controls() {
  const { leave, toggleMic, toggleWebcam, startHls, stopHls } = useMeeting();
  return (
    <div>
      <button onClick={() => leave()}>Leave</button>
      &emsp;|&emsp;
      <button onClick={() => toggleMic()}>toggleMic</button>
      <button onClick={() => toggleWebcam()}>toggleWebcam</button>
      &emsp;|&emsp;
      <button
        onClick={() => {
          startHls({
            layout: {
              type: "SPOTLIGHT",
              priority: "PIN",
              gridSize: "20",
            },
            theme: "DARK",
            mode: "video-and-audio",
            quality: "high",
            orientation: "landscape",
          });
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
    return participant.mode == Constants.modes.CONFERENCE;
  });
  return (
    <div>
      <p>Current HLS State: {hlsState}</p>
      <Controls />
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
    return participant.mode == Constants.modes.VIEWER;
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
    publish("CONFERENCE");
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
  // States to store downstream url and current HLS state
  const playerRef = useRef(null);
  const { hlsUrls, hlsState } = useMeeting();
  const { publish } = usePubSub("REACTION");
  //highlight-start
  function sendEmoji(emoji) {
    publish(emoji);
    // Dispatch custom event here so the local user can see their own emoji
    window.dispatchEvent(
      new CustomEvent("reaction_added", { detail: { emoji } })
    );
  }
  useEffect(() => {
    if (hlsUrls.downstreamUrl && hlsState == "HLS_PLAYABLE") {
      if (Hls.isSupported()) {
        const hls = new Hls({
          capLevelToPlayerSize: true,
          maxLoadingDelay: 4,
          minAutoBitrate: 0,
          autoStartLoad: true,
          defaultAudioCodec: "mp4a.40.2",
        });

        let player = document.querySelector("#hlsPlayer");

        hls.loadSource(hlsUrls.downstreamUrl);
        hls.attachMedia(player);
      } else {
        if (typeof playerRef.current?.play === "function") {
          playerRef.current.src = hlsUrls.downstreamUrl;
          playerRef.current.play();
        }
      }
    }
  }, [hlsUrls, hlsState]);

  return (
    <div>
      <div>
        <button
          onClick={() => {
            sendEmoji("confetti");
            publish("confetti");
          }}
        >
          Send 🎉 Reaction
        </button>

        <button
          onClick={() => {
            sendEmoji("clap");
            publish("clap");
          }}
        >
          Send 👏 Reaction
        </button>
      </div>
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
      if (mMeetingRef.current.localParticipant.mode == "CONFERENCE") {
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
        if (data.mode == Constants.modes.CONFERENCE) {
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
      <FlyingEmojisOverlay />
      <h3>Meeting Id: {props.meetingId}</h3>
      {joined && joined == "JOINED" ? (
        mMeeting.localParticipant.mode == Constants.modes.CONFERENCE ? (
          <SpeakerView />
        ) : mMeeting.localParticipant.mode == Constants.modes.VIEWER ? (
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
  const [mode, setMode] = useState("CONFERENCE");
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
