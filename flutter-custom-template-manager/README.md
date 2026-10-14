# Video SDK for Flutter (Android and iOS)

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/flutter-sdk)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.gg/bGZtAbwvab)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs.

This example livestreams a room over HLS with a [custom template](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/hls/custom-template). The host can change the livestream background and send messages to the viewers. It uses Flutter SDK v5.0.0. If you are upgrading your own app from v4.0.1, follow the [Migration Guide](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/migration-guide).

## Setup Guide

- Sign up on [VideoSDK](https://app.videosdk.live/) and visit [API Keys](https://app.videosdk.live/api-keys) section to get your API key and Secret key.

- Get familiarized with [Authentication and Tokens](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/authentication-and-tokens)

<br/>

## Prerequisites

- Flutter 3.41 or later.
- If your target platform is iOS, your development environment must meet the following requirements:
  - macOS
  - Xcode (Latest version recommended)
  - An iPhone or iPad on iOS 18.0 or later, or the iOS Simulator on a Mac with Apple silicon. The iOS Simulator on an Intel Mac is not supported.
- If your target platform is Android, your development environment must meet the following requirements:
  - macOS, Windows or Linux
  - Android Studio (Latest version recommended)
  - An arm64 Android device on Android 7.0 (API level 24) or later, or an emulator with an arm64-v8a system image. 32-bit ARM devices and x86 or x86_64 emulators are not supported.
- Web, macOS and Windows are not supported.
- Valid Video SDK [Account](https://app.videosdk.live/)

See [Supported Device OS](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/supported-browser-and-device-os) and [Known Issues](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/known-issues) for details.

<br/>

## Run the Sample App

### 1. Clone the sample project

Clone the repository to your local environment.

```js
git clone https://github.com/videosdk-live/quickstart.git
cd quickstart/flutter-custom-template-manager
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

  **`Note : Don't confuse with Room and Meeting keyword, both are same thing 😃`**

- `Sessions` - A particular duration you spend in a given room is a referred as session, you can have multiple session of a particular roomId.
- `Participant` - Participant represents someone who is attending the room's session, `local partcipant` represents self (You), for this self, other participants are `remote participants`.
- `Stream` - Stream means video or audio media content that is either published by `local participant` or `remote participants`.
- `Mode` - The host joins in `Mode.SEND_AND_RECV` and sends audio and video. A viewer joins in `Mode.SIGNALLING_ONLY` and watches the HLS livestream.

<br/>

## Android Setup

Add all the following permissions to AndroidManifest.xml file.

```
    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
```

In `android/app/build.gradle.kts`, use Java 17, set `minSdk` to 24 and build for arm64-v8a only:

```
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

## iOS Setup

Add the following entry to your Info.plist file, located at `<project root>/ios/Runner/Info.plist`:

```
<key>NSCameraUsageDescription</key>
<string>$(PRODUCT_NAME) Camera Usage!</string>
<key>NSMicrophoneUsageDescription</key>
<string>$(PRODUCT_NAME) Microphone Usage!</string>
```

The SDK's iOS plugin ships as a Swift Package. Turn on Swift Package Manager in `pubspec.yaml`, and set the iOS deployment target to 18.0 (`platform :ios, '18.0'` in the Podfile):

```
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

## [Initialize a Room](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/setup-call/initialise-meeting)

- You can initialize the room using `createRoom()` method. `createRoom()` will generate a new `Room` object and the initiated room will be returned.
- On Android, the SDK does not ask for camera and microphone permission, so ask with `VideoSDK.requestPermissions()` before the host joins.
- `join()` throws a `VideoSDKError` when it fails.

```js
Room room = VideoSDK.createRoom(
  roomId: "abcd-efgh-ijkl",
  token: "YOUR TOKEN",
  participantName: "GUEST",
  micEnabled: true,
  camEnabled: true,
  defaultCamera: FacingMode.FRONT,
  mode: Mode.SEND_AND_RECV, // Mode.SIGNALLING_ONLY for a viewer
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

These actions return a `Future` that throws a `VideoSDKError` when the action fails, so await them inside `try` / `catch`.

<br/>

## [Leave or End Room](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/setup-call/leave-end-meeting)

```js
// Only one participant will leave/exit the room; the rest of the participants will remain.
await room.leave();

// The room will come to an end for each and every participant. So, use this function in accordance with your requirements.
await room.end();
```

<br/>

## [Start HLS with a Custom Template](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/hls/custom-template)

To use a custom template, the host calls the [Start HLS API](https://docs.videosdk.live/api-reference/realtime-communication/start-hlsStream) with the `templateUrl`, instead of the `startHLS()` method of the `Room` class. See `startHls()` in `lib/hls_speaker_view.dart`.

```js
String templateUrl =
    'https://lab.videosdk.live/react-custom-template-demo?meetingId=${room.id}&token=$token';

await http.post(
  Uri.parse('https://api.videosdk.live/v2/hls/start'),
  headers: {
    'Authorization': token,
    'Content-Type': "application/json",
  },
  body: utf8.encode(json.encode({
    "roomId": room.id,
    "templateUrl": templateUrl,
    "config": {"orientation": "portrait"},
  })),
);
```

<br/>

## [Stop HLS](https://docs.videosdk.live/flutter/guide/interactive-live-streaming/integrate-hls/stop-hls)

```js
// stop hls
await room.stopHLS();
```

<br/>

## [Send Messages to the Template](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/collaboration-in-meeting/pubsub)

The host changes the livestream background and notifies the viewers by publishing on the topics the template listens to. See `lib/setting_dialog_widget.dart`.

```js
try {
  await room.pubSub.publish("CHANGE_BACKGROUND", "#1A1C22");
  await room.pubSub.publish("VIEWER_MESSAGE", "Welcome to the livestream!");
} on VideoSDKError catch (error) {
  log("Could not publish: ${error.code} ${error.name}");
}
```

<br/>

## [Listen for Room Events](https://docs.videosdk.live/flutter/api/sdk-reference/room-class/events)

By registering callback handlers, VideoSDK sends callbacks to the client app whenever there is a change or update in the room after a user joins.

```js
room.on(Events.roomJoined, () {
  // This event will be emitted when a localParticipant(you) successfully joined the room.
});

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

room.on(Events.error, (VideoSDKError error) {
  // This event will be emitted when the room reports an error.
});
```

<br/>

## [Listen for Stream Events](https://docs.videosdk.live/flutter/api/sdk-reference/room-class/events)

The room delivers stream events for every participant. `stream.participantId` says whose stream it is.

```js
room.on(Events.streamEnabled, (RTCStream stream) {
  // This event will be triggered whenever a participant's video, audio or screen share stream is enabled.
});

room.on(Events.streamDisabled, (RTCStream stream) {
  // This event will be triggered whenever a participant's video, audio or screen share stream is disabled.
});
```

<br/>

## [Event associated with HLS](https://docs.videosdk.live/flutter/api/sdk-reference/room-class/events)

- `hlsStateChanged` - Whenever room HLS state changes, then `hlsStateChanged` event will trigger.

- You can get the `playbackHlsUrl` of the HLS to play it on the Viewer side when the state changes to `HLS_PLAYABLE`

```js
room.on(Events.hlsStateChanged, (String state, HLSUrl? hlsUrl) {
  //State can be :: HLS_STARTING
  //State can be :: HLS_STARTED
  //State can be :: HLS_PLAYABLE
  //State can be :: HLS_STOPPING
  //State can be :: HLS_STOPPED
  log("Room HLS state : $state");
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
