# Video SDK for Flutter (Android and iOS)

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/quick-start-ils)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.gg/bGZtAbwvab)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs.

This sample uses Flutter SDK v5.0.0 and follows the [HLS Quick Start](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/quick-start-ils). Upgrading your own app from v4.0.1? Read the [Migration Guide](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/migration-guide).

## Setup Guide

- Sign up on [VideoSDK](https://app.videosdk.live/) and visit [API Keys](https://app.videosdk.live/api-keys) section to get your API key and Secret key.

- Get familiarized with [Authentication and Tokens](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/authentication-and-tokens)

<br/>

## Prerequisites

- Flutter 3.41 or later
- Valid Video SDK [Account](https://app.videosdk.live/)
- The SDK runs on Android and iOS only. Web, macOS and Windows are not supported.
- If your target platform is Android:
  - Android 7.0 (API level 24) or later
  - An arm64 Android device, or an emulator with an arm64-v8a system image. 32-bit ARM devices and x86/x86_64 emulators are not supported.
  - Android Studio (Latest version recommended)
- If your target platform is iOS:
  - iOS 18.0 or later
  - macOS with Xcode (Latest version recommended)
  - An iPhone or iPad, or the iOS Simulator on a Mac with Apple silicon. The Simulator on an Intel Mac is not supported.

See [Supported Device OS](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/supported-browser-and-device-os) and [Known Issues](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/known-issues) for details.

<br/>

## Run the Sample App

### 1. Clone the sample project

Clone the repository to your local environment.

```js
git clone https://github.com/videosdk-live/quickstart.git
cd quickstart/flutter-hls
```

### 2. Install the dependecies

Install all the dependecies to run the project.

```js
flutter pub get
```

### 3. Update the api_call.dart file

Update the api_call.dart file with your Authentication Token generated from [VideoSDK Dashboard](https://app.videosdk.live/api-keys).

```js
String token = "<Generated-from-dashboard>";
```

### 4. Run the sample app

Bingo, it's time to push the launch button.

```js
flutter run
```

<br/>

## Key Concepts

- `Room` - A Room represents Real time audio and video communication.
- `Sessions` - A particular duration you spend in a given room is a referred as session, you can have multiple session of a particular roomId.
- `Participant` - Participant represents someone who is attending the room's session, `local partcipant` represents self (You), for this self, other participants are `remote participants`.
- `Stream` - Stream means video or audio media content that is either published by `local participant` or `remote participants`. In v5.0.0 a stream is an `RTCStream`.
- `Mode` - A host joins with `Mode.SEND_AND_RECV` and a viewer joins with `Mode.SIGNALLING_ONLY`.

<br/>

## Project Configuration

This sample is already configured. To use the SDK in your own app, follow [Integrate Flutter SDK](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/flutter-sdk).

### pubspec.yaml

```yaml
dependencies:
  videosdk: ^5.0.0
  # Version 13 needs Android Gradle Plugin 9.
  permission_handler: ^12.0.1
  http: ^1.3.0
  flutter_vlc_player: ^7.4.4

flutter:
  # The SDK's iOS plugin ships as a Swift Package.
  config:
    enable-swift-package-manager: true
```

### Android

Add all the following permissions to `android/app/src/main/AndroidManifest.xml`.

```
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

In `android/app/build.gradle.kts`, use Java 17, set `minSdk` to 24 and build for arm64-v8a only:

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

Add this line to `android/gradle.properties`, so Flutter does not add its own ABIs to the filter:

```
disable-abi-filtering=true
```

For release builds, keep the SDK's classes in `android/app/proguard-rules.pro`:

```
-keep class videosdk.org.** { *; }
-dontwarn videosdk.org.**
```

### iOS

Add the following entry to your Info.plist file, located at `<project root>/ios/Runner/Info.plist`:

```
<key>NSCameraUsageDescription</key>
<string>$(PRODUCT_NAME) Camera Usage!</string>
<key>NSMicrophoneUsageDescription</key>
<string>$(PRODUCT_NAME) Microphone Usage!</string>
```

Set the iOS deployment target to 18.0 in Xcode, and `platform :ios, '18.0'` in `ios/Podfile`.

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

```js
Room room = VideoSDK.createRoom(
  roomId: "abcd-efgh-ijkl",
  token: "YOUR TOKEN",
  participantName: "GUEST",
  micEnabled: true,
  camEnabled: true,
  defaultCamera: FacingMode.FRONT,
  mode: Mode.SEND_AND_RECV,
);
```

<br/>

## [Join a Room](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/setup-call/join-meeting)

- On Android, the SDK does not ask for camera and microphone permission. Ask with `VideoSDK.requestPermissions()` before a host joins.
- `join()` returns a Future that throws a `VideoSDKError` when the room cannot be joined.

```js
await VideoSDK.requestPermissions();
try {
  await room.join();
} on VideoSDKError catch (error) {
  log("Could not join the room: ${error.code} ${error.name}");
}
```

<br/>

## [Mute/Unmute Local Audio](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/handling-media/mute-unmute-mic)

```js
// unmute mic
await room.unmuteMic();

// mute mic
await room.muteMic();
```

<br/>

## [Enable/Disable Local Webcam](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/handling-media/on-off-camera)

```js
// enable webcam
await room.enableCam();

// disable webcam
await room.disableCam();
```

<br/>

## [Leave or End Room](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/setup-call/leave-end-meeting)

```js
// Only one participant will leave/exit the room; the rest of the participants will remain.
await room.leave();

// The room will come to an end for each and every participant. So, use this function in accordance with your requirements.
await room.end();
```

Each of these actions returns a Future that throws a `VideoSDKError` when it fails, so await them inside `try` / `catch`.

<br/>

## [Start HLS](https://docs.videosdk.live/flutter/guide/interactive-live-streaming/integrate-hls/start-hls)

```js
// start hls
await room.startHLS(
  config: const HLSConfig(
    layout: ConfigLayout(
      type: ConfigLayoutGrid.SPOTLIGHT,
      priority: ConfigLayoutPriority.PIN,
      gridSize: 20,
    ),
    mode: ConfigMode.VIDEO_AND_AUDIO,
    theme: ConfigTheme.DARK,
    quality: ConfigQuality.HIGH,
    orientation: ConfigOrientation.PORTRAIT,
  ),
);
```

<br/>

## [Stop HLS](https://docs.videosdk.live/flutter/guide/interactive-live-streaming/integrate-hls/stop-hls)

```js
// stop hls
await room.stopHLS();
```

<br/>

## [Listen for Room Events](https://docs.videosdk.live/flutter/api/sdk-reference/room-class/events)

By registering callback handlers, VideoSDK sends callbacks to the client app whenever there is a change or update in the room after a user joins.

```js
room.on(
  Events.roomJoined,
  () {
    // This event will be emitted when a localParticipant(you) successfully joined the room.
  },
);

room.on(Events.roomLeft, () {
  // This event will be emitted when a localParticipant(you) left the room.
});

room.on(Events.participantJoined, (Participant participant) {
  // This event will be emitted when a new participant joined the room.
  // [participant]: new participant who joined the room
});

room.on(Events.participantLeft, (Participant participant, LeaveReason reason) {
  // This event will be emitted when a joined participant left the room.
  // [participant]: participant who left the room
});

room.on(Events.hlsStateChanged, (String state, HLSUrl? hlsUrl) {
  // This event will be emitted whenever room HLS state changes.
});
```

<br/>

## Listen for Stream Events

Stream events are delivered on the Room for every participant. `stream.participantId` says whose stream it is.

```js
room.on(Events.streamEnabled, (RTCStream stream) {
  // This event will be triggered whenever a participant's video, audio or screen share stream is enabled.
});

room.on(Events.streamDisabled, (RTCStream stream) {
  // This event will be triggered whenever a participant's video, audio or screen share stream is disabled.
});
```

<br/>

## [Event associated with HLS](https://docs.videosdk.live/flutter/api/sdk-reference/room-class/events#hlsstatechanged)

- `hlsStateChanged` - Whenever room HLS state changes, then `hlsStateChanged` event will trigger.

- You can get the `playbackHlsUrl` and `livestreamUrl` of the HLS to play it on the Viewer side when the state changes to `HLS_PLAYABLE`

```js
room.on(Events.hlsStateChanged, (String state, HLSUrl? hlsUrl) {
  //Status can be :: HLS_STARTING
  //Status can be :: HLS_STARTED
  //Status can be :: HLS_PLAYABLE
  //Status can be :: HLS_STOPPING
  //Status can be :: HLS_STOPPED
  log("Room HLS status : $state");
  if (state == "HLS_PLAYABLE") {
    log("PLAYBACKHLS URL -- ${hlsUrl?.playbackHlsUrl}");
  }
});
```

<br/>

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
