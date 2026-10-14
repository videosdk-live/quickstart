import 'dart:convert';
import 'package:http/http.dart' as http;

//Auth token we will use to generate a room and connect to it
String token = "<Generated-from-dashboard>";

// API call to create room
Future<String> createRoomId() async {
  final http.Response httpResponse = await http.post(
    Uri.parse("https://api.videosdk.live/v2/rooms"),
    headers: {'Authorization': token},
  );

//Destructuring the roomId from the response
  return json.decode(httpResponse.body)['roomId'];
}
