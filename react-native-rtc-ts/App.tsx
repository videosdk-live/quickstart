import React, {useState} from 'react';
import {
  TouchableOpacity,
  Text,
  TextInput,
  View,
  FlatList,
  type ListRenderItemInfo,
} from 'react-native';
import {SafeAreaProvider, SafeAreaView} from 'react-native-safe-area-context';
import {
  RoomProvider,
  useRoom,
  useParticipant,
  useMediaDevice,
  Constants,
  MediaStream,
  RTCView,
} from '@videosdk.live/react-native-sdk';
import {createMeeting, token} from './api';

function JoinScreen({
  getMeetingId,
}: {
  getMeetingId: (meetingId?: string) => void;
}) {
  const [meetingVal, setMeetingVal] = useState('');
  return (
    <SafeAreaView
      style={{
        flex: 1,
        backgroundColor: '#F6F6FF',
        justifyContent: 'center',
        paddingHorizontal: 6 * 10,
      }}>
      <TouchableOpacity
        onPress={() => {
          getMeetingId();
        }}
        style={{backgroundColor: '#1178F8', padding: 12, borderRadius: 6}}>
        <Text style={{color: 'white', alignSelf: 'center', fontSize: 18}}>
          Create Meeting
        </Text>
      </TouchableOpacity>

      <Text
        style={{
          alignSelf: 'center',
          fontSize: 22,
          marginVertical: 16,
          fontStyle: 'italic',
          color: 'grey',
        }}>
        ---------- OR ----------
      </Text>
      <TextInput
        value={meetingVal}
        onChangeText={setMeetingVal}
        placeholder={'XXXX-XXXX-XXXX'}
        style={{
          padding: 12,
          borderWidth: 1,
          borderRadius: 6,
          fontStyle: 'italic',
        }}
      />
      <TouchableOpacity
        style={{
          backgroundColor: '#1178F8',
          padding: 12,
          marginTop: 14,
          borderRadius: 6,
        }}
        onPress={() => {
          console.log('dmeo user ');
          getMeetingId(meetingVal);
        }}>
        <Text style={{color: 'white', alignSelf: 'center', fontSize: 18}}>
          Join Meeting
        </Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
}

const Button = ({
  onPress,
  buttonText,
  backgroundColor,
}: {
  onPress: () => void;
  buttonText: string;
  backgroundColor: string;
}) => {
  return (
    <TouchableOpacity
      onPress={onPress}
      style={{
        backgroundColor: backgroundColor,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 12,
        borderRadius: 4,
      }}>
      <Text style={{color: 'white', fontSize: 12}}>{buttonText}</Text>
    </TouchableOpacity>
  );
};

function ControlsContainer({
  join,
  leave,
  toggleCam,
  toggleMic,
}: {
  join: () => Promise<void> | void;
  leave: () => Promise<void> | void;
  toggleCam: () => Promise<void> | void;
  toggleMic: () => Promise<void> | void;
}) {
  const {requestPermission} = useMediaDevice();

  const handleJoin = async () => {
    try {
      // Android no longer asks for camera and microphone access by itself
      await requestPermission(Constants.permission.AUDIO_AND_VIDEO);
      await join();
    } catch (error) {
      console.error('Failed to join meeting', error);
    }
  };

  const handleToggleWebcam = async () => {
    try {
      await toggleCam();
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
        padding: 24,
        flexDirection: 'row',
        justifyContent: 'space-between',
      }}>
      <Button
        onPress={handleJoin}
        buttonText={'Join'}
        backgroundColor={'#1178F8'}
      />
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
      <Button
        onPress={handleLeave}
        buttonText={'Leave'}
        backgroundColor={'#FF0000'}
      />
    </View>
  );
}
function ParticipantView({participantId}: {participantId: string}) {
  const {webcamStream, webcamOn} = useParticipant(participantId);
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
      }}>
      <Text style={{fontSize: 16}}>NO MEDIA</Text>
    </View>
  );
}

function ParticipantList({participants}: {participants: string[]}) {
  return participants.length > 0 ? (
    <FlatList
      data={participants}
      keyExtractor={(item: string) => item}
      renderItem={({item}: ListRenderItemInfo<string>) => {
        return <ParticipantView participantId={item} />;
      }}
    />
  ) : (
    <View
      style={{
        flex: 1,
        backgroundColor: '#F6F6FF',
        justifyContent: 'center',
        alignItems: 'center',
      }}>
      <Text style={{fontSize: 20}}>Press Join button to enter meeting.</Text>
    </View>
  );
}

function MeetingView() {
  // Get `participants` from useRoom Hook
  const {join, leave, toggleCam, toggleMic, participants, roomId} =
    useRoom({});
  const participantsArrId = [...participants.keys()];

  return (
    <View style={{flex: 1}}>
      {roomId ? (
        <Text style={{fontSize: 18, padding: 12}}>Meeting Id :{roomId}</Text>
      ) : null}
      <ParticipantList participants={participantsArrId} />
      <ControlsContainer
        join={join}
        leave={leave}
        toggleCam={toggleCam}
        toggleMic={toggleMic}
      />
    </View>
  );
}

function AppContent() {
  const [meetingId, setMeetingId] = useState<string | null>(null);

  const getMeetingId = async (id?: string) => {
    if (!token) {
      console.log('PLEASE PROVIDE TOKEN IN api.js FROM app.videosdk.live');
    }
    const meetingId = id == null ? await createMeeting({token}) : id;
    setMeetingId(meetingId);
  };

  return meetingId ? (
    <SafeAreaView style={{flex: 1, backgroundColor: '#F6F6FF'}}>
      <RoomProvider
        config={{
          roomId: meetingId,
          micEnabled: false,
          camEnabled: true,
          participantName: 'Test User',
          defaultCamera: 'front'
        }}
        token={token}>
        <MeetingView />
      </RoomProvider>
    </SafeAreaView>
  ) : (
    <JoinScreen getMeetingId={getMeetingId} />
  );
}

export default function App() {
  return (
    <SafeAreaProvider>
      <AppContent />
    </SafeAreaProvider>
  );
}
