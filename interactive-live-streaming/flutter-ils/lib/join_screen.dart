import 'package:flutter/material.dart';
import 'package:flutterils/api_call.dart';
import 'package:flutterils/ils_screen.dart';

import 'package:videosdk/videosdk.dart';

class JoinScreen extends StatelessWidget {
  final _livestreamIdController = TextEditingController();

  JoinScreen({super.key});

  //Creates new livestream Id and joins it in CONFERNCE mode.
  void onCreateButtonPressed(BuildContext context) async {
    // call api to create livestream and navigate to ILSScreen with livestreamId,token and mode
    await createLivestream().then((liveStreamId) {
      if (!context.mounted) return;
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (context) => ILSScreen(
            liveStreamId: liveStreamId,
            token: token,
            mode: Mode.SEND_AND_RECV,
          ),
        ),
      );
    });
  }

  //Join the provided livestream with given Mode and livestreamId
  void onJoinButtonPressed(BuildContext context, Mode mode) {
    // check livestream  id is not null or invaild
    // if livestream id is vaild then navigate to ILSScreen with livestreamId, token and mode
    String liveStreamId = _livestreamIdController.text;
    var re = RegExp("\\w{4}\\-\\w{4}\\-\\w{4}");
    if (liveStreamId.isNotEmpty && re.hasMatch(liveStreamId)) {
      _livestreamIdController.clear();
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (context) => ILSScreen(
            liveStreamId: liveStreamId,
            token: token,
            mode: mode,
          ),
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text("Please enter valid livestream id"),
      ));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        title: const Text('VideoSDK ILS QuickStart'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            //Creating a new livestream
            ElevatedButton(
              onPressed: () => onCreateButtonPressed(context),
              child: const Text('Create LiveStream'),
            ),
            const SizedBox(height: 40),
            TextField(
              style: const TextStyle(color: Colors.white),
              decoration: const InputDecoration(
                hintText: 'Enter LiveStream Id',
                border: OutlineInputBorder(),
                hintStyle: TextStyle(color: Colors.white),
              ),
              controller: _livestreamIdController,
            ),
            //Joining the livestream as host
            ElevatedButton(
              onPressed: () => onJoinButtonPressed(context, Mode.SEND_AND_RECV),
              child: const Text('Join livestream as Host'),
            ),
            //Joining the livestream as Audience
            ElevatedButton(
              onPressed: () => onJoinButtonPressed(context, Mode.RECV_ONLY),
              child: const Text('Join livestream as Audience'),
            ),
          ],
        ),
      ),
    );
  }
}
