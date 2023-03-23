import 'package:flutter/material.dart';

class MeetingControls extends StatelessWidget {
  final void Function() onToggleMicButtonPressed;
  final void Function() onToggleCameraButtonPressed;
  final void Function() onLeaveButtonPressed;

  const MeetingControls(
      {super.key,
      required this.onToggleMicButtonPressed,
      required this.onToggleCameraButtonPressed,
      required this.onLeaveButtonPressed});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        ElevatedButton(
            onPressed: onLeaveButtonPressed, child: const Text('Leave')),
        ElevatedButton(
            onPressed: onToggleMicButtonPressed, child: const Text('Toggle Mic')),
        ElevatedButton(
            onPressed: onToggleCameraButtonPressed,
            child: const Text('Toggle WebCam')),
      ],
    );
  }
}
