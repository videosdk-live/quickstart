
import 'package:flutter/material.dart';
import 'package:videosdk/videosdk.dart';

class SettingDialog extends StatelessWidget {
  final Room room;
  const SettingDialog({super.key, required this.room});

  @override
  Widget build(BuildContext context) {
    final backgroundColorController = TextEditingController();
    final messageController = TextEditingController();
    return Dialog(
      backgroundColor: Colors.grey.shade800,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
      child: SizedBox(
        height: 200.0,
        width: 330.0,
        child: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: <Widget>[
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      style: const TextStyle(color: Colors.white),
                      decoration: const InputDecoration(
                        hintText: 'Enter Color',
                      ),
                      controller: backgroundColorController,
                    ),
                  ),
                  const SizedBox(
                    width: 5.0,
                  ),
                  ElevatedButton(
                    onPressed: () => {
                      room.pubSub
                          .publish(
                            "CHANGE_BACKGROUND",
                            backgroundColorController.text,
                            const PubSubPublishOptions(),
                          )
                          .then(
                            (value) => {
                              backgroundColorController.clear(),
                              Navigator.pop(context),
                            },
                          )
                    },
                    child: const Padding(
                      padding: EdgeInsets.all(8.0),
                      child: Text('Change Livestream \n       Background'),
                    ),
                  ),
                ],
              ),
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      style: const TextStyle(color: Colors.white),
                      decoration: const InputDecoration(
                        hintText: 'Enter Message',
                      ),
                      controller: messageController,
                    ),
                  ),
                  const SizedBox(
                    width: 5.0,
                  ),
                  ElevatedButton(
                    onPressed: () => {
                      room.pubSub
                          .publish(
                            "VIEWER_MESSAGE",
                            messageController.text,
                            const PubSubPublishOptions(),
                          )
                          .then(
                            (value) => {
                              messageController.clear(),
                              Navigator.pop(context),
                            },
                          )
                    },
                    child: const Padding(
                      padding: EdgeInsets.all(14.0),
                      child: Text('Notify User'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
