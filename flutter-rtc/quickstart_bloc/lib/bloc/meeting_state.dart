part of 'meeting_cubit.dart';

class MeetingState extends Equatable {
  final bool isMicOff;
  final bool isVideoOff;
  final Map<String, Participant> participants;
  final Map<String, Stream?> participantVideoStreams;

  const MeetingState({
    required this.isMicOff,
    required this.isVideoOff,
    required this.participants,
    required this.participantVideoStreams,
  });

  MeetingState copyWith({
    bool? isMicOff,
    bool? isVideoOff,
    Map<String, Participant>? participants,
    Map<String, Stream?>? participantVideoStreams,
  }) {
    return MeetingState(
      isMicOff: isMicOff ?? this.isMicOff,
      isVideoOff: isVideoOff ?? this.isVideoOff,
      participants: participants ?? this.participants,
      participantVideoStreams:
          participantVideoStreams ?? this.participantVideoStreams,
    );
  }

  @override
  List<Object?> get props => [
    isMicOff,
    isVideoOff,
    participants,
    participantVideoStreams,
  ];
}
