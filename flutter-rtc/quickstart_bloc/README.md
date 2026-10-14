# Video SDK for Flutter (Android and iOS) - BLoC Quickstart

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/quick-start-bloc)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.com/invite/f2WsNDN9S5)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs.

This sample follows the [Quick Start in Flutter using BLoC](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/quick-start-bloc) guide and uses Flutter SDK v5.0.0.

## Setup Guide

- Sign up on [VideoSDK](https://app.videosdk.live/) and visit [API Keys](https://app.videosdk.live/api-keys) section to get your API key and Secret key.

- Get familiarized with [Authentication and Tokens](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/authentication-and-tokens)

<br/>

## Prerequisites

- Flutter 3.41 or later
- flutter_bloc v9.0 or later
- Valid Video SDK [Account](https://app.videosdk.live/)
- If your target platform is Android:
  - Android Studio (Latest version recommended)
  - An arm64 Android device, or an emulator with an arm64-v8a system image, running Android 7.0 (API level 24) or later. 32-bit ARM devices and x86/x86_64 emulators are not supported.
- If your target platform is iOS:
  - macOS with Xcode (Latest version recommended)
  - An iPhone or iPad on iOS 18.0 or later, or the iOS Simulator on a Mac with Apple silicon. The Simulator on an Intel Mac is not supported.

The Flutter SDK v5.0.0 runs on Android and iOS only. See [Supported Device OS](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/supported-browser-and-device-os).

<br/>

## Run the Sample App

### 1. Clone the sample project

Clone the repository to your local environment.

```sh
git clone https://github.com/videosdk-live/quickstart.git
cd quickstart/flutter-rtc/quickstart_bloc
```

### 2. Install the dependencies

Install all the dependencies to run the project.

```sh
flutter pub get
```

### 3. Update the api_call.dart file

Update the api_call.dart file with your Authentication Token generated from [VideoSDK Dashboard](https://app.videosdk.live/api-keys).

```dart
String token = "<Generated-from-dashboard>";
```

### 4. Run the sample app

Bingo, it's time to push the launch button.

```sh
flutter run
```

<br/>

## Project Configuration

The sample is already configured as described in [Integrate Flutter SDK](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/flutter-sdk). If you build your own app, apply the same settings.

### Dependencies

```yaml
dependencies:
  videosdk: ^5.0.0
  # permission_handler 13 needs Android Gradle Plugin 9, so pin it to version 12.
  permission_handler: ^12.0.1
  http: ^1.4.0
  flutter_bloc: ^9.1.1
  equatable: ^2.0.7

flutter:
  # The SDK's iOS plugin ships as a Swift Package.
  config:
    enable-swift-package-manager: true
```

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

In `android/app/build.gradle.kts`, use Java 17, set `minSdk` to 24 and build for arm64-v8a only.

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

Add this line to `android/gradle.properties`, so Flutter does not add its own ABIs to the filter.

```properties
disable-abi-filtering=true
```

Keep the SDK's classes in release builds with `android/app/proguard-rules.pro`.

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

Set the iOS deployment target to 18.0 in Xcode, and in `ios/Podfile`:

```ruby
platform :ios, '18.0'
```

<br/>

## Key Concepts

- `Room` - A Room represents Real time audio and video communication.

  **`Note : Don't confuse with Room and Meeting keyword, both are same thing 😃`**

- `Sessions` - A particular duration you spend in a given room is a referred as session, you can have multiple session of a particular roomId.
- `Participant` - Participant represents someone who is attending the room's session, `local partcipant` represents self (You), for this self, other participants are `remote participants`.
- `RTCStream` - Stream means video or audio media content that is either published by `local participant` or `remote participants`.

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

## [Initialize a Room](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/setup-call/initialise-meeting)

- You can initialize the room using `createRoom()` method. `createRoom()` will generate a new `Room` object and the initiated room will be returned.

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

<br/>

## [Join a Room](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/setup-call/join-meeting)

On Android, the SDK does not ask for camera and microphone permission, so ask before you join. `join()` throws a `VideoSDKError` when it fails.

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

## [Listen for Room Events](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/get-notified/room-events)

By registering callback handlers, VideoSDK sends callbacks to the client app whenever there is a change or update in the room after a user joins.

```dart
room.on(Events.roomJoined, () {
  // This event will be emitted when a localParticipant(you) successfully joined the room.
});

room.on(Events.roomLeft, (LeaveReason? reason) {
  // This event will be emitted when a localParticipant(you) left the room.
  // The room releases itself after this event. To join again, create a new Room.
});

room.on(Events.participantJoined, (Participant participant) {
  // This event will be emitted when a new participant joined the room.
  // [participant]: new participant who joined the room
});

room.on(Events.participantLeft, (Participant participant, LeaveReason reason) {
  // This event will be emitted when a joined participant left the room.
  // [participant]: participant who left the room
});
```

<br/>

## [Listen for Stream Events](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/get-notified/media-events)

Stream events are delivered on the Room for every participant. `stream.participantId` tells whose stream it is.

```dart
room.on(Events.streamEnabled, (RTCStream stream) {
  // This event will be triggered whenever a participant's video, audio or screen share stream is enabled.
});

room.on(Events.streamDisabled, (RTCStream stream) {
  // This event will be triggered whenever a participant's video, audio or screen share stream is disabled.
});
```

Upgrading from an earlier SDK version? Follow the [Migration Guide - v4.0.1 → v5.0.0](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/migration-guide). If something does not work, check the [Known Issues](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/known-issues).

If you want to learn more about the SDK, read the Complete Documentation of [Flutter VideoSDK](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/flutter-sdk)

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
