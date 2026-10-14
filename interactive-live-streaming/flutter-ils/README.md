# Video SDK Interactive Live Streaming for Flutter (Android and iOS)

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/quick-start-flutter-ils)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.com/invite/Gpmj6eCq5u)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

This sample shows interactive live streaming with the VideoSDK Flutter SDK v5. A host joins with `Mode.SEND_AND_RECV` and sends audio and video. The audience joins with `Mode.RECV_ONLY` and only watches. Either side can switch modes during the livestream.

It follows the [Interactive Live Streaming quick start](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/quick-start-flutter-ils) step by step.

## Prerequisites

- Flutter 3.41 or later
- A VideoSDK [account](https://app.videosdk.live/) to generate a token
- Android: Android 7.0 (API level 24) or later, on an arm64 device or an emulator with an arm64-v8a system image. x86 and x86_64 emulators are not supported.
- iOS: iOS 18.0 or later, on an iPhone or iPad, or the iOS Simulator on a Mac with Apple silicon. The Simulator on an Intel Mac is not supported.

The Flutter SDK v5 runs on Android and iOS only.

## Run the Sample App

### 1. Clone the sample project

```sh
git clone https://github.com/videosdk-live/quickstart.git
cd quickstart/interactive-live-streaming/flutter-ils
```

### 2. Install the dependencies

```sh
flutter pub get
```

### 3. Add your token

Generate a token from the [VideoSDK Dashboard](https://app.videosdk.live/api-keys) and paste it into `lib/api_call.dart`. The same token is used to create the livestream and to join it.

```dart
String token = "<GENERATED_TOKEN_HERE>";
```

### 4. Run the sample app

```sh
flutter run
```

## Project setup for SDK v5

This project already contains the setup that the [Flutter SDK](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/flutter-sdk) page describes:

- `pubspec.yaml`: `videosdk: ^5.0.0`, `permission_handler: ^12.0.1` (version 13 needs Android Gradle Plugin 9), and Swift Package Manager turned on with `flutter: config: enable-swift-package-manager: true`.
- `android/app/build.gradle.kts`: Java 17, `minSdk = 24`, and an `arm64-v8a` ABI filter. `android/gradle.properties` has `disable-abi-filtering=true` so that the filter takes effect.
- `android/app/proguard-rules.pro`: keeps the SDK's classes in release builds.
- `ios/Runner/Info.plist`: `NSCameraUsageDescription` and `NSMicrophoneUsageDescription`. The iOS deployment target is 18.0.

On Android, the SDK does not ask for camera and microphone permission. The app asks with `VideoSDK.requestPermissions()` before a host joins, and before a member of the audience switches to host.

## Upgrading from an older SDK version

See the [Migration Guide - v4.0.1 → v5.0.0](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/migration-guide) and [Known Issues](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/known-issues).

## Token Generation

- Development: use a temporary token from the VideoSDK [dashboard](https://app.videosdk.live/api-keys).
- Production: set up an authentication server that generates tokens. See [videosdk-rtc-api-server-examples](https://github.com/videosdk-live/videosdk-rtc-api-server-examples).

## Documentation

[Read the documentation](https://docs.videosdk.live/) to start using Video SDK.

## Community

- [Discord](https://discord.gg/Gpmj6eCq5u) - To get involved with the Video SDK community, ask questions and share tips.
- [Twitter](https://twitter.com/video_sdk) - To receive updates, announcements, blog posts, and general Video SDK tips.
