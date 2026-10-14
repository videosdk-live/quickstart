import 'dart:async' show unawaited;
import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:videosdk/videosdk.dart';
import 'ils_view.dart';
import 'join_screen.dart';

class ILSScreen extends StatefulWidget {
  final String liveStreamId;
  final String token;
  final Mode mode;

  const ILSScreen(
      {super.key,
      required this.liveStreamId,
      required this.token,
      required this.mode});

  @override
  State<ILSScreen> createState() => _ILSScreenState();
}

class _ILSScreenState extends State<ILSScreen> {
  late Room _room;
  bool isJoined = false;
  Mode? localParticipantMode;

  @override
  void initState() {
    super.initState();
    // create room when widget loads
    _room = VideoSDK.createRoom(
      roomId: widget.liveStreamId,
      token: widget.token,
      participantName: "John Doe",
      micEnabled: true,
      camEnabled: true,
      defaultCamera: FacingMode.FRONT, // camera used when the livestream starts
      mode: widget.mode,
    );
    localParticipantMode = widget.mode;
    // setting the event listener for join and leave events
    setLivestreamEventListener();

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
      log("Could not join the livestream: ${error.code} ${error.name}");
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
  void setLivestreamEventListener() {
    _room.on(Events.roomJoined, () {
      if (widget.mode == Mode.SEND_AND_RECV) {
        unawaited(pinLocalParticipant());
      }
      setState(() {
        localParticipantMode = _room.localParticipant.mode;
        isJoined = true;
      });
    });

    //Handling navigation when livestream is left
    _room.on(Events.roomLeft, () {
      Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (context) => JoinScreen()),
        (route) => false, // Removes all previous routes
      );
    });
  }

  Future<void> leaveRoom() async {
    try {
      await _room.leave();
    } on VideoSDKError catch (error) {
      // 3054: the room has already left the livestream.
      if (error.code != 3054) {
        log("Could not leave the livestream: ${error.code} ${error.name}");
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      // leave the livestream when the back button closes the screen
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) unawaited(leaveRoom());
      },
      child: Scaffold(
        backgroundColor: Colors.black,
        appBar: AppBar(
          title: const Text('VideoSDK ILS QuickStart'),
        ),
        //Showing the Host or Audience View based on the mode
        body: isJoined
            ? ILSView(room: _room, bar: false, mode: widget.mode)
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
