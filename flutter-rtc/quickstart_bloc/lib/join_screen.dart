import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'api_call.dart';
import 'room_screen.dart';
import 'bloc/room_cubit.dart';

class JoinScreen extends StatelessWidget {
  final _roomIdController = TextEditingController();

  JoinScreen({super.key});

  void onCreateButtonPressed(BuildContext context) async {
    // call api to create room and then navigate to RoomScreen with roomId,token
    await createRoomId().then((roomId) {
      if (!context.mounted) return;
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => BlocProvider<RoomCubit>(
            create: (_) => RoomCubit(),
            child: RoomScreen(roomId: roomId, token: token),
          ),
        ),
      );
    });
  }

  void onJoinButtonPressed(BuildContext context) {
    String roomId = _roomIdController.text;
    var re = RegExp("\\w{4}\\-\\w{4}\\-\\w{4}");
    // check room id is not null or invaild
    // if room id is vaild then navigate to RoomScreen with roomId,token
    if (roomId.isNotEmpty && re.hasMatch(roomId)) {
      _roomIdController.clear();
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => BlocProvider<RoomCubit>(
            create: (_) => RoomCubit(),
            child: RoomScreen(roomId: roomId, token: token),
          ),
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Please enter valid room id")),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('VideoSDK QuickStart')),
      body: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () => onCreateButtonPressed(context),
              child: const Text('Create Room'),
            ),
            Container(
              margin: const EdgeInsets.fromLTRB(0, 8.0, 0, 8.0),
              child: TextField(
                decoration: const InputDecoration(
                  hintText: 'Room Id',
                  border: OutlineInputBorder(),
                ),
                controller: _roomIdController,
              ),
            ),
            ElevatedButton(
              onPressed: () => onJoinButtonPressed(context),
              child: const Text('Join Room'),
            ),
          ],
        ),
      ),
    );
  }
}
