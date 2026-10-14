import 'package:flutter/material.dart';
import 'package:videosdk/videosdk.dart';

class ParticipantTile extends StatefulWidget {
  final Room room;
  final Participant participant;
  const ParticipantTile(
      {super.key, required this.room, required this.participant});

  @override
  State<ParticipantTile> createState() => _ParticipantTileState();
}

class _ParticipantTileState extends State<ParticipantTile> {
  RTCStream? videoStream;

  @override
  void initState() {
    super.initState();
    // initialze video stream for the participant if they are present
    videoStream = widget.participant.streams.values
        .where((stream) => stream.kind == MediaType.VIDEO)
        .firstOrNull;
    _initStreamListeners();
  }

  //Event listener for the video stream of the participant.
  //stream.participantId is the id of the participant the stream belongs to.
  void _initStreamListeners() {
    widget.room.on(Events.streamEnabled, _onStreamEnabled);
    widget.room.on(Events.streamDisabled, _onStreamDisabled);
  }

  bool _isVideoOfThisParticipant(RTCStream stream) =>
      stream.participantId == widget.participant.id &&
      stream.kind == MediaType.VIDEO;

  void _onStreamEnabled(RTCStream stream) {
    if (_isVideoOfThisParticipant(stream)) {
      setState(() => videoStream = stream);
    }
  }

  void _onStreamDisabled(RTCStream stream) {
    if (_isVideoOfThisParticipant(stream)) {
      setState(() => videoStream = null);
    }
  }

  @override
  void dispose() {
    // The room outlives the tile, so remove the handlers from it.
    widget.room.off(Events.streamEnabled, _onStreamEnabled);
    widget.room.off(Events.streamDisabled, _onStreamDisabled);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      //Rending the Video Stream of the participant
      child: videoStream != null
          ? RTCVideoView(
              videoStream?.renderer as RTCVideoRenderer,
              objectFit: RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
            )
          : Container(
              color: Colors.grey.shade800,
              child: const Center(
                child: Icon(
                  Icons.person,
                  size: 100,
                ),
              ),
            ),
    );
  }
}
