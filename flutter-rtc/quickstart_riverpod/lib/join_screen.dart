import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'api_call.dart';
import 'meeting_screen.dart';

class JoinScreen extends ConsumerWidget {
  final TextEditingController _meetingIdController = TextEditingController();

  JoinScreen({super.key});

  void onCreateButtonPressed(BuildContext context, WidgetRef ref) async {
    final meetingId = await createMeeting();
    if (context.mounted) {
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => MeetingScreen(meetingId: meetingId, token: token),
        ),
      );
    }
  }

  void onJoinButtonPressed(BuildContext context, WidgetRef ref) {
    final meetingId = _meetingIdController.text.trim();
    final re = RegExp(r'\w{4}-\w{4}-\w{4}');
    if (meetingId.isNotEmpty && re.hasMatch(meetingId)) {
      _meetingIdController.clear();
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => MeetingScreen(meetingId: meetingId, token: token),
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Please enter a valid meeting ID")),
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
              child: const Text('Create Meeting'),
            ),
            Container(
              margin: const EdgeInsets.symmetric(vertical: 8.0),
              child: TextField(
                controller: _meetingIdController,
                decoration: const InputDecoration(
                  hintText: 'Meeting ID',
                  border: OutlineInputBorder(),
                ),
              ),
            ),
            ElevatedButton(
              onPressed: () => onJoinButtonPressed(context, ref),
              child: const Text('Join Meeting'),
            ),
          ],
        ),
      ),
    );
  }
}
