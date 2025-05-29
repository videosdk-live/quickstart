import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'package:videosdk/videosdk.dart';

part 'meeting_state.dart';

/// Cubit to manage video meeting state using VideoSDK
class MeetingCubit extends Cubit<MeetingState> {
  // The room instance from VideoSDK
  late Room _room;

  /// Initializes with default state
  MeetingCubit()
    : super(
        MeetingState(
          isMicOff: false,
          isVideoOff: false,
          participants: {},
          participantVideoStreams: {},
        ),
      );

  void initializeRoom(String token, String meetingId, BuildContext context) {
    // create room
    _room = VideoSDK.createRoom(
      roomId: meetingId,
      token: token,
      displayName: "John Doe",
      micEnabled: true,
      camEnabled: true,
      defaultCameraIndex: kIsWeb ? 0 : 1,
    );
    // Set room and add listeners
    setRoom(_room, context);
    // Join room
    _room.join();
  }

  /// Sets the room and registers meeting event listeners
  void setRoom(Room room, BuildContext context) {
    _room = room;
    _setMeetingEventListener(context);
  }

  /// Handles room and participant lifecycle events
  void _setMeetingEventListener(BuildContext context) {
    _room.on(Events.roomJoined, () {
      final updated = Map<String, Participant>.from(state.participants);
      updated[_room.localParticipant.id] = _room.localParticipant;
      _initializeParticipantStreams(_room.localParticipant);
      emit(state.copyWith(participants: updated));
    });

    _room.on(Events.participantJoined, (Participant participant) {
      final updated = Map<String, Participant>.from(state.participants);
      updated[participant.id] = participant;
      _initializeParticipantStreams(participant);
      emit(state.copyWith(participants: updated));
    });

    _room.on(Events.participantLeft, (String participantId) {
      final updated = Map<String, Participant>.from(state.participants);
      final updatedStreams = Map<String, Stream?>.from(
        state.participantVideoStreams,
      );
      updated.remove(participantId);
      updatedStreams.remove(participantId);
      emit(
        state.copyWith(
          participants: updated,
          participantVideoStreams: updatedStreams,
        ),
      );
    });

    _room.on(Events.roomLeft, () {
      emit(state.copyWith(participants: {}, participantVideoStreams: {}));
      Navigator.popUntil(context, ModalRoute.withName('/'));
    });
  }

  /// Initializes and listens to participant video stream changes
  void _initializeParticipantStreams(Participant participant) {
    final updatedStreams = Map<String, Stream?>.from(
      state.participantVideoStreams,
    );

    // Initialize video stream
    participant.streams.forEach((key, Stream stream) {
      if (stream.kind == 'video') {
        updatedStreams[participant.id] = stream;
      }
    });

    // Video stream enabled
    participant.on(Events.streamEnabled, (Stream stream) {
      if (stream.kind == 'video') {
        final newStreams = Map<String, Stream?>.from(
          state.participantVideoStreams,
        );
        newStreams[participant.id] = stream;
        emit(state.copyWith(participantVideoStreams: newStreams));
      }
    });

    // Video stream disabled
    participant.on(Events.streamDisabled, (Stream stream) {
      if (stream.kind == 'video') {
        final newStreams = Map<String, Stream?>.from(
          state.participantVideoStreams,
        );
        newStreams[participant.id] = null;
        emit(state.copyWith(participantVideoStreams: newStreams));
      }
    });

    emit(state.copyWith(participantVideoStreams: updatedStreams));
  }

  /// Toggles the local participant's mic
  void toggleMic() {
    if (state.isMicOff) {
      _room.unmuteMic();
    } else {
      _room.muteMic();
    }
    emit(state.copyWith(isMicOff: !state.isMicOff));
  }

  /// Toggles the local participant's camera
  void toggleCam() {
    if (state.isVideoOff) {
      _room.enableCam();
    } else {
      _room.disableCam();
    }
    emit(state.copyWith(isVideoOff: !state.isVideoOff));
  }

  /// Leaves the current meeting
  void leaveMeeting() {
    _room.leave();
  }

  /// Room accessor for UI or other logic
  Room get room => _room;
}