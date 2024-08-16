import "./App.css";
import React, { useEffect, useMemo, useRef, useState } from "react";
import {
  MeetingProvider,
  useMeeting,
  useParticipant,
  useCharacter,
} from "@videosdk.live/react-sdk";
import { authToken, createMeeting } from "./API";
import ReactPlayer from "react-player";

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

function CharacterView(props) {
  const micRef = useRef(null);
  const { webcamStream, micStream, isLocal, webcamOn, micOn, displayName } =
    useCharacter({
      id: props.characterId,
    });

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
    <div>
      <p>
        Character: {displayName} | Webcam: {webcamOn ? "ON" : "OFF"} | Mic:{" "}
        {micOn ? "ON" : "OFF"}
      </p>
      <audio ref={micRef} autoPlay playsInline muted={isLocal} />
      {webcamOn && (
        <ReactPlayer
          //
          playsinline // extremely crucial prop
          pip={false}
          light={false}
          controls={false}
          muted={true}
          playing={true}
          //
          url={videoStream}
          //
          height={"300px"}
          width={"300px"}
          onError={(err) => {
            console.log(err, "character video error");
          }}
        />
      )}
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
    <div>
      <p>
        Participant: {displayName} | Webcam: {webcamOn ? "ON" : "OFF"} | Mic:{" "}
        {micOn ? "ON" : "OFF"}
      </p>
      <audio ref={micRef} autoPlay playsInline muted={isLocal} />
      {webcamOn && (
        <ReactPlayer
          //
          playsinline // extremely crucial prop
          pip={false}
          light={false}
          controls={false}
          muted={true}
          playing={true}
          //
          url={videoStream}
          //
          height={"300px"}
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
  const { leave: leaveMeeting, toggleMic, toggleWebcam } = useMeeting({});
  const [text, setText] = useState("");

  /* 'text' | 'co_pilot' | 'auto_pilot' | 'vision_pilot' */
  const characterMode = "auto_pilot";

  //
  const characterId = "naina-v1";

  const [characterConfig, setCharacterConfig] = useState({
    id: characterId,
    characterMode: characterMode,
  });

  const { join, leave, sendMessage, characterState } = useCharacter(
    characterConfig,
    {
      onCharacterMessage: (d) => {
        console.log("character message :: ", d);
      },
      onCharacterStateChanged: (d) => {
        console.log("character state changed :: ", d);
      },
      onCharacterJoined: () => {
        console.log("character => character joined");
      },
      onCharacterLeft: () => {
        console.log("character => character left");
      },
    }
  );

  const joinCharacter = () => {
    join();
  };

  const leaveCharacter = () => {
    leave();
  };

  const handleSendMessage = (e) => {
    e.preventDefault();
    sendMessage(text);
  };

  return (
    <div>
      <button onClick={() => leaveMeeting()}>Leave</button>
      <button onClick={() => toggleMic()}>toggleMic</button>
      <button onClick={() => toggleWebcam()}>toggleWebcam</button>

      <div>
        <select
          value={characterConfig["id"]}
          onChange={(e) =>
            setCharacterConfig((old) => {
              return { ...old, id: e.target.value };
            })
          }
        >
          <option value="naina-v1">Naina v1 (Interviewer)</option>
          <option value="nancy-v1">Nancy V1 (Girlfriend)</option>
          <option value="call-buddy-v1">Call Buddy V1 (Call Assistant)</option>
          <option value="shivani-v1">Shivani V1 (Maths Teacher)</option>
        </select>
        <select
          value={characterConfig["characterMode"]}
          onChange={(e) =>
            setCharacterConfig((old) => {
              return { ...old, characterMode: e.target.value };
            })
          }
        >
          {/* 'text' | 'co_pilot' | 'auto_pilot' | 'vision_pilot' */}
          <option value="text">Text</option>
          <option value="co_pilot">Co Pilot</option>
          <option value="auto_pilot">Auto Pilot</option>
          <option value="vision_pilot">Vision Pilot</option>
        </select>
      </div>
      <p>Character State: {characterState}</p>
      <button onClick={joinCharacter}>join Character</button>
      <button onClick={leaveCharacter}>leave Character</button>
      <form onSubmit={handleSendMessage}>
        <input
          placeholder="say hi"
          value={text}
          onChange={(e) => setText(e.target.value)}
        />
        <button type="submit">send</button>
      </form>
    </div>
  );
}

const MemoizedControls = React.memo(Controls, (prev, next) => false);

function MeetingView(props) {
  const [joined, setJoined] = useState(null);
  //Get the method which will be used to join the meeting.
  //We will also get the participants list to display all participants
  const { join, participants, characters } = useMeeting({
    //callback for when meeting is joined successfully
    onMeetingJoined: () => {
      setJoined("JOINED");
    },
    //callback for when meeting is left
    onMeetingLeft: () => {
      props.onMeetingLeave();
    },
  });

  const joinMeeting = () => {
    setJoined("JOINING");
    join();
  };

  return (
    <div className="container">
      <h3>Meeting Id: {props.meetingId}</h3>
      {joined && joined === "JOINED" ? (
        <div>
          <MemoizedControls />
          {/* // For rendering all the participants in the meeting */}
          {[...characters.keys()].map((characterId) => (
            <CharacterView characterId={characterId} key={characterId} />
          ))}
          {[...participants.keys()].map((participantId) => (
            <ParticipantView
              participantId={participantId}
              key={participantId}
            />
          ))}
        </div>
      ) : joined && joined === "JOINING" ? (
        <p>Joining the meeting...</p>
      ) : (
        <button onClick={joinMeeting}>Join</button>
      )}
    </div>
  );
}

function App() {
  const [meetingId, setMeetingId] = useState(null);

  //Getting the meeting id by calling the api we just wrote
  const getMeetingAndToken = async (id) => {
    const meetingId =
      id == null ? await createMeeting({ token: authToken }) : id;
    setMeetingId(meetingId);
  };

  //This will set Meeting Id to null when meeting is left or ended
  const onMeetingLeave = () => {
    setMeetingId(null);
  };

  return authToken && meetingId ? (
    <MeetingProvider
      config={{
        meetingId,
        micEnabled: true,
        webcamEnabled: true,
        name: "Raman",
      }}
      token={authToken}
    >
      <MeetingView meetingId={meetingId} onMeetingLeave={onMeetingLeave} />
    </MeetingProvider>
  ) : (
    <JoinScreen getMeetingAndToken={getMeetingAndToken} />
  );
}

export default App;
