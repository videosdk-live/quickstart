import 'dart:async' show unawaited;
import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:videosdk/videosdk.dart';
import './room_controls.dart';
import './participant_tile.dart';

class RoomScreen extends StatefulWidget {
  final String roomId;
  final String token;

  const RoomScreen(
      {super.key, required this.roomId, required this.token});

  @override
  State<RoomScreen> createState() => _RoomScreenState();
}

class _RoomScreenState extends State<RoomScreen> {
  late Room _room;
  var micEnabled = true;
  var camEnabled = true;

  Map<String, Participant> participants = {};

  @override
  void initState() {
    super.initState();
    // create room
    _room = VideoSDK.createRoom(
      roomId: widget.roomId,
      token: widget.token,
      participantName: "John Doe",
      micEnabled: micEnabled,
      camEnabled: camEnabled,
      defaultCamera: FacingMode.FRONT, // camera used when the room starts
    );

    setRoomEventListener();

    // Join room
    unawaited(joinRoom());
  }

  Future<void> joinRoom() async {
    // On Android, the SDK does not ask for camera and microphone permission.
    await VideoSDK.requestPermissions();
    try {
      await _room.join();
    } on VideoSDKError catch (error) {
      log("Could not join the room: ${error.code} ${error.name}");
    }
  }

  @override
  void setState(fn) {
    if (mounted) {
      super.setState(fn);
    }
  }

  // listening to room events
  void setRoomEventListener() {
    _room.on(Events.roomJoined, () {
      setState(() {
        participants.putIfAbsent(
            _room.localParticipant.id, () => _room.localParticipant);
      });
    });

    _room.on(
      Events.participantJoined,
      (Participant participant) {
        setState(
          () => participants.putIfAbsent(participant.id, () => participant),
        );
      },
    );

    _room.on(Events.participantLeft,
        (Participant participant, LeaveReason reason) {
      if (participants.containsKey(participant.id)) {
        setState(
          () => participants.remove(participant.id),
        );
      }
    });

    _room.on(Events.roomLeft, () {
      participants.clear();
      Navigator.popUntil(context, ModalRoute.withName('/'));
    });
  }

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

  Future<void> toggleMic() async {
    try {
      micEnabled ? await _room.muteMic() : await _room.unmuteMic();
      micEnabled = !micEnabled;
    } on VideoSDKError catch (error) {
      log("Could not toggle the mic: ${error.code} ${error.name}");
    }
  }

  Future<void> toggleCamera() async {
    try {
      camEnabled ? await _room.disableCam() : await _room.enableCam();
      camEnabled = !camEnabled;
    } on VideoSDKError catch (error) {
      log("Could not toggle the camera: ${error.code} ${error.name}");
    }
  }

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return PopScope(
      // leave the room when the back button closes the screen
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) unawaited(leaveRoom());
      },
      child: Scaffold(
        appBar: AppBar(
          title: const Text('VideoSDK QuickStart'),
        ),
        body: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            children: [
              Text(widget.roomId),
              //render all participant
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
                    itemBuilder: (context, index) {
                      return ParticipantTile(
                          key: Key(participants.values.elementAt(index).id),
                          room: _room,
                          participant: participants.values.elementAt(index));
                    },
                    itemCount: participants.length,
                  ),
                ),
              ),
              RoomControls(
                onToggleMicButtonPressed: toggleMic,
                onToggleCameraButtonPressed: toggleCamera,
                onLeaveButtonPressed: leaveRoom,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
