import React, { useState, useMemo, useRef, useEffect } from 'react';
import {
  TouchableOpacity,
  Text,
  TextInput,
  View,
  FlatList,
} from 'react-native';
import {
  MeetingProvider,
  useMeeting,
  useParticipant,
  MediaStream,
  RTCView,
  Constants,
} from '@videosdk.live/react-native-sdk';
import Clipboard from '@react-native-clipboard/clipboard';
import { createMeeting, authToken } from './api';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import Video from 'react-native-video';

// Responsible for either schedule new meeting or to join existing meeting as a host or as a viewer.
function JoinScreen({ getMeetingAndToken, setMode }) {
  const [meetingVal, setMeetingVal] = useState('');

  const JoinButton = ({ value, onPress }) => {
    return (
      <TouchableOpacity
        style={{
          backgroundColor: '#1178F8',
          padding: 12,
          marginVertical: 8,
          borderRadius: 6,
        }}
        onPress={onPress}
      >
        <Text style={{ color: 'white', alignSelf: 'center', fontSize: 18 }}>
          {value}
        </Text>
      </TouchableOpacity>
    );
  };
  return (
    <SafeAreaView
      style={{
        flex: 1,
        backgroundColor: 'black',
        justifyContent: 'center',
        paddingHorizontal: 6 * 10,
      }}
    >
      <TextInput
        value={meetingVal}
        onChangeText={setMeetingVal}
        placeholder={'XXXX-XXXX-XXXX'}
        placeholderTextColor={'grey'}
        style={{
          padding: 12,
          borderWidth: 1,
          borderColor: 'white',
          borderRadius: 6,
          color: 'white',
          marginBottom: 16,
        }}
      />
      <JoinButton
        onPress={() => {
          setMode('SEND_AND_RECV');
          getMeetingAndToken(meetingVal);
        }}
        value={'Join as Host'}
      />
      <JoinButton
        onPress={() => {
          setMode('SIGNALLING_ONLY');
          getMeetingAndToken(meetingVal);
        }}
        value={'Join as Viewer'}
      />
      <Text
        style={{
          alignSelf: 'center',
          fontSize: 22,
          marginVertical: 16,
          fontStyle: 'italic',
          color: 'grey',
        }}
      >
        ---------- OR ----------
      </Text>

      <JoinButton
        onPress={() => {
          getMeetingAndToken();
        }}
        value={'Create Studio Room'}
      />
    </SafeAreaView>
  );
}

// Responsible for managing participant video stream
function ParticipantView({ participantId }) {
  const { webcamStream, webcamOn } = useParticipant(participantId);
  return webcamOn && webcamStream ? (
    <RTCView
      streamURL={new MediaStream([webcamStream.track]).toURL()}
      objectFit={'cover'}
      style={{
        height: 300,
        marginVertical: 8,
        marginHorizontal: 8,
      }}
    />
  ) : (
    <View
      style={{
        backgroundColor: 'grey',
        height: 300,
        justifyContent: 'center',
        alignItems: 'center',
        marginVertical: 8,
        marginHorizontal: 8,
      }}
    >
      <Text style={{ fontSize: 16 }}>NO MEDIA</Text>
    </View>
  );
}

// Responsible for managing meeting controls such as toggle mic / webcam and leave
function Controls() {
  const { toggleWebcam, toggleMic, startHls, stopHls, hlsState } = useMeeting(
    {},
  );

  const _handleHLS = async () => {
    try {
      if (!hlsState || hlsState === 'HLS_STOPPED') {
        await startHls({
          layout: {
            type: 'GRID',
            priority: 'PIN',
            gridSize: 4,
          },
          theme: 'DARK',
          orientation: 'portrait',
        });
      } else if (hlsState === 'HLS_STARTED' || hlsState === 'HLS_PLAYABLE') {
        await stopHls();
      }
    } catch (error) {
      console.error('Failed to toggle HLS', error);
    }
  };

  const handleToggleWebcam = async () => {
    try {
      await toggleWebcam();
    } catch (error) {
      console.error('Failed to toggle webcam', error);
    }
  };

  const handleToggleMic = async () => {
    try {
      await toggleMic();
    } catch (error) {
      console.error('Failed to toggle mic', error);
    }
  };

  return (
    <View
      style={{
        padding: 24,
        flexDirection: 'row',
        justifyContent: 'space-between',
      }}
    >
      <Button
        onPress={handleToggleWebcam}
        buttonText={'Toggle Webcam'}
        backgroundColor={'#1178F8'}
      />
      <Button
        onPress={handleToggleMic}
        buttonText={'Toggle Mic'}
        backgroundColor={'#1178F8'}
      />
      {hlsState === 'HLS_STARTED' ||
      hlsState === 'HLS_STOPPING' ||
      hlsState === 'HLS_STARTING' ||
      hlsState === 'HLS_PLAYABLE' ? (
        <Button
          onPress={_handleHLS}
          buttonText={
            hlsState === 'HLS_STARTED'
              ? `Live Starting`
              : hlsState === 'HLS_STOPPING'
              ? `Live Stopping`
              : hlsState === 'HLS_PLAYABLE'
              ? `Stop Live`
              : `Go Live`
          }
          backgroundColor={'#FF5D5D'}
        />
      ) : (
        <Button
          onPress={_handleHLS}
          buttonText={`Go Live`}
          backgroundColor={'#1178F8'}
        />
      )}
    </View>
  );
}

// Responsible for Speaker side view, which contains Meeting Controls(toggle mic/webcam & leave) and Participant list
function SpeakerView() {
  // Get the Participant Map and meetingId
  const { meetingId, participants } = useMeeting({});

  // For getting speaker participant, we will filter out `SEND_AND_RECV` mode participant
  const speakers = useMemo(() => {
    const speakerParticipants = [...participants.values()].filter(
      participant => {
        return participant.mode == Constants.modes.SEND_AND_RECV;
      },
    );
    return speakerParticipants;
  }, [participants]);

  return (
    <SafeAreaView style={{ backgroundColor: 'black', flex: 1 }}>
      {/* Render Header for copy meetingId and leave meeting*/}
      <HeaderView />

      {/* Render Participant List */}
      {speakers.length > 0 ? (
        <FlatList
          data={speakers}
          renderItem={({ item }) => {
            return <ParticipantView participantId={item.id} />;
          }}
        />
      ) : null}

      {/* Render Controls */}
      <Controls />
    </SafeAreaView>
  );
}

function HeaderView() {
  const { meetingId, leave } = useMeeting();

  const handleLeave = async () => {
    try {
      await leave();
    } catch (error) {
      console.error('Failed to leave meeting', error);
    }
  };

  return (
    <View
      style={{
        flexDirection: 'row',
        marginTop: 12,
        justifyContent: 'space-evenly',
        alignItems: 'center',
      }}
    >
      <Text style={{ fontSize: 24, color: 'white' }}>{meetingId}</Text>
      <Button
        btnStyle={{
          borderWidth: 1,
          borderColor: 'white',
        }}
        onPress={() => {
          Clipboard.setString(meetingId);
          alert('MeetingId copied successfully');
        }}
        buttonText={'Copy MeetingId'}
        backgroundColor={'transparent'}
      />
      <Button
        onPress={handleLeave}
        buttonText={'Leave'}
        backgroundColor={'#FF0000'}
      />
    </View>
  );
}

// Responsible for Viewer side view, which contains video player for streaming HLS and managing HLS state (HLS_STARTED, HLS_STOPPING, HLS_STARTING, etc.)
function ViewerView({}) {
  const { hlsState, hlsUrls } = useMeeting();

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: 'black' }}>
      {hlsState == 'HLS_PLAYABLE' ? (
        <>
          {/* Render Header for copy meetingId and leave meeting*/}
          <HeaderView />

          {/* Render VideoPlayer that will play `playbackHlsUrl`*/}
          <Video
            controls={true}
            source={{ uri: hlsUrls.playbackHlsUrl }}
            resizeMode={'contain'}
            style={{ flex: 1, backgroundColor: 'black' }}
            onError={e => console.log('error', e)}
          />
        </>
      ) : (
        <SafeAreaView
          style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}
        >
          <Text style={{ fontSize: 20, color: 'white' }}>
            HLS is not started yet or is stopped
          </Text>
        </SafeAreaView>
      )}
    </SafeAreaView>
  );
}

// Responsible for managing two view (Speaker & Viewer) based on provided mode (`SEND_AND_RECV` & `SIGNALLING_ONLY`)
function Container() {
  const { join, localParticipant } = useMeeting();
  const mMeeting = useMeeting({
    onMeetingJoined: async () => {
      // Pin the local participant if he joins in SEND_AND_RECV mode
      if (mMeetingRef.current.localParticipant.mode === 'SEND_AND_RECV') {
        try {
          await mMeetingRef.current.localParticipant.pin();
        } catch (error) {
          console.error('Failed to pin the local participant', error);
        }
      }
    },
    onError: error => {
      console.log(error.message);
    },
  });

  // Create a ref to meeting object so that when used inside the
  // Callback functions, meeting state is maintained
  const mMeetingRef = useRef(mMeeting);
  useEffect(() => {
    mMeetingRef.current = mMeeting;
  }, [mMeeting]);

  const handleJoin = async () => {
    try {
      await join();
    } catch (error) {
      console.error('Failed to join meeting', error);
    }
  };

  return (
    <View style={{ flex: 1 }}>
      {localParticipant?.mode == Constants.modes.SEND_AND_RECV ? (
        <SpeakerView />
      ) : localParticipant?.mode == Constants.modes.SIGNALLING_ONLY ? (
        <ViewerView />
      ) : (
        <View
          style={{
            flex: 1,
            justifyContent: 'center',
            alignItems: 'center',
            backgroundColor: 'black',
          }}
        >
          <Text style={{ fontSize: 20, color: 'white' }}>
            Press Join button to enter studio.
          </Text>
          <Button
            btnStyle={{
              marginTop: 8,
              paddingHorizontal: 22,
              padding: 12,
              borderWidth: 1,
              borderColor: 'white',
              borderRadius: 8,
            }}
            buttonText={'Join'}
            onPress={handleJoin}
          />
        </View>
      )}
    </View>
  );
}

// Common Component which will also be used in Controls Component
const Button = ({ onPress, buttonText, backgroundColor, btnStyle }) => {
  return (
    <TouchableOpacity
      onPress={onPress}
      style={{
        ...btnStyle,
        backgroundColor: backgroundColor,
        padding: 10,
        borderRadius: 8,
      }}
    >
      <Text style={{ color: 'white', fontSize: 12 }}>{buttonText}</Text>
    </TouchableOpacity>
  );
};

function AppContent() {
  const [meetingId, setMeetingId] = useState(null);

  //State to handle the mode of the participant i.e. SEND_AND_RECV or SIGNALLING_ONLY
  const [mode, setMode] = useState('SEND_AND_RECV');

  //Getting MeetingId from the API we created earlier
  const getMeetingAndToken = async id => {
    const meetingId =
      id == null ? await createMeeting({ token: authToken }) : id;
    setMeetingId(meetingId);
  };

  return authToken && meetingId ? (
    <MeetingProvider
      config={{
        meetingId,
        micEnabled: true,
        webcamEnabled: true,
        name: 'Ahmed',
        //These will be the mode of the participant SEND_AND_RECV or SIGNALLING_ONLY
        mode: mode,
        defaultCamera: 'front',
      }}
      token={authToken}
    >
      <Container />
    </MeetingProvider>
  ) : (
    <JoinScreen getMeetingAndToken={getMeetingAndToken} setMode={setMode} />
  );
}

function App() {
  return (
    <SafeAreaProvider>
      <AppContent />
    </SafeAreaProvider>
  );
}
export default App;
