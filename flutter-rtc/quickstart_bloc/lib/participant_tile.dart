import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:videosdk/videosdk.dart';
import 'bloc/meeting_cubit.dart';

class ParticipantTile extends StatelessWidget {
  final Participant participant;
  const ParticipantTile({super.key, required this.participant});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<MeetingCubit, MeetingState>(
      builder: (context, state) {
        final videoStream = state.participantVideoStreams[participant.id];

        return Padding(
          padding: const EdgeInsets.all(8.0),
          child: videoStream != null
              ? RTCVideoView(
                  videoStream.renderer as RTCVideoRenderer,
                  objectFit: RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
                )
              : Container(
                  color: Colors.grey.shade800,
                  child: const Center(child: Icon(Icons.person, size: 100)),
                ),
        );
      },
    );
  }
}
