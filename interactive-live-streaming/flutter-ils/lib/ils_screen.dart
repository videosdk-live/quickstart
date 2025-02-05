import 'package:flutter/material.dart';
import 'package:flutterils/ils_view.dart';
import 'package:flutterils/join_screen.dart';
import 'package:videosdk/videosdk.dart';

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
    // create room when widget loads
    _room = VideoSDK.createRoom(
      roomId: widget.liveStreamId,
      token: widget.token,
      displayName: "John Doe",
      micEnabled: true,
      camEnabled: true,
      defaultCameraIndex:
          1, // Index of MediaDevices will be used to set default camera
      mode: widget.mode,
    );
    localParticipantMode = widget.mode;
    // setting the event listener for join and leave events
    setLivestreamEventListener();

    // Joining room
    _room.join();

    super.initState();
  }

  // listening to room events
  void setLivestreamEventListener() {
    _room.on(Events.roomJoined, () {
      if (widget.mode == Mode.SEND_AND_RECV) {
        _room.localParticipant.pin();
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

  // onbackButton pressed leave the room
  Future<bool> _onWillPop() async {
    _room.leave();
    return true;
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () => _onWillPop(),
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
