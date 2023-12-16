import 'dart:convert';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:videosdk/videosdk.dart';
import 'package:videosdk_flutter_quickstart/meeting_controls.dart';
import 'package:videosdk_flutter_quickstart/participant_tile.dart';
import 'package:http/http.dart' as http;
import 'package:videosdk_flutter_quickstart/setting_dialog_widget.dart';
import 'api_call.dart';

class ILSSpeakerView extends StatefulWidget {
  final Room room;
  const ILSSpeakerView({super.key, required this.room});

  @override
  State<ILSSpeakerView> createState() => _ILSSpeakerViewState();
}

class _ILSSpeakerViewState extends State<ILSSpeakerView> {
  var micEnabled = true;
  var camEnabled = true;
  String hlsState = "HLS_STOPPED";

  Map<String, Participant> participants = {};

  @override
  void initState() {
    super.initState();
    setMeetingEventListener();
    participants.putIfAbsent(
        widget.room.localParticipant.id, () => widget.room.localParticipant);
    widget.room.participants.values.forEach((participant) {
      if (participant.mode == Mode.CONFERENCE) {
        participants.putIfAbsent(participant.id, () => participant);
      }
    });
    hlsState = widget.room.hlsState;
  }

  @override
  void setState(VoidCallback fn) {
    if (mounted) {
      super.setState(fn);
    }
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
                style: const TextStyle(color: Colors.white),
              )),
              ElevatedButton(
                onPressed: () => {
                  Clipboard.setData(ClipboardData(text: widget.room.id)),
                  ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
                    content: Text("Meeting Id Coppied"),
                  ))
                },
                child: const Text("Copy Meeting Id"),
              ),
              const SizedBox(width: 10),
              ElevatedButton(
                onPressed: () => {widget.room.leave()},
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                ),
                child: const Text("Leave"),
              ),
              IconButton(
                onPressed: () => {
                  showDialog(
                    context: context,
                    builder: (BuildContext context) =>
                        SettingDialog(room: widget.room),
                  )
                },
                icon: const Icon(
                  Icons.settings,
                  color: Colors.white,
                ),
              )
            ],
          ),
          const SizedBox(
            height: 20,
          ),
          Text(
            "Stater: $hlsState",
            style: const TextStyle(color: Colors.white),
          ),
          Text(
            "Current HLS State: ${hlsState == "HLS_STARTED" || hlsState == "HLS_PLAYABLE" ? "Livestream is Started" : hlsState == "HLS_STARTING" ? "Livestream is starting" : hlsState == "HLS_STOPPING" ? "Livestream is stopping" : "Livestream is stopped"}",
            style: const TextStyle(color: Colors.white),
          ),
          //render all participant
          Expanded(
            child: GridView.builder(
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 1,
              ),
              itemBuilder: (context, index) {
                return ParticipantTile(
                    participant: participants.values.elementAt(index));
              },
              itemCount: participants.length,
            ),
          ),
          MeetingControls(
            hlsState: hlsState,
            onToggleMicButtonPressed: () {
              micEnabled ? widget.room.muteMic() : widget.room.unmuteMic();
              micEnabled = !micEnabled;
            },
            onToggleCameraButtonPressed: () {
              camEnabled ? widget.room.disableCam() : widget.room.enableCam();
              camEnabled = !camEnabled;
            },
            onHLSButtonPressed: () => {
              if (hlsState == "HLS_STOPPED")
                {startHls()}
              else if (hlsState == "HLS_STARTED" || hlsState == "HLS_PLAYABLE")
                {widget.room.stopHls()}
            },
          ),
        ],
      ),
    );
  }

  void startHls() async {
    //Update your Custom Template URL here if you have deployed your own
    String templateUrl =
        'https://lab.videosdk.live/react-custom-template-demo?meetingId=${widget.room.id}&token=${token}';

    final body = utf8.encode(
      json.encode(
        <String, dynamic>{
          "roomId": widget.room.id,
          "templateUrl": templateUrl,
          "config":{
            "orientation":"portrait",
          },
        },
      ),
    );

    final http.Response httpResponse = await http.post(
      Uri.parse('https://api.videosdk.live/v2/hls/start'),
      headers: {
        'Authorization': token,
        'Content-Type': "application/json",
      },
      body: body,
    );

    log("response ${json.decode(httpResponse.body)}"); // result will have playbackHlsUrl
  }

  // listening to meeting events
  void setMeetingEventListener() {
    widget.room.on(
      Events.participantJoined,
      (Participant participant) {
        if (participant.mode == Mode.CONFERENCE) {
          setState(
            () => participants.putIfAbsent(participant.id, () => participant),
          );
        }
      },
    );

    widget.room.on(Events.participantLeft, (String participantId) {
      if (participants.containsKey(participantId)) {
        setState(
          () => participants.remove(participantId),
        );
      }
    });
    widget.room.on(
      Events.hlsStateChanged,
      (Map<String, dynamic> data) {
        setState(
          () => hlsState = data['status'],
        );
      },
    );
  }
}
