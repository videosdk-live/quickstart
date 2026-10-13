import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  FlatList,
} from "react-native";
import { SafeAreaProvider, SafeAreaView } from "react-native-safe-area-context";
import {
  RoomProvider,
  useRoom,
  useParticipant,
  useMediaDevice,
  Constants,
  RTCView,
  MediaStream,
} from "@videosdk.live/react-native-sdk";
import { authToken, createStream } from "./api";

// Join Screen - Handles joining or creating a live stream
function JoinView({ initializeStream, setMode }) {
  const [streamId, setStreamId] = useState("");

  const handleAction = async (mode) => {
    // Sets the mode (Host or Audience) and initializes the stream
    setMode(mode);
    await initializeStream(streamId);
  };

  return (
    <SafeAreaView style={styles.container}>
      <TouchableOpacity
        style={styles.button}
        onPress={() => handleAction(Constants.Mode.SEND_AND_RECV)}
      >
        <Text style={styles.buttonText}>Create Live Stream as Host</Text>
      </TouchableOpacity>
      <Text style={styles.separatorText}>---------- OR ----------</Text>
      <TextInput
        style={styles.input}
        placeholder="Enter Stream Id"
        onChangeText={setStreamId}
      />
      <TouchableOpacity
        style={styles.button}
        onPress={() => handleAction(Constants.Mode.SEND_AND_RECV)}
      >
        <Text style={styles.buttonText}>Join as Host</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.button}
        onPress={() => handleAction(Constants.Mode.RECV_ONLY)}
      >
        <Text style={styles.buttonText}>Join as Audience</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
}

// Component to manage live stream container and session joining
function LSContainer({ streamId, onLeave }) {
  const [joined, setJoined] = useState(false); // Track if the user has joined the stream

  const { requestPermission } = useMediaDevice();
  const { join } = useRoom({
    onRoomJoined: () => setJoined(true), // Set `joined` to true when successfully joined
    onRoomLeft: onLeave, // Handle the leave stream event
    onError: (error) => Alert.alert("Error", error.message), // Display an alert on encountering an error
  });

  const handleJoin = async () => {
    try {
      // Android no longer asks for camera and microphone access by itself
      await requestPermission(Constants.permission.AUDIO_AND_VIDEO);
      await join();
    } catch (error) {
      console.error("Failed to join stream", error);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      {streamId ? (
        <Text style={{ fontSize: 18, padding: 12 }}>Stream Id :{streamId}</Text>
      ) : null}
      {/* Show the stream view if joined, otherwise display the "Join Stream" button */}
      {joined ? (
        <StreamView />
      ) : (
        <TouchableOpacity style={styles.button} onPress={handleJoin}>
          <Text style={styles.buttonText}>Join Stream</Text>
        </TouchableOpacity>
      )}
    </SafeAreaView>
  );
}

// Component to display the live stream view
function StreamView() {
  const { participants } = useRoom(); // Access participants using the VideoSDK useRoom hook
  const participantsArrId = Array.from(participants.entries())
    .filter(
      ([_, participant]) => participant.mode === Constants.Mode.SEND_AND_RECV
    )
    .map(([key]) => key);

  return (
    <View style={{ flex: 1 }}>
      <FlatList
        data={participantsArrId}
        renderItem={({ item }) => {
          return <Participant participantId={item} />;
        }}
      />
      <LSControls />
    </View>
  );
}

// Component to render video streams for a participant
function Participant({ participantId }) {
  const { webcamStream, webcamOn } = useParticipant(participantId);

  return webcamOn && webcamStream ? (
    <RTCView
      streamURL={new MediaStream([webcamStream?.track]).toURL()}
      objectFit={"cover"}
      style={{
        height: 300,
        marginVertical: 8,
        marginHorizontal: 8,
      }}
    />
  ) : (
    <View style={styles.noMedia}>
      <Text style={styles.noMediaText}>NO MEDIA</Text>
    </View>
  );
}

// Component for managing stream controls
function LSControls() {
  const { leave, toggleMic, toggleCam, changeMode, room } = useRoom(); // Access methods
  const { requestPermission } = useMediaDevice();

  const currentMode = room.localParticipant.mode; // Get the current participant's mode

  const handleLeave = async () => {
    try {
      await leave();
    } catch (error) {
      console.error("Failed to leave stream", error);
    }
  };

  const handleToggleMic = async () => {
    try {
      await toggleMic();
    } catch (error) {
      console.error("Failed to toggle mic", error);
    }
  };

  const handleToggleWebcam = async () => {
    try {
      await toggleCam();
    } catch (error) {
      console.error("Failed to toggle webcam", error);
    }
  };

  const handleChangeMode = async () => {
    const nextMode =
      currentMode === Constants.Mode.SEND_AND_RECV
        ? Constants.Mode.RECV_ONLY
        : Constants.Mode.SEND_AND_RECV;
    try {
      if (nextMode === Constants.Mode.SEND_AND_RECV) {
        // a host publishes, so ask for camera and microphone access first
        await requestPermission(Constants.permission.AUDIO_AND_VIDEO);
      }
      await changeMode(nextMode);
    } catch (error) {
      console.error("Failed to change mode", error);
    }
  };

  return (
    <View style={styles.controls}>
      <TouchableOpacity style={styles.button} onPress={handleLeave}>
        <Text style={styles.buttonText}>Leave</Text>
      </TouchableOpacity>
      {currentMode === Constants.Mode.SEND_AND_RECV && (
        <>
          <TouchableOpacity style={styles.button} onPress={handleToggleMic}>
            <Text style={styles.buttonText}>Toggle Mic</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.button} onPress={handleToggleWebcam}>
            <Text style={styles.buttonText}>Toggle Camera</Text>
          </TouchableOpacity>
        </>
      )}
      <TouchableOpacity style={styles.button} onPress={handleChangeMode}>
        <Text style={styles.buttonText}>
          {currentMode === Constants.Mode.SEND_AND_RECV
            ? "Switch to Audience"
            : "Switch to Host"}
        </Text>
      </TouchableOpacity>
    </View>
  );
}

// Main App Component - Handles the app flow and stream lifecycle
function AppContent() {
  const [streamId, setStreamId] = useState(null); // Holds the current stream ID
  const [mode, setMode] = useState(Constants.Mode.SEND_AND_RECV); // Holds the current user mode (Host or Audience)

  const initializeStream = async (id) => {
    if (!authToken) {
      console.error("PLEASE PROVIDE TOKEN IN api.js FROM app.videosdk.live");
      return;
    }
    // Creates a new stream if no ID is provided or uses the given stream ID
    const newStreamId = id || (await createStream({ token: authToken }));
    setStreamId(newStreamId);
  };

  const onStreamLeave = () => setStreamId(null); // Resets the stream state on leave

  return authToken && streamId ? (
    <RoomProvider
      config={{
        roomId: streamId,
        micEnabled: true, // Enables microphone by default
        camEnabled: true, // Enables webcam by default
        participantName: "John Doe", // Default participant name
        mode,
        defaultCamera: "front",
      }}
      token={authToken}
    >
      {/* Renders the live stream container if a stream is active */}
      <LSContainer streamId={streamId} onLeave={onStreamLeave} />
    </RoomProvider>
  ) : (
    // Renders the join view if no stream is active
    <JoinView initializeStream={initializeStream} setMode={setMode} />
  );
}

function App() {
  return (
    <SafeAreaProvider>
      <AppContent />
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F6F6FF",
    justifyContent: "center",
    paddingHorizontal: 12,
  },
  button: {
    backgroundColor: "#1178F8",
    padding: 12,
    marginTop: 14,
    borderRadius: 6,
    alignItems: "center",
  },
  buttonText: {
    color: "white",
    fontSize: 16,
  },
  input: {
    padding: 12,
    borderWidth: 1,
    borderRadius: 6,
    fontStyle: "italic",
    marginVertical: 10,
  },
  controls: {
    flexDirection: "row",
    justifyContent: "space-around",
    marginVertical: 16,
    flexWrap: "wrap",
  },
  noMedia: {
    backgroundColor: "grey",
    height: 300,
    justifyContent: "center",
    alignItems: "center",
    marginVertical: 8,
    marginHorizontal: 8,
  },
  noMediaText: {
    fontSize: 16,
  },
  separatorText: {
    alignSelf: "center",
    fontSize: 22,
    marginVertical: 16,
    fontStyle: "italic",
    color: "grey",
  },
});

export default App;
