import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutterils/participant_grid.dart';
import 'package:videosdk/videosdk.dart';

class ParticipantTile extends StatefulWidget {
  final Room room;
  const ParticipantTile({Key? key, required this.room}) : super(key: key);

  @override
  State<ParticipantTile> createState() => _ParticipantTileState();
}

class _ParticipantTileState extends State<ParticipantTile> {
  late Participant localParticipant;
  int numberofColumns = 1;
  int numberOfMaxOnScreenParticipants = 6;

  Map<String, Participant> participants = {};
  Map<String, Participant> onScreenParticipants = {};

  @override
  void initState() {
    localParticipant = widget.room.localParticipant;
    participants.putIfAbsent(localParticipant.id, () => localParticipant);
    participants.addAll(widget.room.participants);
    updateOnScreenParticipants();
    // Setting livestream event listeners
    setLivestreamEventListener(widget.room);

    super.initState();
  }

  @override
  void setState(fn) {
    if (mounted) {
      super.setState(fn);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Flex(
      direction: Axis.vertical,
      children: [
        for (
          int i = 0;
          i < (onScreenParticipants.length / numberofColumns).ceil();
          i++
        )
          Flexible(
            child: Flex(
              direction: Axis.vertical,
              children: [
                for (
                  int j = 0;
                  j <
                      onScreenParticipants.values
                          .toList()
                          .sublist(
                            i * numberofColumns,
                            (i + 1) * numberofColumns >
                                    onScreenParticipants.length
                                ? onScreenParticipants.length
                                : (i + 1) * numberofColumns,
                          )
                          .length;
                  j++
                )
                  Flexible(
                    child: Padding(
                      padding: const EdgeInsets.all(4.0),
                      child: ParticipantGridTile(
                        key: Key(
                          onScreenParticipants.values
                              .toList()
                              .sublist(
                                i * numberofColumns,
                                (i + 1) * numberofColumns >
                                        onScreenParticipants.length
                                    ? onScreenParticipants.length
                                    : (i + 1) * numberofColumns,
                              )
                              .elementAt(j)
                              .id,
                        ),
                        participant: onScreenParticipants.values
                            .toList()
                            .sublist(
                              i * numberofColumns,
                              (i + 1) * numberofColumns >
                                      onScreenParticipants.length
                                  ? onScreenParticipants.length
                                  : (i + 1) * numberofColumns,
                            )
                            .elementAt(j),
                      ),
                    ),
                  ),
              ],
            ),
          ),
      ],
    );
  }

  void setLivestreamEventListener(Room livestream) {
    // Called when participant joined livestream
    livestream.on(Events.participantJoined, (Participant participant) {
      final newParticipants = participants;
      newParticipants[participant.id] = participant;
      setState(() {
        participants = newParticipants;
        updateOnScreenParticipants();
      });
    });

    // Called when participant left livestream
    livestream.on(Events.participantLeft, (participantId) {
      final newParticipants = participants;

      newParticipants.remove(participantId);
      setState(() {
        participants = newParticipants;
        updateOnScreenParticipants();
      });
    });

    livestream.on(Events.participantModeChanged, (data) {
      Map<String, Participant> _participants = {};
      Participant _localParticipant = widget.room.localParticipant;
      _participants.putIfAbsent(_localParticipant.id, () => _localParticipant);
      _participants.addAll(livestream.participants);
      // log("List Mode Change mode:: ${_participants[data['participantId']]?.mode.name}");

      setState(() {
        localParticipant = _localParticipant;
        participants = _participants;
        updateOnScreenParticipants();
      });
    });

    livestream.localParticipant.on(Events.streamEnabled, (Stream stream) {
      if (stream.kind == "share") {
        setState(() {
          numberOfMaxOnScreenParticipants = 2;
          updateOnScreenParticipants();
        });
      }
    });
    livestream.localParticipant.on(Events.streamDisabled, (Stream stream) {
      if (stream.kind == "share") {
        setState(() {
          numberOfMaxOnScreenParticipants = 6;
          updateOnScreenParticipants();
        });
      }
    });
  }

  updateOnScreenParticipants() {
    Map<String, Participant> newScreenParticipants = <String, Participant>{};
    List<Participant> conferenceParticipants =
        participants.values
            .where((element) => element.mode == Mode.SEND_AND_RECV)
            .toList();

    conferenceParticipants
        .sublist(
          0,
          conferenceParticipants.length > numberOfMaxOnScreenParticipants
              ? numberOfMaxOnScreenParticipants
              : conferenceParticipants.length,
        )
        .forEach((participant) {
          newScreenParticipants.putIfAbsent(participant.id, () => participant);
        });

    if (!listEquals(
      newScreenParticipants.keys.toList(),
      onScreenParticipants.keys.toList(),
    )) {
      setState(() {
        onScreenParticipants = newScreenParticipants;
      });
    }
    if (numberofColumns !=
        (newScreenParticipants.length > 2 ||
                numberOfMaxOnScreenParticipants == 2
            ? 2
            : 1)) {
      setState(() {
        numberofColumns =
            newScreenParticipants.length > 2 ||
                    numberOfMaxOnScreenParticipants == 2
                ? 2
                : 1;
      });
    }
  }
}
