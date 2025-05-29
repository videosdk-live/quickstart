# Video SDK for Flutter (Android and iOS) - BLoC Quickstart

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/getting-started)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.com/invite/f2WsNDN9S5)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs.

## Setup Guide

- Sign up on [VideoSDK](https://app.videosdk.live/) and visit [API Keys](https://app.videosdk.live/api-keys) section to get your API key and Secret key.

- Get familiarized with [API key and Secret key](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/signup-and-create-api)

- Get familiarized with [Token](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/server-setup)

<br/>

## Prerequisites

- If your target platform is iOS, your development environment must meet the following requirements:
  - Flutter 2.0 or later
  - Dart 2.12.0 or later
  - flutter_bloc v8.0 or later
  - macOS
  - Xcode (Latest version recommended)
- If your target platform is Android, your development environment must meet the following requirements:
  - Flutter 2.0 or later
  - Dart 2.12.0 or later
  - flutter_bloc v8.0 or later
  - macOS or Windows
  - Android Studio (Latest version recommended)
- If your target platform is iOS, you need a real iOS device.
- If your target platform is Android, you need an Android simulator or a real Android device.
- Valid Video SDK [Account](https://app.videosdk.live/)

<br/>

## Run the Sample App

### 1. Clone the sample project

Clone the repository to your local environment.

```js
git clone https://github.com/videosdk-live/quickstart.git
cd quickstart/flutter-rtc/quickstart_bloc
```

### 2. Install the dependecies

Install all the dependecies to run the project.

```js
flutter pub get
```

### 3. Update the api_call.dart file

Update the api_call.dart file with your Authentication Token generated from [VideoSDK Dashboard](https://app.videosdk.live/api-keys).

```js
const token = '<Generated-from-dashboard>';
```

### 4. Run the sample app

Bingo, it's time to push the launch button.

```js
flutter run
```

<br/>

## Key Concepts

- `Meeting` - A Meeting represents Real time audio and video communication.

  **`Note : Don't confuse with Room and Meeting keyword, both are same thing 😃`**

- `Sessions` - A particular duration you spend in a given meeting is a referred as session, you can have multiple session of a particular meetingId.
- `Participant` - Participant represents someone who is attending the meeting's session, `local partcipant` represents self (You), for this self, other participants are `remote participants`.
- `Stream` - Stream means video or audio media content that is either published by `local participant` or `remote participants`.

<br/>

## Android Permission

Add all the following permissions to AndroidManifest.xml file.

```
    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
```

## iOS Permission

Add the following entry to your Info.plist file, located at `<project root>/ios/Runner/Info.plist`:

```
<key>NSCameraUsageDescription</key>
<string>$(PRODUCT_NAME) Camera Usage!</string>
<key>NSMicrophoneUsageDescription</key>
<string>$(PRODUCT_NAME) Microphone Usage!</string>
```

<br/>

## Token Generation

Token is used to create and validate a meeting using API and also initialise a meeting.

🛠️ `Development Environment`:

- For development, you can use temporary token. Visit VideoSDK [dashboard](https://app.videosdk.live/api-keys) to generate temporary token.

🌐 `Production Environment`:

- For production, you have to set up an authentication server to authorize users. Follow our official example repositories to setup authentication server, [videosdk-rtc-api-server-examples](https://github.com/videosdk-live/videosdk-rtc-api-server-examples)

<br/>

## API: Create and Validate meeting

- `create meeting` - Please refer this [documentation](https://docs.videosdk.live/api-reference/realtime-communication/create-room) to create meeting.
- `validate meeting`- Please refer this [documentation](https://docs.videosdk.live/api-reference/realtime-communication/validate-room) to validate the meetingId.

<br/>

## [Initialize a Meeting](https://docs.videosdk.live/flutter/api/sdk-reference/videosdk-class/methods#createroom)

- You can initialize the meeting using `createRoom()` method. `createRoom()` will generate a new `Room` object and the initiated meeting will be returned.

```js
  Room room = VideoSDK.createRoom(
        roomId: "abcd-efgh-ijkl",
        token: "YOUR TOKEN",
        displayName: "GUEST",
        micEnabled: true,
        camEnabled: true,
        defaultCameraIndex: 1,
      );
```

<br/>

## [Mute/Unmute Local Audio](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/features/mic-controls)

```js
// unmute mic
room.unmuteMic();

// mute mic
room.muteMic();
```

<br/>

## [Enable/Disable Local Webcam](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/features/camera-controls)

```js
// enable webcam
room.enableCam();

// disable webcam
room.disableCam();
```

<br/>

## [Leave or End Meeting](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/features/leave-end-room)

```js
// Only one participant will leave/exit the meeting; the rest of the participants will remain.
room.leave();

// The meeting will come to an end for each and every participant. So, use this function in accordance with your requirements.
room.end();
```

<br/>

## [Listen for Room Events](https://docs.videosdk.live/flutter/api/sdk-reference/room-class/events)

By registering callback handlers, VideoSDK sends callbacks to the client app whenever there is a change or update in the meeting after a user joins.

```js
    room.on(
      Events.roomJoined,
      () {
        // This event will be emitted when a localParticipant(you) successfully joined the meeting.
      },
    );

    room.on(Events.roomLeft, (String? errorMsg) {
      // This event will be emitted when a localParticipant(you) left the meeting.
      // [errorMsg]: It will have the message if meeting was left due to some error like Network problem
    });

    room.on(Events.participantJoined, (Participant participant) {
      // This event will be emitted when a new participant joined the meeting.
      // [participant]: new participant who joined the meeting
    });

    room.on(Events.participantLeft, (participantId) => {
      // This event will be emitted when a joined participant left the meeting.
      // [participantId]: id of participant who left the meeting
    });

```

<br/>

## [Listen for Participant Events](https://docs.videosdk.live/flutter/api/sdk-reference/participant-class/events)

By registering callback handlers, VideoSDK sends callbacks to the client app whenever a participant's video, audio, or screen share stream is enabled or disabled.

```js
  participant.on(Events.streamEnabled, (Stream _stream) {
    // This event will be triggered whenever a participant's video, audio or screen share stream is enabled.
  });

  participant.on(Events.stereamDisabled, (Stream _stream) {
    // This event will be triggered whenever a participant's video, audio or screen share stream is disabled.
  });

```

If you want to learn more about the SDK, read the Complete Documentation of [Flutter VideoSDK](https://docs.videosdk.live/flutter/guide/video-and-audio-calling-api-sdk/getting-started)

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
