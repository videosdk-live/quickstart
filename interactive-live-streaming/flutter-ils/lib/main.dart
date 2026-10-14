import 'package:flutter/material.dart';
import 'join_screen.dart';

void main() async {
  // Run Flutter App
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // Material App
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'VideoSDK Flutter ils Example',
      theme: ThemeData.dark().copyWith(
        appBarTheme: const AppBarTheme().copyWith(
          backgroundColor: Colors.black,
        ),
        primaryColor: Colors.black,
        scaffoldBackgroundColor: Colors.black45,
      ),
      home: JoinScreen(),
    );
  }
}
