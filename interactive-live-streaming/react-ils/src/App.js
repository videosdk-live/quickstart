import "./App.css";
import React, { useEffect, useRef, useState } from "react";
import {
  MeetingProvider,
  useMeeting,
  useParticipant,
  Constants,
} from "@videosdk.live/react-sdk";
import { authToken, createStream } from "./API";

// Join Screen - Handles joining or creating a stream
function JoinView({ initializeStream, setMode }) {
  const [streamId, setStreamId] = useState("");

  const handleAction = async (mode) => {
    // Sets the mode (Host or Audience) and initializes the stream
    setMode(mode);
    await initializeStream(streamId);
  };

  return (
    <div className="container">
      {/* Button to create a new stream */}
      <button onClick={() => handleAction(Constants.modes.SEND_AND_RECV)}>
        Create Live Stream as Host
      </button>
      {/* Input field for entering an existing Stream ID */}
      <input
        type="text"
        placeholder="Enter Stream Id"
        onChange={(e) => setStreamId(e.target.value)}
      />
      {/* Button to join as a host */}
      <button onClick={() => handleAction(Constants.modes.SEND_AND_RECV)}>
        Join as Host
      </button>
      {/* Button to join as an audience member */}
      <button onClick={() => handleAction(Constants.modes.RECV_ONLY)}>
        Join as Audience
      </button>
    </div>
  );
}

// Component to manage live stream container and session joining
function LSContainer({ streamId, onLeave }) {
  const [joined, setJoined] = useState(false); // Track if the user has joined the stream

  const { join } = useMeeting({
    onMeetingJoined: () => setJoined(true), // Set `joined` to true when successfully joined
    onMeetingLeft: onLeave, // Handle the leave stream event
    onError: (error) => alert(error.message), // Display an alert on encountering an error
  });

  return (
    <div className="container">
      <h3>Stream Id: {streamId}</h3>
      {/* Show the stream view if joined, otherwise display the "Join Stream" button */}
      {joined ? <StreamView /> : <button onClick={join}>Join Stream</button>}
    </div>
  );
}

// Component to display the live stream view
function StreamView() {
  const { participants } = useMeeting(); // Access participants using the VideoSDK useMeeting hook

  return (
    <div>
      <LSControls /> {/* Render live stream controls */}
      {[...participants.values()]
        .filter((p) => p.mode === Constants.modes.SEND_AND_RECV) // Filter participants in SEND_AND_RECV mode
        .map((p) => (
          <Participant participantId={p.id} key={p.id} /> // Render each participant's view
        ))}
    </div>
  );
}

// Component to render audio and video streams for a participant
function Participant({ participantId }) {
  const { webcamStream, micStream, webcamOn, micOn, isLocal, displayName } =
    useParticipant(participantId);

  const audioRef = useRef(null); // Reference for audio element
  const videoRef = useRef(null); // Reference for video element

  // Function to attach or clear the stream
  const setupStream = (stream, ref, condition) => {
    if (ref.current && stream) {
      ref.current.srcObject = condition
        ? new MediaStream([stream.track])
        : null;
      condition && ref.current.play().catch(console.error);
    }
  };

  useEffect(() => setupStream(micStream, audioRef, micOn), [micStream, micOn]); // Handle mic stream
  useEffect(
    () => setupStream(webcamStream, videoRef, webcamOn),
    [webcamStream, webcamOn]
  ); // Handle webcam stream

  return (
    <div>
      <p>
        {displayName} | Webcam: {webcamOn ? "ON" : "OFF"} | Mic:{" "}
        {micOn ? "ON" : "OFF"}
      </p>
      <audio ref={audioRef} autoPlay muted={isLocal} /> {/* Play mic stream */}
      {webcamOn && (
        <video
          ref={videoRef}
          autoPlay
          muted={isLocal}
          height="200"
          width="300"
        /> /* Display webcam stream */
      )}
    </div>
  );
}

// Component for managing stream controls
function LSControls() {
  const { leave, toggleMic, toggleWebcam, changeMode, meeting } = useMeeting(); // Access methods
  const currentMode = meeting.localParticipant.mode; // Get the current participant's mode

  return (
    <div className="controls">
      {/* Button to leave the stream */}
      <button onClick={leave}>Leave</button>

      {/* Show mic and webcam toggles if in SEND_AND_RECV mode */}
      {currentMode === Constants.modes.SEND_AND_RECV && (
        <>
          <button onClick={toggleMic}>Toggle Mic</button>{" "}
          {/* Mute/unmute mic */}
          <button onClick={toggleWebcam}>Toggle Camera</button>{" "}
          {/* Enable/disable Camera */}
        </>
      )}

      {/* Button to switch between Host Mode and Viewer Mode */}
      <button
        onClick={() =>
          changeMode(
            currentMode === Constants.modes.SEND_AND_RECV
              ? Constants.modes.RECV_ONLY
              : Constants.modes.SEND_AND_RECV
          )
        }
      >
        {currentMode === Constants.modes.SEND_AND_RECV
          ? "Switch to Audience Mode"
          : "Switch to Host Mode"}
      </button>
    </div>
  );
}

// Main App Component - Handles the app flow and live stream lifecycle
function App() {
  const [streamId, setStreamId] = useState(null); // Holds the current stream ID
  const [mode, setMode] = useState(Constants.modes.SEND_AND_RECV); // Holds the current user mode (Host or Audience)

  const initializeStream = async (id) => {
    // Creates a new stream if no ID is provided or uses the given stream ID
    const newStreamId = id || (await createStream({ token: authToken }));
    setStreamId(newStreamId);
  };

  const onStreamLeave = () => setStreamId(null); // Resets the stream state on leave

  return authToken && streamId ? (
    // Provides the stream context to the application
    <MeetingProvider
      config={{
        meetingId: streamId,
        micEnabled: true, // Enables microphone by default
        webcamEnabled: true, // Enables webcam by default
        name: "John Doe", // Default participant name
        mode,
      }}
      token={authToken}
    >
      {/* Renders the live stream container if a stream is active */}
      <LSContainer streamId={streamId} onLeave={onStreamLeave} />
    </MeetingProvider>
  ) : (
    // Renders the join view if no stream is active
    <JoinView initializeStream={initializeStream} setMode={setMode} />
  );
}

export default App;
