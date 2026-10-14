import 'dart:async' show unawaited;
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'riverpod/room_controller.dart';
import 'room_controls.dart';
import 'participant_tile.dart';

class RoomScreen extends ConsumerWidget {
  final String roomId;
  final String token;

  const RoomScreen({
    super.key,
    required this.roomId,
    required this.token,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Watch room state from Riverpod provider to update UI
    final roomState = ref.watch(
      roomControllerProvider((token, roomId, context)),
    );

    // Read notifier to perform actions like toggle mic/cam or leave
    final controller = ref.read(
      roomControllerProvider((token, roomId, context)).notifier,
    );

    return PopScope(
      // leave the room when the back button closes the screen
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) unawaited(controller.leaveRoom());
      },
      child: Scaffold(
        appBar: AppBar(title: const Text('VideoSDK QuickStart')),
        body: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            children: [
              Text(roomId),
              // Render all participant
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: GridView.builder(
                    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 2,
                      crossAxisSpacing: 10,
                      mainAxisSpacing: 10,
                      mainAxisExtent: 300,
                    ),
                    itemCount: roomState.participants.length,
                    itemBuilder: (context, index) {
                      final participants = roomState.participants.values.toList();
                      return ParticipantTile(
                        key: Key(participants[index].id),
                        participant: participants[index],
                        videoStream: roomState
                            .participantVideoStreams[participants[index].id],
                      );
                    },
                  ),
                ),
              ),
              RoomControls(
                onToggleMicButtonPressed: controller.toggleMic,
                onToggleCameraButtonPressed: controller.toggleCam,
                onLeaveButtonPressed: controller.leaveRoom,
                isMicOff: roomState.isMicOff,
                isCamOff: roomState.isVideoOff,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
