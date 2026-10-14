import 'package:flutter/material.dart';
import 'api_call.dart';
import 'hls_screen.dart';
import 'package:videosdk/videosdk.dart';

class JoinScreen extends StatelessWidget {
  final _roomIdController = TextEditingController();

  JoinScreen({super.key});

  //Creates new Room Id and joins it in SEND_AND_RECV mode.
  void onCreateButtonPressed(BuildContext context) async {
    // call api to create room and navigate to HLSScreen with roomId,token and mode
    await createRoomId().then((roomId) {
      if (!context.mounted) return;
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (context) => HLSScreen(
            roomId: roomId,
            token: token,
            mode: Mode.SEND_AND_RECV,
          ),
        ),
      );
    });
  }

  //Join the provided room with given Mode and roomId
  void onJoinButtonPressed(BuildContext context, Mode mode) {
    // check room id is not null or invalid
    // if room id is valid then navigate to HLSScreen with roomId, token and mode
    String roomId = _roomIdController.text;
    var re = RegExp("\\w{4}\\-\\w{4}\\-\\w{4}");
    if (roomId.isNotEmpty && re.hasMatch(roomId)) {
      _roomIdController.clear();
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (context) => HLSScreen(
            roomId: roomId,
            token: token,
            mode: mode,
          ),
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text("Please enter valid room id"),
      ));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        title: const Text('VideoSDK HLS QuickStart'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            //Creating a new room
            ElevatedButton(
              onPressed: () => onCreateButtonPressed(context),
              child: const Text('Create Room'),
            ),
            const SizedBox(height: 40),
            TextField(
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(
                hintText: 'Enter Room Id',
                border: OutlineInputBorder(),
                hintStyle: TextStyle(color: Colors.white),
              ),
              controller: _roomIdController,
            ),
            //Joining the room as host
            ElevatedButton(
              onPressed: () => onJoinButtonPressed(context, Mode.SEND_AND_RECV),
              child: const Text('Join Room as Host'),
            ),
            //Joining the room as viewer
            ElevatedButton(
              onPressed: () => onJoinButtonPressed(context, Mode.SIGNALLING_ONLY),
              child: const Text('Join Room as Viewer'),
            ),
          ],
        ),
      ),
    );
  }
}
