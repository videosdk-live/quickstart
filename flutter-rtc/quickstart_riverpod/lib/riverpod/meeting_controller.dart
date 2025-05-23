import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:videosdk/videosdk.dart';
import 'meeting_state.dart';

part 'meeting_controller.g.dart';

@riverpod
class MeetingController extends _$MeetingController {
  late Room _room;

  @override
  MeetingState build((String token, String meetingId, BuildContext context) args) {
    final token = args.$1;
    final meetingId = args.$2;
    final context = args.$3;

    _room = VideoSDK.createRoom(
      roomId: meetingId,
      token: token,
      displayName: "John Doe",
      micEnabled: true,
      camEnabled: true,
      defaultCameraIndex: kIsWeb ? 0 : 1,
    );

    _setRoomListeners(context);
    _room.join();

    return const MeetingState();
  }

  void _setRoomListeners(BuildContext context) {
    _room.on(Events.roomJoined, () {
      final updated = Map<String, Participant>.from(state.participants);
      updated[_room.localParticipant.id] = _room.localParticipant;
      _initParticipantStreams(_room.localParticipant);
      state = state.copyWith(participants: updated);
    });

    _room.on(Events.participantJoined, (Participant participant) {
      final updated = Map<String, Participant>.from(state.participants);
      updated[participant.id] = participant;
      _initParticipantStreams(participant);
      state = state.copyWith(participants: updated);
    });

    _room.on(Events.participantLeft, (String participantId) {
      final updated = Map<String, Participant>.from(state.participants);
      final updatedStreams = Map<String, Stream?>.from(state.participantVideoStreams);
      updated.remove(participantId);
      updatedStreams.remove(participantId);
      state = state.copyWith(participants: updated, participantVideoStreams: updatedStreams);
    });

    _room.on(Events.roomLeft, () {
      state = state.copyWith(participants: {}, participantVideoStreams: {});
      Navigator.popUntil(context, ModalRoute.withName('/'));
    });
  }

  void _initParticipantStreams(Participant participant) {
    final updatedStreams = Map<String, Stream?>.from(state.participantVideoStreams);
    participant.streams.forEach((key, Stream stream) {
      if (stream.kind == 'video') {
        updatedStreams[participant.id] = stream;
      }
    });

    participant.on(Events.streamEnabled, (Stream stream) {
      if (stream.kind == 'video') {
        final newStreams = Map<String, Stream?>.from(state.participantVideoStreams);
        newStreams[participant.id] = stream;
        state = state.copyWith(participantVideoStreams: newStreams);
      }
    });

    participant.on(Events.streamDisabled, (Stream stream) {
      if (stream.kind == 'video') {
        final newStreams = Map<String, Stream?>.from(state.participantVideoStreams);
        newStreams[participant.id] = null;
        state = state.copyWith(participantVideoStreams: newStreams);
      }
    });

    state = state.copyWith(participantVideoStreams: updatedStreams);
  }

  void toggleMic() {
    if (state.isMicOff) {
      _room.unmuteMic();
    } else {
      _room.muteMic();
    }
    state = state.copyWith(isMicOff: !state.isMicOff);
  }

  void toggleCam() {
    if (state.isVideoOff) {
      _room.enableCam();
    } else {
      _room.disableCam();
    }
    state = state.copyWith(isVideoOff: !state.isVideoOff);
  }

  void leaveMeeting() {
    _room.leave();
  }

  Room get room => _room;
}
