import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:videosdk/videosdk.dart';
import './livestream_player.dart';

class HLSViewerView extends StatefulWidget {
  final Room room;

  const HLSViewerView({super.key, required this.room});

  @override
  State<HLSViewerView> createState() => _HLSViewerViewState();
}

class _HLSViewerViewState extends State<HLSViewerView> {
  String hlsState = "HLS_STOPPED";
  String? playbackHlsUrl;

  @override
  void initState() {
    super.initState();
    //initialize the room's hls state and hls's playbackHlsUrl
    hlsState = widget.room.hlsState;
    playbackHlsUrl = widget.room.hlsUrl?.playbackHlsUrl;
    //add the hlsStateChanged event listener
    setRoomEventListener();
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

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              children: [
                Expanded(
                    child: Text(widget.room.id,
                        style: const TextStyle(color: Colors.white))),
                ElevatedButton(
                  onPressed: () =>
                      {Clipboard.setData(ClipboardData(text: widget.room.id))},
                  child: const Text("Copy Room Id"),
                ),
                const SizedBox(width: 10),
                ElevatedButton(
                  onPressed: leave,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.red,
                  ),
                  child: const Text("Leave"),
                )
              ],
            ),
          ),
          //Show the livestream player if the playbackHlsUrl is present
          playbackHlsUrl != null
              ? LivestreamPlayer(playbackHlsUrl: playbackHlsUrl!)
              : const Text("Host has not started the HLS",
                  style: TextStyle(color: Colors.white)),
        ],
      ),
    );
  }

  void setRoomEventListener() {
    // listening to hlsStateChanged events and updating the hlsState and playbackHlsUrl
    widget.room.on(
      Events.hlsStateChanged,
      (String state, HLSUrl? hlsUrl) {
        if (mounted) {
          setState(() {
            hlsState = state;
            if (state == "HLS_PLAYABLE") {
              playbackHlsUrl = hlsUrl?.playbackHlsUrl;
            } else if (state == "HLS_STOPPED") {
              playbackHlsUrl = null;
            }
          });
        }
      },
    );
  }
}
