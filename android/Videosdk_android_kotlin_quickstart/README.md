# VideoSDK for Android

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/react/guide/video-and-audio-calling-api-sdk/getting-started)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.gg/kgAvyxtTxv)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs

## Setup Guide
- Sign up on [VideoSDK](https://app.videosdk.live) and visit [API Keys](https://app.videosdk.live/api-keys) section to get your API key and Secret key.

- Get familiarized with [API key and Secret key](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/signup-and-create-api).

- Get familiarized with [Token](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/server-setup).

<br/>

## Prerequisites
- Development environment requirements:
  - [Java Development Kit](https://www.oracle.com/java/technologies/downloads/)
  - Android Studio 3.0 or later
- A physical or virtual mobile device running Android 5.0 or later
- Valid [Video SDK Account](https://app.videosdk.live/)

<br/>

## Run the Sample Project
### 1. Clone the sample project

Clone the repository to your local environment.

```js
git clone https://github.com/videosdk-live/quickstart.git
```

### 2. Modify JoinActivity.kt

Generate temporary token from [Video SDK Account](https://app.videosdk.live/signup).

```js title="JoinActivity.kt"
var sampleToken = "TEMPORARY-TOKEN";
```

### 3. Run the sample app

Run the android app with **Shift+F10** or the **▶ Run** from toolbar.

<br/>

## Key Concepts

- `Meeting` - A Meeting represents Real time audio and video communication.

  **`Note : Don't confuse with Room and Meeting keyword, both are same thing 😃`**

- `Sessions` - A particular duration you spend in a given meeting is a referred as session, you can
  have multiple session of a particular meetingId.
- `Participant` - Participant represents someone who is attending the meeting's
  session, `local partcipant` represents self (You), for this self, other participants
  are `remote participants`.
- `Stream` - Stream means video or audio media content that is either published
  by `local participant` or `remote participants`.

<br/>

## Android Permission

Add all the following permissions to AndroidManifest.xml file.

```
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
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

## [Initialize a Meeting](https://docs.videosdk.live/android/api/sdk-reference/initMeeting)
1. For meeting initialization, you have to first initialize the `VideoSDK`. You can initialize the `VideoSDK` using `initialize()` method.

```js
  VideoSDK.initialize(context: Context)
```

2. After successfully initialization, you can configure `VideoSDK` by passing token in `config` method

```js
  VideoSDK.config(token: String?)
```

3. After VideoSDK initialization and configuration, you can initialize the meeting using `initMeeting()` method. `initMeeting()` will generate a new `Meeting` class and the initiated meeting will be returned.

```js
  val meeting: Meeting? = VideoSDK.initMeeting(
                            context: Context?,
                            meetingId: String?,
                            name: String?,
                            micEnabled: Boolean,
                            webcamEnabled: Boolean,
                            participantId: String?,
                            mode: String?,
                            customTracks: Map<String?, CustomStreamTrack?>?
                        )
```

<br/>

## [Mute/Unmute Local Audio](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/mic-controls)
```js
// unmute mic
meeting!!.unmuteMic()

// mute mic
meeting!!.muteMic()
```

<br/>

## [Enable/Disable Local Webcam](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/camera-controls)
```js
// enable webcam
meeting!!.enableWebcam()

// disable webcam
meeting!!.disableWebcam()
```

<br/>

## [Switch Local Webcam](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/camera-controls)
```js
// switch webcam
meeting!!.changeWebcam()
```

<br/>

## [Leave or End Meeting](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/leave-end-meeting)
```js
// Only one participant will leave/exit the meeting; the rest of the participants will remain.
meeting!!.leave()

// The meeting will come to an end for each and every participant. So, use this function in accordance with your requirements.
meeting!!.end()
```

<br/>

## [Setup MeetingEventListener](https://docs.videosdk.live/android/api/sdk-reference/meeting-class/meeting-event-listener-class)
By implementing `MeetingEventListener`, VideoSDK sends callbacks to the client app whenever there is a change or update in the meeting after a user joins.

```js
  val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {
        override fun onMeetingJoined() {
           // This event will be emitted when a localParticipant(you) successfully joined the meeting.
        }

        override fun onMeetingLeft() {
           // This event will be emitted when a localParticipant(you) left the meeting.
        }

        override fun onParticipantJoined(participant: Participant) {
           // This event will be emitted when a new participant joined the meeting.
           // [participant]: new participant who joined the meeting
        }

        override fun onParticipantLeft(participant: Participant) {
           // This event will be emitted when a joined participant left the meeting.
           // [participant]: participant who left the meeting
        }
    }
```

<br/>

## [Setup ParticipantEventListener](https://docs.videosdk.live/android/api/sdk-reference/participant-class/participant-event-listener-class)
By implementing `ParticipantEventListener`, VideoSDK sends callbacks to the client app whenever a participant's video, audio, or screen share stream is enabled or disabled.

```js
  val participantEventListener: ParticipantEventListener = object : ParticipantEventListener() {
       override fun onStreamEnabled(stream: Stream) {
          // This event will be triggered whenever a participant's video, audio or screen share stream is enabled.
       }

       override fun onStreamDisabled(stream: Stream) {
          // This event will be triggered whenever a participant's video, audio or screen share stream is disabled.
       }
   }

```

If you want to learn more about, read the complete documentation of [Android VideoSDK](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/getting-started)

<br/>

## Examples
- [Prebuilt SDK Examples](https://github.com/videosdk-live/videosdk-rtc-prebuilt-examples)
- [JavaScript SDK Example](https://github.com/videosdk-live/videosdk-rtc-javascript-sdk-example)
- [React JS SDK Example](https://github.com/videosdk-live/videosdk-rtc-react-sdk-example)
- [React Native SDK Example](https://github.com/videosdk-live/videosdk-rtc-react-native-sdk-example)
- [Flutter SDK Example](https://github.com/videosdk-live/videosdk-rtc-flutter-sdk-example)
- [Android SDK Example](https://github.com/videosdk-live/videosdk-rtc-android-java-sdk-example)
- [iOS SDK Example](https://github.com/videosdk-live/videosdk-rtc-ios-sdk-example)

<br/>

## Documentation
[Read the documentation](https://docs.videosdk.live/) to start using Video SDK.

<br/>

## Community
- [Discord](https://discord.gg/Gpmj6eCq5u) - To get involved with the Video SDK community, ask questions and share tips.
- [Twitter](https://twitter.com/video_sdk) - To receive updates, announcements, blog posts, and general Video SDK tips.
