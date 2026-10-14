import 'package:flutter_test/flutter_test.dart';

import 'package:videosdk_flutter_quickstart/main.dart';

void main() {
  testWidgets('Join screen shows the host and viewer actions',
      (WidgetTester tester) async {
    await tester.pumpWidget(const MyApp());

    expect(find.text('Create Room'), findsOneWidget);
    expect(find.text('Join Room as Host'), findsOneWidget);
    expect(find.text('Join Room as Viewer'), findsOneWidget);
  });
}
