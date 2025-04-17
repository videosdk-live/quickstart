import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutterils/livestream_control.dart';
import 'package:flutterils/praticipant_tile.dart';
import 'package:videosdk/videosdk.dart';

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
    //filtering the CONFERENCE participants to be shown in the grid
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
                onPressed:
                    () => {
                      Clipboard.setData(ClipboardData(text: widget.room.id)),
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text("Livestream Id Copied")),
                      ),
                    },
                child: const Text("Copy Livestream Id"),
              ),
              const SizedBox(width: 10),
              ElevatedButton(
                onPressed: () => widget.room.leave(),
                style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                child: const Text("Leave"),
              ),
            ],
          ),
          Expanded(child: ParticipantTile(room: widget.room)),
          _buildLivestreamControls(),
        ],
      ),
    );
  }

  Widget _buildLivestreamControls() {
    if (localMode == Mode.SEND_AND_RECV) {
      return LivestreamControls(
        mode: Mode.SEND_AND_RECV,
        onToggleMicButtonPressed: () {
          micEnabled ? widget.room.muteMic() : widget.room.unmuteMic();
          micEnabled = !micEnabled;
        },
        onToggleCameraButtonPressed: () {
          camEnabled ? widget.room.disableCam() : widget.room.enableCam();
          camEnabled = !camEnabled;
        },
        onChangeModeButtonPressed: () {
          widget.room.changeMode(Mode.RECV_ONLY);
          setState(() {
            localMode = Mode.RECV_ONLY;
          });
        },
      );
    } else if (localMode == Mode.RECV_ONLY) {
      return Column(
        children: [
          LivestreamControls(
            mode: Mode.RECV_ONLY,
            onToggleMicButtonPressed: () {
              micEnabled ? widget.room.muteMic() : widget.room.unmuteMic();
              micEnabled = !micEnabled;
            },
            onToggleCameraButtonPressed: () {
              camEnabled ? widget.room.disableCam() : widget.room.enableCam();
              camEnabled = !camEnabled;
            },
            onChangeModeButtonPressed: () {
              widget.room.changeMode(Mode.SEND_AND_RECV);
              setState(() {
                localMode = Mode.SEND_AND_RECV;
              });
            },
          ),
        ],
      );
    } else {
      // Default controls
      return LivestreamControls(
        mode: Mode.RECV_ONLY,
        onToggleMicButtonPressed: () {
          micEnabled ? widget.room.muteMic() : widget.room.unmuteMic();
          micEnabled = !micEnabled;
        },
        onToggleCameraButtonPressed: () {
          camEnabled ? widget.room.disableCam() : widget.room.enableCam();
          camEnabled = !camEnabled;
        },
        onChangeModeButtonPressed: () {
          widget.room.changeMode(Mode.RECV_ONLY);
        },
      );
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
    widget.room.on(Events.participantModeChanged, () {});
    widget.room.on(Events.participantLeft, (String participantId) {
      if (participants.containsKey(participantId)) {
        setState(() => participants.remove(participantId));
      }
    });
  }
}
