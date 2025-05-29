import 'package:videosdk/videosdk.dart';

class MeetingState {
  final bool isMicOff;
  final bool isVideoOff;
  final Map<String, Participant> participants;
  final Map<String, Stream?> participantVideoStreams;

  const MeetingState({
    this.isMicOff = false,
    this.isVideoOff = false,
    this.participants = const {},
    this.participantVideoStreams = const {},
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
      participantVideoStreams: participantVideoStreams ?? this.participantVideoStreams,
    );
  }
}
