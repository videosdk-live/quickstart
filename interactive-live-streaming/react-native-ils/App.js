import React, { useState, useCallback } from "react";
import {
  SafeAreaView,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  FlatList,
} from "react-native";
import {
  MeetingProvider,
  useMeeting,
  useParticipant,
  Constants,
  register,
  RTCView,
} from "@videosdk.live/react-native-sdk";
import { authToken, createStream } from "./api";

register();

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
        onPress={() => handleAction(Constants.modes.CONFERENCE)}
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
        onPress={() => handleAction(Constants.modes.CONFERENCE)}
      >
        <Text style={styles.buttonText}>Join as Host</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.button}
        onPress={() => handleAction(Constants.modes.VIEWER)}
      >
        <Text style={styles.buttonText}>Join as Audience</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
}

// Component to manage live stream container and session joining
function LSContainer({ streamId, onLeave }) {
  const [joined, setJoined] = useState(false); // Track if the user has joined the stream

  const { join } = useMeeting({
    onMeetingJoined: () => setJoined(true), // Set `joined` to true when successfully joined
    onMeetingLeft: onLeave, // Handle the leave stream event
    onError: (error) => Alert.alert("Error", error.message), // Display an alert on encountering an error
  });

  return (
    <SafeAreaView style={styles.container}>
      {streamId ? (
        <Text style={{ fontSize: 18, padding: 12 }}>Stream Id :{streamId}</Text>
      ) : null}
      {/* Show the stream view if joined, otherwise display the "Join Stream" button */}
      {joined ? (
        <StreamView />
      ) : (
        <TouchableOpacity style={styles.button} onPress={join}>
          <Text style={styles.buttonText}>Join Stream</Text>
        </TouchableOpacity>
      )}
    </SafeAreaView>
  );
}

// Component to display the live stream view
function StreamView() {
  const { participants } = useMeeting(); // Access participants using the VideoSDK useMeeting hook
  const participantsArrId = [...participants.keys()];
  return (
    <View style={{ flex: 1 }}>
      <FlatList
        data={participantsArrId}
        renderItem={({ item }) => {
          return <Participant participantId={item} />;
        }}
      />
      <LSControls /> {/* Render live stream controls */}
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

function LSControls() {
  return null;
}

// Main App Component - Handles the app flow and stream lifecycle
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
