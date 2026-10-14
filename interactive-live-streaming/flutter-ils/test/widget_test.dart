import 'package:flutter_test/flutter_test.dart';

import 'package:flutterils/main.dart';

void main() {
  testWidgets('Join screen shows the livestream actions',
      (WidgetTester tester) async {
    await tester.pumpWidget(const MyApp());

    expect(find.text('Create LiveStream'), findsOneWidget);
    expect(find.text('Join livestream as Host'), findsOneWidget);
    expect(find.text('Join livestream as Audience'), findsOneWidget);
  });
}
