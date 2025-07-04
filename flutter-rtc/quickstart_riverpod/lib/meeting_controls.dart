import 'package:flutter/material.dart';

class MeetingControls extends StatelessWidget {
  final void Function() onToggleMicButtonPressed;
  final void Function() onToggleCameraButtonPressed;
  final void Function() onLeaveButtonPressed;
  final bool isMicOff;
  final bool isCamOff;

  const MeetingControls({
    super.key,
    required this.onToggleMicButtonPressed,
    required this.onToggleCameraButtonPressed,
    required this.onLeaveButtonPressed,
    required this.isMicOff,
    required this.isCamOff,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        ElevatedButton(
          onPressed: onLeaveButtonPressed,
          child: const Text('Leave'),
        ),
        ElevatedButton(
          onPressed: onToggleMicButtonPressed,
          child: Text('Toggle Mic'),
        ),
        ElevatedButton(
          onPressed: onToggleCameraButtonPressed,
          child: Text('Toggle Cam'),
        ),
      ],
    );
  }
}
