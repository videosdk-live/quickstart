import 'dart:async' show unawaited;
import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:videosdk/videosdk.dart';
import 'room_view_state.dart';

// Generated file for Riverpod using build_runner
part 'room_controller.g.dart';

// Define a Riverpod provider using @riverpod annotation and code generation
@riverpod
class RoomController extends _$RoomController {
  // Room instance from VideoSDK
  late Room _room;

  @override
  RoomViewState build((String token, String roomId, BuildContext context) args) {
    final token = args.$1;
    final roomId = args.$2;
    final context = args.$3;

    // create room
    _room = VideoSDK.createRoom(
      roomId: roomId,
      token: token,
      participantName: "John Doe",
      micEnabled: true,
      camEnabled: true,
      defaultCamera: FacingMode.FRONT, // camera used when the room starts
    );

    // Set room and add listeners
    _setRoomListeners(context);

    // Join room
    unawaited(_joinRoom());

    return const RoomViewState();
  }

  Future<void> _joinRoom() async {
    // On Android, the SDK does not ask for camera and microphone permission.
    await VideoSDK.requestPermissions();
    try {
      await _room.join();
    } on VideoSDKError catch (error) {
      log("Could not join the room: ${error.code} ${error.name}");
    }
  }

  // Handles room and participant lifecycle events
  void _setRoomListeners(BuildContext context) {
    // Triggered when the local participant successfully joins the room.
    _room.on(Events.roomJoined, () {
      final updated = Map<String, Participant>.from(state.participants);
      updated[_room.localParticipant.id] = _room.localParticipant;
      _initParticipantStreams(_room.localParticipant);
      state = state.copyWith(participants: updated);
    });

    // Triggered when a remote participant joins the room.
    _room.on(Events.participantJoined, (Participant participant) {
      final updated = Map<String, Participant>.from(state.participants);
      updated[participant.id] = participant;
      _initParticipantStreams(participant);
      state = state.copyWith(participants: updated);
    });

    // Triggered when a remote participant left the room.
    _room.on(Events.participantLeft, (Participant participant, LeaveReason reason) {
      final updated = Map<String, Participant>.from(state.participants);
      final updatedStreams = Map<String, RTCStream?>.from(
        state.participantVideoStreams,
      );
      updated.remove(participant.id);
      updatedStreams.remove(participant.id);
      state = state.copyWith(
        participants: updated,
        participantVideoStreams: updatedStreams,
      );
    });

    // Triggered when a participant's video is turned on.
    // stream.participantId is the id of the participant the stream belongs to.
    _room.on(Events.streamEnabled, (RTCStream stream) {
      if (stream.kind == MediaType.VIDEO) {
        final newStreams = Map<String, RTCStream?>.from(state.participantVideoStreams);
        newStreams[stream.participantId] = stream;
        state = state.copyWith(participantVideoStreams: newStreams);
      }
    });

    // Triggered when a participant's video is turned off.
    _room.on(Events.streamDisabled, (RTCStream stream) {
      if (stream.kind == MediaType.VIDEO) {
        final newStreams = Map<String, RTCStream?>.from(state.participantVideoStreams);
        newStreams[stream.participantId] = null;
        state = state.copyWith(participantVideoStreams: newStreams);
      }
    });

    // Triggered when the local participant successfully leave the room.
    _room.on(Events.roomLeft, () {
      state = state.copyWith(participants: {}, participantVideoStreams: {});
      Navigator.popUntil(context, ModalRoute.withName('/'));
    });
  }

  /// Picks up the video stream a participant already has
  void _initParticipantStreams(Participant participant) {
    final updatedStreams = Map<String, RTCStream?>.from(state.participantVideoStreams);

    // Initialize video stream
    participant.streams.forEach((key, RTCStream stream) {
      if (stream.kind == MediaType.VIDEO) {
        updatedStreams[participant.id] = stream;
      }
    });

    state = state.copyWith(participantVideoStreams: updatedStreams);
  }

  /// Toggles the local participant's mic
  Future<void> toggleMic() async {
    try {
      if (state.isMicOff) {
        await _room.unmuteMic();
      } else {
        await _room.muteMic();
      }
      if (ref.mounted) state = state.copyWith(isMicOff: !state.isMicOff);
    } on VideoSDKError catch (error) {
      log("Could not toggle the mic: ${error.code} ${error.name}");
    }
  }

  /// Toggles the local participant's camera
  Future<void> toggleCam() async {
    try {
      if (state.isVideoOff) {
        await _room.enableCam();
      } else {
        await _room.disableCam();
      }
      if (ref.mounted) state = state.copyWith(isVideoOff: !state.isVideoOff);
    } on VideoSDKError catch (error) {
      log("Could not toggle the camera: ${error.code} ${error.name}");
    }
  }

  /// Leaves the current room
  Future<void> leaveRoom() async {
    try {
      await _room.leave();
    } on VideoSDKError catch (error) {
      // 3054: the room has already left.
      if (error.code != 3054) {
        log("Could not leave the room: ${error.code} ${error.name}");
      }
    }
  }

  /// Room accessor for UI or other logic
  Room get room => _room;
}
