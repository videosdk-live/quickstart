import 'dart:async' show unawaited;
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'participant_tile.dart';
import 'room_controls.dart';
import 'bloc/room_cubit.dart';

class RoomScreen extends StatefulWidget {
  final String roomId;
  final String token;

  const RoomScreen({
    super.key,
    required this.roomId,
    required this.token,
  });

  @override
  State<RoomScreen> createState() => _RoomScreenState();
}

class _RoomScreenState extends State<RoomScreen> {
  @override
  void initState() {
    super.initState();
    // Initialize room once, when the screen opens
    context
        .read<RoomCubit>()
        .initializeRoom(widget.token, widget.roomId, context);
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      // leave the room when the back button closes the screen
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) unawaited(context.read<RoomCubit>().leaveRoom());
      },
      child: Scaffold(
        appBar: AppBar(title: const Text('VideoSDK QuickStart')),
        body: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            children: [
              Text(widget.roomId),
              //render all participant
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: BlocBuilder<RoomCubit, RoomViewState>(
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
              BlocBuilder<RoomCubit, RoomViewState>(
                builder: (context, state) {
                  return RoomControls(
                    onToggleMicButtonPressed: () {
                      unawaited(context.read<RoomCubit>().toggleMic());
                    },
                    onToggleCameraButtonPressed: () {
                      unawaited(context.read<RoomCubit>().toggleCam());
                    },
                    onLeaveButtonPressed: () {
                      unawaited(context.read<RoomCubit>().leaveRoom());
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
