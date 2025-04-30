
import 'package:flutter/material.dart';
import 'package:videosdk/videosdk.dart';

class ParticipantGridTile extends StatefulWidget {
  final Participant participant;
  final bool isLocalParticipant;
  const ParticipantGridTile({
    Key? key,
    required this.participant,

    this.isLocalParticipant = false,
  }) : super(key: key);

  @override
  State<ParticipantGridTile> createState() => _ParticipantGridTileState();
}

class _ParticipantGridTileState extends State<ParticipantGridTile> {
  Stream? videoStream;
  Stream? audioStream;

  @override
  void initState() {
    _initStreamListeners();
    super.initState();

    widget.participant.streams.forEach((key, Stream stream) {
      setState(() {
        if (stream.kind == 'video') {
          videoStream = stream;
        } else if (stream.kind == 'audio') {
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

  _initStreamListeners() {
    widget.participant.on(Events.streamEnabled, (Stream _stream) {
      setState(() {
        if (_stream.kind == 'video') {
          videoStream = _stream;
        } else if (_stream.kind == 'audio') {
          audioStream = _stream;
        }
      });
    });

    widget.participant.on(Events.streamDisabled, (Stream _stream) {
      setState(() {
        if (_stream.kind == 'video' && videoStream?.id == _stream.id) {
          videoStream = null;
        } else if (_stream.kind == 'audio' && audioStream?.id == _stream.id) {
          audioStream = null;
        }
      });
    });

    widget.participant.on(Events.streamPaused, (Stream _stream) {
      setState(() {
        if (_stream.kind == 'video' && videoStream?.id == _stream.id) {
          videoStream = null;
        } else if (_stream.kind == 'audio' && audioStream?.id == _stream.id) {
          audioStream = _stream;
        }
      });
    });

    widget.participant.on(Events.streamResumed, (Stream _stream) {
      setState(() {
        if (_stream.kind == 'video' && videoStream?.id == _stream.id) {
          videoStream = _stream;
        } else if (_stream.kind == 'audio' && audioStream?.id == _stream.id) {
          audioStream = _stream;
        }
      });
    });
  }
}
