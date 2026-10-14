import 'package:videosdk/videosdk.dart';

class RoomViewState {
  final bool isMicOff;
  final bool isVideoOff;
  final Map<String, Participant> participants;
  final Map<String, RTCStream?> participantVideoStreams;

  // Constructor with default values
  const RoomViewState({
    this.isMicOff = false,
    this.isVideoOff = false,
    this.participants = const {},
    this.participantVideoStreams = const {},
  });

  // Creates a copy of the state with optional overrides
  RoomViewState copyWith({
    bool? isMicOff,
    bool? isVideoOff,
    Map<String, Participant>? participants,
    Map<String, RTCStream?>? participantVideoStreams,
  }) {
    return RoomViewState(
      isMicOff: isMicOff ?? this.isMicOff,
      isVideoOff: isVideoOff ?? this.isVideoOff,
      participants: participants ?? this.participants,
      participantVideoStreams: participantVideoStreams ?? this.participantVideoStreams,
    );
  }
}
