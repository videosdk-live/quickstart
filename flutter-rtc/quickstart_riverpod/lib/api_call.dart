import 'dart:convert';
import 'package:http/http.dart' as http;

// Replace with your actual token from VideoSDK Dashboard
String token = "<Generated-from-dashboard>";
// API call to create a meeting
Future<String> createMeeting() async {
  final http.Response httpResponse = await http.post(
    Uri.parse("https://api.videosdk.live/v2/rooms"),
    headers: {'Authorization': token},
  );

  return json.decode(httpResponse.body)['roomId'];
}
