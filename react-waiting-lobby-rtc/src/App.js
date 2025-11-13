import "./App.css";
import React, { useEffect, useMemo, useRef, useState } from "react";
import {
  MeetingProvider,
  MeetingConsumer,
  useMeeting,
  useParticipant,
  VideoPlayer
} from "@videosdk.live/react-sdk";
import { createMeeting, hostToken, guestToken } from "./API";

function JoinScreen({ getMeetingAndToken }) {
  const [meetingId, setMeetingId] = useState(null);
  const onClick = async () => {
    await getMeetingAndToken(meetingId);
  };
  return (
    <div>
      <input
        type="text"
        placeholder="Enter Meeting Id"
        onChange={(e) => {
          setMeetingId(e.target.value);
        }}
      />
      <button onClick={onClick}>Join</button>
      {" or "}
      <button onClick={onClick}>Create Meeting</button>
    </div>
  );
}

function ParticipantView(props) {
  const micRef = useRef(null);
  const {  micStream, webcamOn, micOn, isLocal, displayName } =
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

function Controls() {
  const { leave, toggleMic, toggleWebcam } = useMeeting();
  return (
    <div>
      <button onClick={() => leave()}>Leave</button>
      <button onClick={() => toggleMic()}>toggleMic</button>
      <button onClick={() => toggleWebcam()}>toggleWebcam</button>
    </div>
  );
}

function MeetingView(props) {
  const { setIsHost } = props;
  const [joined, setJoined] = useState(null);
  const [requestedEntries, setRequestedEntries] = useState([]);

  const { join, participants } = useMeeting({
    onMeetingJoined: () => {
      setJoined("JOINED");
    },
    onMeetingLeft: () => {
      props.onMeetingLeave();
    },
    onEntryRequested: (data) => {
      console.log("entry requested");
      const { participantId, name, allow, deny } = data;

      setRequestedEntries((s) => [...s, { participantId, name, allow, deny }]);
    },
    onEntryResponded(participantId, decision) {
      console.log("entry responded");

      setRequestedEntries((s) =>
        s.filter((p) => p.participantId !== participantId)
      );

      if (decision === "allowed") {
        // entry allowed
      } else {
        // entry denied
      }
    },
  });
  const joinMeeting = () => {
    setJoined("JOINING");
    join();
  };

  const joinHostMeeting = () => {
    setIsHost(true);
    setJoined("JOINING");
    join();
  };

  return (
    <div className="container">
      <h3>Meeting Id: {props.meetingId}</h3>
      {joined && joined == "JOINED" ? (
        <div>
          <Controls />
          {requestedEntries.map(({ participantId, name, allow, deny }) => {
            return (
              <>
                <p>{name} wants to join Meeting</p>
                <button onClick={allow}>Allow</button>
                <button onClick={deny} style={{ marginLeft: 8 }}>
                  Deny
                </button>
              </>
            );
          })}
          {[...participants.keys()].map((participantId) => (
            <ParticipantView
              participantId={participantId}
              key={participantId}
            />
          ))}
        </div>
      ) : joined && joined == "JOINING" ? (
        <p>Joining the meeting...</p>
      ) : (
        <>
          <button onClick={joinHostMeeting}>Join As a Host</button>
          <button onClick={joinMeeting} style={{ marginLeft: 8 }}>
            Join As a Guest
          </button>
        </>
      )}
    </div>
  );
}

function App() {
  const [meetingId, setMeetingId] = useState(null);
  const [isHost, setIsHost] = useState(false);

  const getMeetingAndToken = async (id) => {
    const meetingId =
      id == null ? await createMeeting({ token: hostToken }) : id;

    if (id) {
      setMeetingId(meetingId);
    } else {
      setIsHost(true);
      setMeetingId(meetingId);
    }
  };

  const onMeetingLeave = () => {
    setMeetingId(null);
  };

  return (hostToken || guestToken) && meetingId ? (
    <MeetingProvider
      config={{
        meetingId,
        micEnabled: true,
        webcamEnabled: true,
        name: "C.V. Raman",
      }}
      token={isHost ? hostToken : guestToken}
    >
      <MeetingConsumer>
        {() => (
          <MeetingView
            meetingId={meetingId}
            onMeetingLeave={onMeetingLeave}
            setIsHost={setIsHost}
          />
        )}
      </MeetingConsumer>
    </MeetingProvider>
  ) : (
    <JoinScreen getMeetingAndToken={getMeetingAndToken} />
  );
}

export default App;
