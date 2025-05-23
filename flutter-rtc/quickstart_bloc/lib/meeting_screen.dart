import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'participant_tile.dart';
import 'meeting_controls.dart';
import 'bloc/meeting_cubit.dart';

class MeetingScreen extends StatelessWidget {
  final String meetingId;
  final String token;

  const MeetingScreen({
    super.key,
    required this.meetingId,
    required this.token,
  });

  @override
  Widget build(BuildContext context) {
    // Initialize room when the screen is first built
    context.read<MeetingCubit>().initializeRoom(token, meetingId, context);

    return WillPopScope(
      onWillPop: () async {
        context.read<MeetingCubit>().leaveMeeting();
        return true;
      },
      child: Scaffold(
        appBar: AppBar(title: const Text('VideoSDK QuickStart')),
        body: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            children: [
              Text(meetingId),
              //render all participant
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: BlocBuilder<MeetingCubit, MeetingState>(
                    builder: (context, state) {
                      final participants = state.participants.values.toList();
                      return GridView.builder(
                        gridDelegate:
                            const SliverGridDelegateWithFixedCrossAxisCount(
                              crossAxisCount: 2,
                              crossAxisSpacing: 10,
                              mainAxisSpacing: 10,
                              mainAxisExtent: 300,
                            ),
                        itemBuilder: (context, index) {
                          return ParticipantTile(
                            key: Key(participants[index].id),
                            participant: participants[index],
                          );
                        },
                        itemCount: participants.length,
                      );
                    },
                  ),
                ),
              ),

              BlocBuilder<MeetingCubit, MeetingState>(
                builder: (context, state) {
                  return MeetingControls(
                    onToggleMicButtonPressed: () {
                      context.read<MeetingCubit>().toggleMic();
                    },
                    onToggleCameraButtonPressed: () {
                      context.read<MeetingCubit>().toggleCam();
                    },
                    onLeaveButtonPressed: () {
                      context.read<MeetingCubit>().leaveMeeting();
                    },
                    isMicOff: state.isMicOff,
                    isCamOff: state.isVideoOff,
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
