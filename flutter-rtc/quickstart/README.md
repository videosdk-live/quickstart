# Video SDK for Flutter (Android and iOS)

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/quick-start)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.com/invite/f2WsNDN9S5)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs.

This sample uses Flutter SDK **v5.0.0** and follows the [Flutter Quick Start](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/quick-start). Upgrading your own app from v4? See the [Migration Guide](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/migration-guide).

## Setup Guide

- Sign up on [VideoSDK](https://app.videosdk.live/) and visit [API Keys](https://app.videosdk.live/api-keys) section to get your API key and Secret key.

- Get familiarized with [Authentication and Tokens](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/authentication-and-tokens)

<br/>

## Prerequisites

- Flutter 3.41 or later
- Valid Video SDK [Account](https://app.videosdk.live/)
- Android: Android Studio, and an **arm64** Android device (Android 7.0 / API 24 or later) or an emulator with an **arm64-v8a** system image. x86 and x86_64 emulators are not supported.
- iOS: macOS with Xcode, and an iPhone or iPad on **iOS 18.0** or later. The iOS Simulator works only on a Mac with Apple silicon.
- Web, macOS and Windows are not supported by v5.0.0.

<br/>

## Run the Sample App

### 1. Clone the sample project

Clone the repository to your local environment.

```sh
git clone https://github.com/videosdk-live/quickstart.git
cd quickstart/flutter-rtc/quickstart
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

### 4. Run the sample app

Bingo, it's time to push the launch button.

```sh
flutter run
```

<br/>

## Key Concepts

- `Room` - A Room represents Real time audio and video communication.
- `Sessions` - A particular duration you spend in a given room is a referred as session, you can have multiple session of a particular roomId.
- `Participant` - Participant represents someone who is attending the room's session, `local participant` represents self (You), for this self, other participants are `remote participants`.
- `RTCStream` - Stream means video or audio media content that is either published by `local participant` or `remote participants`.

<br/>

## Project Configuration

This sample is already configured. If you are adding the SDK to your own app, follow [Install the Flutter SDK](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/flutter-sdk). In short:

### Android

Add the following permissions to `android/app/src/main/AndroidManifest.xml`.

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

In `android/app/build.gradle.kts`, use Java 17, set `minSdk` to 24 or higher and build for arm64-v8a only:

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

Add `disable-abi-filtering=true` to `android/gradle.properties`, and keep the SDK's classes in release builds with `android/app/proguard-rules.pro`:

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

Turn on Swift Package Manager in `pubspec.yaml`, and set the iOS deployment target to 18.0:

```yaml
flutter:
  config:
    enable-swift-package-manager: true
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

## [Initialize a Room](https://docs.videosdk.live/flutter/api/sdk-reference/videosdk-class/methods#createroom)

- You can initialize the room using `createRoom()` method. `createRoom()` will generate a new `Room` object.

```dart
Room room = VideoSDK.createRoom(
  roomId: "abcd-efgh-ijkl",
  token: "YOUR TOKEN",
  participantName: "GUEST",
  micEnabled: true,
  camEnabled: true,
  defaultCamera: FacingMode.FRONT,
);
```

- On Android, the SDK does not ask for camera and microphone permission. Ask before you join. `join()` throws a `VideoSDKError` if the room cannot be joined.

```dart
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

These actions return a `Future` that throws a `VideoSDKError` when the action fails, so await them inside `try` / `catch`.

<br/>

## [Listen for Room Events](https://docs.videosdk.live/flutter/api/sdk-reference/room-class/events)

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

## [Listen for Stream Events](https://docs.videosdk.live/flutter/api/sdk-reference/participant-class/events)

The room delivers stream events for every participant. `stream.participantId` says whose stream it is.

```dart
room.on(Events.streamEnabled, (RTCStream stream) {
  // This event will be triggered whenever a participant's video, audio or screen share stream is enabled.
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
