import 'dart:developer';
import 'package:flutter/material.dart';
import 'package:videosdk/videosdk.dart';

class SettingDialog extends StatelessWidget {
  final Room room;
  const SettingDialog({super.key, required this.room});

  // Publishes the text on the topic, then closes the dialog.
  Future<void> publish(BuildContext context, String topic,
      TextEditingController controller) async {
    try {
      await room.pubSub
          .publish(topic, controller.text, const PubSubPublishOptions());
      controller.clear();
      if (context.mounted) Navigator.pop(context);
    } on VideoSDKError catch (error) {
      log("Could not publish on $topic: ${error.code} ${error.name}");
    }
  }

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
                    onPressed: () =>
                        publish(context, "CHANGE_BACKGROUND", backgroundColorController),
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
                    onPressed: () =>
                        publish(context, "VIEWER_MESSAGE", messageController),
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
