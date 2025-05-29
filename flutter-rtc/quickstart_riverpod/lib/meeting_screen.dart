import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'riverpod/meeting_controller.dart';
import 'meeting_controls.dart';
import 'participant_tile.dart';

class MeetingScreen extends ConsumerWidget {
  final String meetingId;
  final String token;

  const MeetingScreen({
    super.key,
    required this.meetingId,
    required this.token,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final meetingState = ref.watch(
      meetingControllerProvider((token, meetingId, context)),
    );

    final controller = ref.read(
      meetingControllerProvider((token, meetingId, context)).notifier,
    );

    return WillPopScope(
      onWillPop: () async {
        controller.leaveMeeting();
        return true;
      },
      child: Scaffold(
        appBar: AppBar(title: const Text('VideoSDK QuickStart')),
        body: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            children: [
              Text(meetingId),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: GridView.builder(
                    gridDelegate:
                        const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 2,
                          crossAxisSpacing: 10,
                          mainAxisSpacing: 10,
                          mainAxisExtent: 300,
                        ),
                    itemCount: meetingState.participants.length,
                    itemBuilder: (context, index) {
                      final participants = meetingState.participants.values
                          .toList();
                      return ParticipantTile(
                        key: Key(participants[index].id),
                        participant: participants[index],
                        videoStream: meetingState
                            .participantVideoStreams
                            [participants[index].id],
                      );
                    },
                  ),
                ),
              ),
              MeetingControls(
                onToggleMicButtonPressed: controller.toggleMic,
                onToggleCameraButtonPressed: controller.toggleCam,
                onLeaveButtonPressed: controller.leaveMeeting,
                isMicOff: meetingState.isMicOff,
                isCamOff: meetingState.isVideoOff,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
