import 'package:flutter/material.dart';
import 'package:videosdk/videosdk.dart';

class LivestreamControls extends StatefulWidget {
  final Mode mode;
  final void Function()? onToggleMicButtonPressed;
  final void Function()? onToggleCameraButtonPressed;
  final void Function()? onChangeModeButtonPressed;

  LivestreamControls({
    super.key,
    required this.mode,
    this.onToggleMicButtonPressed,
    this.onToggleCameraButtonPressed,
    this.onChangeModeButtonPressed,
  });

  @override
  State<LivestreamControls> createState() => _LivestreamControlsState();
}

class _LivestreamControlsState extends State<LivestreamControls> {
  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        if (widget.mode == Mode.SEND_AND_RECV) ...[
          ElevatedButton(
            onPressed: widget.onToggleMicButtonPressed,
            child: const Text('Toggle Mic'),
          ),
          const SizedBox(width: 10),
          ElevatedButton(
            onPressed: widget.onToggleCameraButtonPressed,
            child: const Text('Toggle Cam'),
          ),
          ElevatedButton(
            onPressed: widget.onChangeModeButtonPressed,
            child: const Text('Audience'),
          ),
        ] else if (widget.mode == Mode.RECV_ONLY) ...[
          ElevatedButton(
            onPressed: widget.onChangeModeButtonPressed,
            child: const Text('Host/Speaker'),
          ),
        ]
      ],
    );
  }
}
