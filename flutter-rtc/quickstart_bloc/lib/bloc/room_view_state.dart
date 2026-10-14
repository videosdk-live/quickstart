part of 'room_cubit.dart';

/// Represents the current state of the room
class RoomViewState extends Equatable {
  final bool isMicOff;
  final bool isVideoOff;
  final Map<String, Participant> participants;
  final Map<String, RTCStream?> participantVideoStreams;

  /// Creates a new immutable RoomViewState instance
  const RoomViewState({
    required this.isMicOff,
    required this.isVideoOff,
    required this.participants,
    required this.participantVideoStreams,
  });

  /// Creates a copy of the current state with updated values
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

  /// Used by Equatable to compare state instances efficiently
  @override
  List<Object?> get props => [
    isMicOff,
    isVideoOff,
    participants,
    participantVideoStreams,
  ];
}
