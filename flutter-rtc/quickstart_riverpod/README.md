# Video SDK for Flutter (Android and iOS) - Riverpod Quickstart

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/quick-start-riverpod)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.com/invite/f2WsNDN9S5)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs.

This example uses Flutter Video SDK **v5.0.0** with [Riverpod](https://riverpod.dev/) for state management. It follows the [Quick Start in Flutter using RiverPod](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/quick-start-riverpod) guide. Upgrading your own app from v4.0.1? Read the [Migration Guide](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/migration-guide).

## Setup Guide

- Sign up on [VideoSDK](https://app.videosdk.live/) and visit [API Keys](https://app.videosdk.live/api-keys) section to get your API key and Secret key.

- Get familiarized with [Authentication and Tokens](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/authentication-and-tokens)

<br/>

## Prerequisites

- Flutter 3.41 or later
- Familiarity with Flutter and basic Riverpod concepts
- Android: Android 7.0 (API level 24) or later, on an arm64 device or an emulator with an arm64-v8a system image. 32-bit ARM devices and x86/x86_64 emulators are not supported.
- iOS: iOS 18.0 or later, on an iPhone, an iPad, or the iOS Simulator on a Mac with Apple silicon. The Simulator on an Intel Mac is not supported.
- Valid Video SDK [Account](https://app.videosdk.live/)

The Flutter SDK v5.0.0 runs on Android and iOS only. Web, macOS and Windows are not supported. See [Supported Device OS](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/supported-browser-and-device-os) and [Known Issues](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/known-issues).

<br/>

## Run the Sample App

### 1. Clone the sample project

Clone the repository to your local environment.

```sh
git clone https://github.com/videosdk-live/quickstart.git
cd quickstart/flutter-rtc/quickstart_riverpod
```

### 2. Install the dependencies

Install all the dependencies to run the project.

```sh
flutter pub get
```

### 3. Update the api_call.dart file

Update the `lib/api_call.dart` file with your Authentication Token generated from [VideoSDK Dashboard](https://app.videosdk.live/api-keys).

```dart
String token = "<Generated-from-dashboard>";
```

### 4. Generate Riverpod `.g.dart` Files

```sh
dart run build_runner build --delete-conflicting-outputs
```

### 5. Run the sample app

Bingo, it's time to push the launch button.

```sh
flutter run
```

<br/>

## Key Concepts

- `Room` - A Room represents Real time audio and video communication.
- `Sessions` - A particular duration you spend in a given room is a referred as session, you can have multiple session of a particular roomId.
- `Participant` - Participant represents someone who is attending the room's session, `local participant` represents self (You), for this self, other participants are `remote participants`.
- `Stream` - Stream (`RTCStream`) means video or audio media content that is either published by `local participant` or `remote participants`.

<br/>

## Project Configuration

This project is already configured. Apply the same settings when you add the SDK to your own app. The full steps are in [Integrate Flutter SDK](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/flutter-sdk).

### Dependencies

```sh
flutter pub add videosdk:^5.0.0
# permission_handler 13 needs Android Gradle Plugin 9, so pin it to 12.
flutter pub add permission_handler:^12.0.1
flutter pub add http
flutter pub add flutter_riverpod riverpod_annotation dev:build_runner dev:riverpod_generator
```

### Android

Add all the following permissions to `android/app/src/main/AndroidManifest.xml`.

```xml
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

In `android/app/build.gradle.kts`, use Java 17, set `minSdk` to 24 or higher, and build for arm64-v8a only.

```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        minSdk = 24
        ndk {
            abiFilters.add("arm64-v8a")
        }
    }
}
```

Add this line to `android/gradle.properties`. Without it, Flutter adds its own ABIs to the filter and yours has no effect.

```properties
disable-abi-filtering=true
```

For release builds, keep the SDK's classes in `android/app/proguard-rules.pro`.

```
-keep class videosdk.org.** { *; }
-dontwarn videosdk.org.**
```

### iOS

Add the following entries to `ios/Runner/Info.plist`:

```xml
<key>NSCameraUsageDescription</key>
<string>$(PRODUCT_NAME) Camera Usage!</string>
<key>NSMicrophoneUsageDescription</key>
<string>$(PRODUCT_NAME) Microphone Usage!</string>
```

The SDK's iOS plugin ships as a Swift Package, so turn on Swift Package Manager in `pubspec.yaml`:

```yaml
flutter:
  config:
    enable-swift-package-manager: true
```

Set the iOS deployment target to 18.0 or higher in Xcode, and in `ios/Podfile`:

```ruby
platform :ios, '18.0'
```

<br/>

## Token Generation

Token is used to create and validate a room using API and also initialise a room.

🛠️ `Development Environment`:

- For development, you can use temporary token. Visit VideoSDK [dashboard](https://app.videosdk.live/api-keys) to generate temporary token.

🌐 `Production Environment`:

- For production, you have to set up an authentication server to authorize users. Follow our official example repositories to setup authentication server, [videosdk-rtc-api-server-examples](https://github.com/videosdk-live/videosdk-rtc-api-server-examples)

<br/>

## API: Create and Validate room

- `create room` - Please refer this [documentation](https://docs.videosdk.live/api-reference/realtime-communication/create-room) to create room.
- `validate room`- Please refer this [documentation](https://docs.videosdk.live/api-reference/realtime-communication/validate-room) to validate the roomId.

<br/>

## [Initialize and Join a Room](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/setup-call/initialise-meeting)

- You can initialize the room using `createRoom()` method. `createRoom()` will generate a new `Room` object.
- On Android, the SDK does not ask for camera and microphone permission, so ask with `VideoSDK.requestPermissions()` before you call `join()`.
- `join()` and the other actions return a `Future` that throws a `VideoSDKError` when the action fails, so await them inside `try` / `catch`.

```dart
Room room = VideoSDK.createRoom(
  roomId: "abcd-efgh-ijkl",
  token: "YOUR TOKEN",
  participantName: "GUEST",
  micEnabled: true,
  camEnabled: true,
  defaultCamera: FacingMode.FRONT,
);

await VideoSDK.requestPermissions();
try {
  await room.join();
} on VideoSDKError catch (error) {
  log("Could not join the room: ${error.code} ${error.name}");
}
```

<br/>

## [Mute/Unmute Local Audio](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/handling-media/mute-unmute-mic)

```dart
// unmute mic
await room.unmuteMic();

// mute mic
await room.muteMic();
```

<br/>

## [Enable/Disable Local Webcam](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/handling-media/on-off-camera)

```dart
// enable webcam
await room.enableCam();

// disable webcam
await room.disableCam();
```

<br/>

## [Leave or End Room](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/setup-call/leave-end-meeting)

```dart
// Only one participant will leave/exit the room; the rest of the participants will remain.
await room.leave();

// The room will come to an end for each and every participant. So, use this function in accordance with your requirements.
await room.end();
```

A room is single-use. After it leaves it releases itself, so there is no `dispose()` to call. To join again, create a new `Room` with `VideoSDK.createRoom()`.

<br/>

## [Listen for Room Events](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/get-notified/room-events)

By registering callback handlers, VideoSDK sends callbacks to the client app whenever there is a change or update in the room after a user joins.

```dart
room.on(Events.roomJoined, () {
  // This event will be emitted when a localParticipant(you) successfully joined the room.
});

room.on(Events.roomLeft, () {
  // This event will be emitted when a localParticipant(you) left the room.
});

room.on(Events.participantJoined, (Participant participant) {
  // This event will be emitted when a new participant joined the room.
});

room.on(Events.participantLeft, (Participant participant, LeaveReason reason) {
  // This event will be emitted when a joined participant left the room.
});

room.on(Events.error, (VideoSDKError error) {
  // This event will be emitted when the room reports an error.
});
```

<br/>

## [Listen for Stream Events](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/get-notified/participant-events)

Stream events are delivered on the `Room` for every participant. `stream.participantId` tells whose stream it is.

```dart
room.on(Events.streamEnabled, (RTCStream stream) {
  // This event will be triggered whenever a participant's video, audio or screen share stream is enabled.
  if (stream.kind == MediaType.VIDEO) {
    // render it with RTCVideoView(stream.renderer as RTCVideoRenderer)
  }
});

room.on(Events.streamDisabled, (RTCStream stream) {
  // This event will be triggered whenever a participant's video, audio or screen share stream is disabled.
});
```

If you want to learn more about the SDK, read the Complete Documentation of [Flutter VideoSDK](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/concept-and-architecture)

<br/>

## Examples

- [Prebuilt SDK Examples](https://github.com/videosdk-live/videosdk-rtc-prebuilt-examples)
- [JavaScript SDK Example](https://github.com/videosdk-live/videosdk-rtc-javascript-sdk-example)
- [React JS SDK Example](https://github.com/videosdk-live/videosdk-rtc-react-sdk-example)
- [React Native SDK Example](https://github.com/videosdk-live/videosdk-rtc-react-native-sdk-example)
- [Flutter SDK Example](https://github.com/videosdk-live/videosdk-rtc-flutter-sdk-example)
- [Android SDK Example](https://github.com/videosdk-live/videosdk-rtc-android-java-sdk-example)
- [iOS SDK Example](https://github.com/videosdk-live/videosdk-rtc-ios-sdk-example)

## Documentation

[Read the documentation](https://docs.videosdk.live/) to start using Video SDK.

<br/>

## Community

- [Discord](https://discord.gg/Gpmj6eCq5u) - To get involved with the Video SDK community, ask questions and share tips.
- [Twitter](https://twitter.com/video_sdk) - To receive updates, announcements, blog posts, and general Video SDK tips.
