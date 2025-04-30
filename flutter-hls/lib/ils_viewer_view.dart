import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:videosdk/videosdk.dart';
import './livestream_player.dart';

class ILSViewerView extends StatefulWidget {
  final Room room;
  const ILSViewerView({super.key, required this.room});

  @override
  State<ILSViewerView> createState() => _ILSViewerViewState();
}

class _ILSViewerViewState extends State<ILSViewerView> {
  String hlsState = "HLS_STOPPED";
  String? playbackHlsUrl;

  @override
  void initState() {
    super.initState();
    hlsState = widget.room.hlsState;
    playbackHlsUrl = widget.room.hlsUrls['playbackHlsUrl'];
    setMeetingEventListener();
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
                  child: const Text("Copy Meeting Id"),
                ),
                const SizedBox(width: 10),
                ElevatedButton(
                  onPressed: () => {widget.room.leave()},
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.red,
                  ),
                  child: const Text("Leave"),
                )
              ],
            ),
          ),
          playbackHlsUrl != null
              ? LivestreamPlayer(playbackHlsUrl: playbackHlsUrl!)
              : const Text("Host has not started the HLS",
                  style: TextStyle(color: Colors.white)),
        ],
      ),
    );
  }

  // listening to meeting events
  void setMeetingEventListener() {
    widget.room.on(
      Events.hlsStateChanged,
      (Map<String, dynamic> data) {
        String status = data['status'];
        log("HLS STATE " + data.toString());
        if (mounted) {
          setState(() {
            hlsState = status;
            if (status == "HLS_PLAYABLE" || status == "HLS_STOPPED") {
              playbackHlsUrl = data['playbackHlsUrl'];
            }
          });
        }
      },
    );
  }
}
