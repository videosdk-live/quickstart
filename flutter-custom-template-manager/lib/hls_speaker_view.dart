import 'dart:convert';
import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:videosdk/videosdk.dart';
import './api_call.dart';
import './room_controls.dart';
import './participant_tile.dart';
import './setting_dialog_widget.dart';

class HLSSpeakerView extends StatefulWidget {
  final Room room;
  const HLSSpeakerView({super.key, required this.room});

  @override
  State<HLSSpeakerView> createState() => _HLSSpeakerViewState();
}

class _HLSSpeakerViewState extends State<HLSSpeakerView> {
  var micEnabled = true;
  var camEnabled = true;
  String hlsState = "HLS_STOPPED";

  Map<String, Participant> participants = {};

  @override
  void initState() {
    super.initState();
    //Setting up the event listeners and initializing the participants and hls state
    setRoomEventListener();
    participants.putIfAbsent(
        widget.room.localParticipant.id, () => widget.room.localParticipant);
    //filtering the SEND_AND_RECV participants to be shown in the grid
    for (final participant in widget.room.participants.values) {
      if (participant.mode == Mode.SEND_AND_RECV) {
        participants.putIfAbsent(participant.id, () => participant);
      }
    }
    hlsState = widget.room.hlsState;
  }

  @override
  void setState(VoidCallback fn) {
    if (mounted) {
      super.setState(fn);
    }
  }

  Future<void> leave() async {
    try {
      await widget.room.leave();
    } on VideoSDKError catch (error) {
      // 3054: the room has already left.
      if (error.code != 3054) {
        log("Could not leave the room: ${error.code} ${error.name}");
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

  Future<void> toggleCamera() async {
    try {
      camEnabled
          ? await widget.room.disableCam()
          : await widget.room.enableCam();
      camEnabled = !camEnabled;
    } on VideoSDKError catch (error) {
      log("Could not toggle the camera: ${error.code} ${error.name}");
    }
  }

  //HLS handler which will start HLS with the custom template and stop HLS
  Future<void> toggleHls() async {
    try {
      if (hlsState == "HLS_STOPPED") {
        await startHls();
      } else if (hlsState == "HLS_STARTED" || hlsState == "HLS_PLAYABLE") {
        await widget.room.stopHLS();
      }
    } on VideoSDKError catch (error) {
      log("Could not start or stop HLS: ${error.code} ${error.name}");
    }
  }

  // Starts HLS with the Start HLS API, so the livestream uses the custom template.
  Future<void> startHls() async {
    //Update your Custom Template URL here if you have deployed your own, if not deployed you can use this template from our URL.
    String templateUrl =
        'https://lab.videosdk.live/react-custom-template-demo?meetingId=${widget.room.id}&token=$token';

    final body = utf8.encode(
      json.encode(
        <String, dynamic>{
          "roomId": widget.room.id,
          "templateUrl": templateUrl,
          "config": {
            "orientation": "portrait",
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

    log("response ${json.decode(httpResponse.body)}"); // result will have playbackHlsUrl and livestreamUrl
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
                    content: Text("Room Id Coppied"),
                  ))
                },
                child: const Text("Copy Room Id"),
              ),
              const SizedBox(width: 10),
              ElevatedButton(
                onPressed: leave,
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                ),
                child: const Text("Leave"),
              ),
              //Opens the settings to change the livestream background and notify viewers
              IconButton(
                onPressed: () => showDialog(
                  context: context,
                  builder: (BuildContext context) =>
                      SettingDialog(room: widget.room),
                ),
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
          //Showing the current HLS state
          Text(
            "Current HLS State: ${hlsState == "HLS_STARTED" || hlsState == "HLS_PLAYABLE" ? "Livestream is Started" : hlsState == "HLS_STARTING" ? "Livestream is starting" : hlsState == "HLS_STOPPING" ? "Livestream is stopping" : "Livestream is stopped"}",
            style: const TextStyle(color: Colors.white),
          ),
          //render all participants in the room
          Expanded(
            child: GridView.builder(
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 1,
              ),
              itemBuilder: (context, index) {
                return ParticipantTile(
                    key: Key(participants.values.elementAt(index).id),
                    room: widget.room,
                    participant: participants.values.elementAt(index));
              },
              itemCount: participants.length,
            ),
          ),
          //Rending the room Controls
          RoomControls(
            hlsState: hlsState,
            onToggleMicButtonPressed: toggleMic,
            onToggleCameraButtonPressed: toggleCamera,
            onHLSButtonPressed: toggleHls,
          ),
        ],
      ),
    );
  }

  // listening to room events for participants join, left and hls state changes
  void setRoomEventListener() {
    widget.room.on(
      Events.participantJoined,
      (Participant participant) {
        //Adding only SEND_AND_RECV participant to show in grid
        if (participant.mode == Mode.SEND_AND_RECV) {
          setState(
            () => participants.putIfAbsent(participant.id, () => participant),
          );
        }
      },
    );

    widget.room.on(Events.participantLeft,
        (Participant participant, LeaveReason reason) {
      if (participants.containsKey(participant.id)) {
        setState(
          () => participants.remove(participant.id),
        );
      }
    });

    widget.room.on(
      Events.hlsStateChanged,
      (String state, HLSUrl? hlsUrl) {
        setState(
          () => hlsState = state,
        );
      },
    );
  }
}
