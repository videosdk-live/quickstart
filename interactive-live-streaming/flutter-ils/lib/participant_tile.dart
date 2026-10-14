import 'package:flutter/material.dart';
import 'package:videosdk/videosdk.dart';

class ParticipantTile extends StatefulWidget {
  final Room room;
  final Participant participant;
  final bool isLocalParticipant;
  const ParticipantTile({
    Key? key,
    required this.room,
    required this.participant,
    this.isLocalParticipant = false,
  }) : super(key: key);

  @override
  State<ParticipantTile> createState() => _ParticipantTileState();
}

class _ParticipantTileState extends State<ParticipantTile> {
  RTCStream? videoStream;
  RTCStream? audioStream;

  @override
  void initState() {
    _initStreamListeners();
    super.initState();

    widget.participant.streams.forEach((key, RTCStream stream) {
      setState(() {
        if (stream.kind == MediaType.VIDEO) {
          videoStream = stream;
        } else if (stream.kind == MediaType.AUDIO) {
          audioStream = stream;
        }
      });
    });
  }

  @override
  void setState(fn) {
    if (mounted) {
      super.setState(fn);
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
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(12),
        color: Colors.black12,
      ),
      child: Stack(
        children: [
          videoStream != null
              ? ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: RTCVideoView(
                    videoStream?.renderer as RTCVideoRenderer,
                    objectFit: RTCVideoViewObjectFit.RTCVideoViewObjectFitCover,
                  ),
                )
              : Center(
                  child: Container(
                    padding: const EdgeInsets.all(15),
                    decoration: const BoxDecoration(
                      shape: BoxShape.circle,
                      color: Colors.black12,
                    ),
                    child: Text(
                      widget.participant.displayName.characters.first
                          .toUpperCase(),
                      style: const TextStyle(fontSize: 30),
                    ),
                  ),
                ),
        ],
      ),
    );
  }

  // stream.participantId is the id of the participant the stream belongs to.
  bool _isMine(RTCStream stream) =>
      stream.participantId == widget.participant.id;

  void _onStreamEnabled(RTCStream _stream) {
    if (!_isMine(_stream)) return;
    setState(() {
      if (_stream.kind == MediaType.VIDEO) {
        videoStream = _stream;
      } else if (_stream.kind == MediaType.AUDIO) {
        audioStream = _stream;
      }
    });
  }

  void _onStreamDisabled(RTCStream _stream) {
    if (!_isMine(_stream)) return;
    setState(() {
      if (_stream.kind == MediaType.VIDEO && videoStream?.id == _stream.id) {
        videoStream = null;
      } else if (_stream.kind == MediaType.AUDIO &&
          audioStream?.id == _stream.id) {
        audioStream = null;
      }
    });
  }

  void _initStreamListeners() {
    // Stream events are delivered on the room.
    widget.room.on(Events.streamEnabled, _onStreamEnabled);
    widget.room.on(Events.streamDisabled, _onStreamDisabled);
  }
}
