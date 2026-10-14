import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'api_call.dart';
import 'room_screen.dart';

class JoinScreen extends ConsumerWidget {
  final TextEditingController _roomIdController = TextEditingController();

  JoinScreen({super.key});

  void onCreateButtonPressed(BuildContext context, WidgetRef ref) async {
    // call api to create room and then navigate to RoomScreen with roomId,token
    final roomId = await createRoomId();
    if (context.mounted) {
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => RoomScreen(roomId: roomId, token: token),
        ),
      );
    }
  }

  void onJoinButtonPressed(BuildContext context, WidgetRef ref) {
    final roomId = _roomIdController.text.trim();
    final re = RegExp(r'\w{4}-\w{4}-\w{4}');

    // check room id is not null or invaild
    // if room id is vaild then navigate to RoomScreen with roomId,token
    if (roomId.isNotEmpty && re.hasMatch(roomId)) {
      _roomIdController.clear();
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => RoomScreen(roomId: roomId, token: token),
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Please enter a valid room ID")),
      );
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('VideoSDK QuickStart')),
      body: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () => onCreateButtonPressed(context, ref),
              child: const Text('Create Room'),
            ),
            Container(
              margin: const EdgeInsets.symmetric(vertical: 8.0),
              child: TextField(
                controller: _roomIdController,
                decoration: const InputDecoration(
                  hintText: 'Room ID',
                  border: OutlineInputBorder(),
                ),
              ),
            ),
            ElevatedButton(
              onPressed: () => onJoinButtonPressed(context, ref),
              child: const Text('Join Room'),
            ),
          ],
        ),
      ),
    );
  }
}
