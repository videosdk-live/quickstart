import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:videosdk/videosdk.dart';
import 'livestream_controls.dart';
import 'participant_grid.dart';

class ILSView extends StatefulWidget {
  final Room room;
  final Mode mode;
  final bool bar;

  const ILSView({
    super.key,
    required this.room,
    required this.bar,
    required this.mode,
  });

  @override
  State<ILSView> createState() => _ILSViewState();
}

class _ILSViewState extends State<ILSView> {
  var micEnabled = true;
  var camEnabled = true;
  Map<String, Participant> participants = {};
  Mode? localMode;

  @override
  void initState() {
    super.initState();
    localMode = widget.mode;
    //Setting up the event listeners and initializing the participants and hls state
    setlivestreamEventListener();
    participants.putIfAbsent(
      widget.room.localParticipant.id,
      () => widget.room.localParticipant,
    );
    //filtering the SEND_AND_RECV participants to be shown in the grid
    widget.room.participants.values.forEach((participant) {
      if (participant.mode == Mode.SEND_AND_RECV) {
        participants.putIfAbsent(participant.id, () => participant);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  widget.room.id,
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 15,
                  ),
                ),
              ),
              ElevatedButton(
                onPressed: () => {
                  Clipboard.setData(ClipboardData(text: widget.room.id)),
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text("Livestream Id Copied")),
                  ),
                },
                child: const Text("Copy Livestream Id"),
              ),
              const SizedBox(width: 10),
              ElevatedButton(
                onPressed: leave,
                style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                child: const Text("Leave"),
              ),
            ],
          ),
          Expanded(child: ParticipantGrid(room: widget.room)),
          _buildLivestreamControls(),
        ],
      ),
    );
  }

  Widget _buildLivestreamControls() {
    if (localMode == Mode.SEND_AND_RECV) {
      return LivestreamControls(
        mode: Mode.SEND_AND_RECV,
        onToggleMicButtonPressed: toggleMic,
        onToggleCameraButtonPressed: toggleCam,
        onChangeModeButtonPressed: () => changeMode(Mode.RECV_ONLY),
      );
    } else if (localMode == Mode.RECV_ONLY) {
      return Column(
        children: [
          LivestreamControls(
            mode: Mode.RECV_ONLY,
            onToggleMicButtonPressed: toggleMic,
            onToggleCameraButtonPressed: toggleCam,
            onChangeModeButtonPressed: () => changeMode(Mode.SEND_AND_RECV),
          ),
        ],
      );
    } else {
      // Default controls
      return LivestreamControls(
        mode: Mode.RECV_ONLY,
        onToggleMicButtonPressed: toggleMic,
        onToggleCameraButtonPressed: toggleCam,
        onChangeModeButtonPressed: () => changeMode(Mode.RECV_ONLY),
      );
    }
  }

  Future<void> leave() async {
    try {
      await widget.room.leave();
    } on VideoSDKError catch (error) {
      // 3054: the room has already left the livestream.
      if (error.code != 3054) {
        log("Could not leave the livestream: ${error.code} ${error.name}");
      }
    }
  }

  Future<void> toggleMic() async {
    try {
      micEnabled ? await widget.room.muteMic() : await widget.room.unmuteMic();
      micEnabled = !micEnabled;
    } on VideoSDKError catch (error) {
      log("Could not toggle the mic: ${error.code} ${error.name}");
    }
  }

  Future<void> toggleCam() async {
    try {
      camEnabled
          ? await widget.room.disableCam()
          : await widget.room.enableCam();
      camEnabled = !camEnabled;
    } on VideoSDKError catch (error) {
      log("Could not toggle the camera: ${error.code} ${error.name}");
    }
  }

  Future<void> changeMode(Mode mode) async {
    // A host sends media, and on Android the SDK does not ask for permission.
    if (mode == Mode.SEND_AND_RECV) {
      await VideoSDK.requestPermissions();
    }
    try {
      // localMode follows participantModeChanged once the server applies it.
      await widget.room.changeMode(mode);
    } on VideoSDKError catch (error) {
      log("Could not change the mode: ${error.code} ${error.name}");
    }
  }

  // listening to room events for participants join, left and hls state changes
  void setlivestreamEventListener() {
    widget.room.on(Events.participantJoined, (Participant participant) {
      //Adding only Conference participant to show in grid
      if (participant.mode == Mode.SEND_AND_RECV) {
        setState(
          () => participants.putIfAbsent(participant.id, () => participant),
        );
      }
    });

    widget.room.on(Events.participantModeChanged, (Map<String, dynamic> data) {
      if (data['participantId'] == widget.room.localParticipant.id) {
        setState(() => localMode = widget.room.localParticipant.mode);
      }
    });

    widget.room.on(Events.participantLeft,
        (Participant participant, LeaveReason reason) {
      if (participants.containsKey(participant.id)) {
        setState(() => participants.remove(participant.id));
      }
    });
  }
}
