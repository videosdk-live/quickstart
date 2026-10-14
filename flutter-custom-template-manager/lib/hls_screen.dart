import 'dart:async' show unawaited;
import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:videosdk/videosdk.dart';
import './hls_speaker_view.dart';
import './hls_viewer_view.dart';

class HLSScreen extends StatefulWidget {
  final String roomId;
  final String token;
  final Mode mode;

  const HLSScreen(
      {super.key,
      required this.roomId,
      required this.token,
      required this.mode});

  @override
  State<HLSScreen> createState() => _HLSScreenState();
}

class _HLSScreenState extends State<HLSScreen> {
  late Room _room;
  bool isJoined = false;

  @override
  void initState() {
    super.initState();
    // create room when widget loads
    _room = VideoSDK.createRoom(
      roomId: widget.roomId,
      token: widget.token,
      participantName: "John Doe",
      micEnabled: true,
      camEnabled: true,
      defaultCamera: FacingMode.FRONT, // camera used when the room starts
      mode: widget.mode,
    );

    // setting the event listener for join and leave events
    setRoomEventListener();

    // Joining room
    unawaited(joinRoom());
  }

  Future<void> joinRoom() async {
    // On Android, the SDK does not ask for camera and microphone permission.
    if (widget.mode == Mode.SEND_AND_RECV) {
      await VideoSDK.requestPermissions();
    }
    try {
      await _room.join();
    } on VideoSDKError catch (error) {
      log("Could not join the room: ${error.code} ${error.name}");
    }
  }

  Future<void> pinLocalParticipant() async {
    try {
      await _room.localParticipant.pin();
    } on VideoSDKError catch (error) {
      log("Could not pin: ${error.code} ${error.name}");
    }
  }

  // listening to room events
  void setRoomEventListener() {
    //Setting the joining flag to true when room is joined
    _room.on(Events.roomJoined, () {
      if (widget.mode == Mode.SEND_AND_RECV) {
        unawaited(pinLocalParticipant());
      }
      setState(() {
        isJoined = true;
      });
    });

    //Handling navigation when room is left
    _room.on(Events.roomLeft, () {
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

  @override
  Widget build(BuildContext context) {
    return PopScope(
      // leave the room when the back button closes the screen
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) unawaited(leaveRoom());
      },
      child: Scaffold(
        backgroundColor: Colors.black,
        appBar: AppBar(
          title: const Text('VideoSDK HLS QuickStart'),
        ),
        //Showing the Speaker or Viewer View based on the mode
        body: isJoined
            ? widget.mode == Mode.SEND_AND_RECV
                ? HLSSpeakerView(room: _room)
                : widget.mode == Mode.SIGNALLING_ONLY
                    ? HLSViewerView(room: _room)
                    : null
            : const Center(
                child: Text(
                  "Joining...",
                  style: TextStyle(color: Colors.white),
                ),
              ),
      ),
    );
  }
}
