import 'package:flutter_test/flutter_test.dart';

import 'package:videosdk_flutter_quickstart/main.dart';

void main() {
  testWidgets('Join screen shows the room actions', (WidgetTester tester) async {
    await tester.pumpWidget(const MyApp());

    expect(find.text('Create Room'), findsOneWidget);
    expect(find.text('Join Room'), findsOneWidget);
  });
}
