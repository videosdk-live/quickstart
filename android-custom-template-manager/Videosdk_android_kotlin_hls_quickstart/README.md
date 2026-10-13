<p align="center">
<img width="400" src="https://static.videosdk.live/videosdk_logo_website_black.png"/>
</p>

---

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/getting-started)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.gg/bGZtAbwvab)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs.

## Interactive Livestream (HLS)

- Interactive live stream (HLS) is a media streaming protocol for delivering visual and audio media to viewers over the internet.
- Interactive live stream (HLS) allows you to distribute content and ensure excellent viewing experiences across devices, playback platforms, and network conditions. It is the ideal protocol for streaming video to large audiences scattered across geographies.

<p align="center">
<img src="https://cdn.videosdk.live/website-resources/docs-resources/mobile_hls.png"/>
</p>

- VideoSDK also allows you to configure the interactive livestream layouts in numerous ways like by simply setting different prebuilt layouts in the configuration or by providing your own [custom template](https://docs.videosdk.live/docs/tutorials/customized-layout) to do the livestream according to your layout choice.

<br/>

> **Note** :
>
> With VideoSDK, you can also use your own custom designed layout template to livestream the meetings. In order to use the custom template, you need to create a template for which you can [follow these guide](https://docs.videosdk.live/docs/tutorials/customized-layout). Once you have setup the template, you can use the [REST API to start](https://docs.videosdk.live/api-reference/realtime-communication/start-hlsStream) the livestream with the `templateURL` parameter.

<br/>

## Setup Guide

- Sign up on [VideoSDK](https://app.videosdk.live) and visit [API Keys](https://app.videosdk.live/api-keys) section to get your API key and Secret key.

- Get familiarized with [API key and Secret key](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/signup-and-create-api).

- Get familiarized with [Token](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/server-setup).

<br/>

## Prerequisites

- Development environment requirements:
  - [Java Development Kit](https://www.oracle.com/java/technologies/downloads/)
  - Android Studio 3.0 or later
- An ARM Android device (or an `arm64-v8a` emulator image) running Android 7.0 (API 24) or later
- Kotlin 1.8 or later: VideoSDK Android 3.x is Kotlin-only
- Valid [Video SDK Account](https://app.videosdk.live/)

<br/>

## Run the Sample Project

### 1. Clone the sample project

Clone the repository to your local environment.

```js
git clone https://github.com/videosdk-live/quickstart.git
```

### 2. Update the JoinActivity.kt file

Generate temporary token from [Video SDK Account](https://app.videosdk.live/signup).

```js title="JoinActivity.kt"
private var sampleToken = "TEMPORARY-TOKEN";
```

### 3. Run the sample app

Run the android app with **Shift+F10** or the **▶ Run** from toolbar.

<br/>

## Key Concepts

- `Room` - A Room represents Real time audio and video communication. (`Room` was called `Meeting` before 3.0.0.)

- `Sessions` - A particular duration you spend in a given meeting is a referred as session, you can
  have multiple session of a particular roomId.
- `Participant` - Participant represents someone who is attending the meeting's
  session, `local partcipant` represents self (You), for this self, other participants
  are `remote participants`.
- `RTCStream` - Stream means video or audio media content that is either published
  by `local participant` or `remote participants`.
- `Mode` - This sample uses 2 of the modes:
  1. `Mode.SEND_AND_RECV` (the host): Both audio and video streams will be produced and consumed in this mode.
  2. `Mode.SIGNALLING_ONLY` (the viewer): Audio and video streams will not be produced or consumed in this mode.

<br/>

## Android Permission

Add all the following permissions to AndroidManifest.xml file.

```
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.INTERNET" />
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

## [Create a Room](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/quick-start)

1. Initialize the `VideoSDK` once with `initialize()`.

```kotlin
  VideoSDK.initialize(applicationContext)
```

2. Create the `Room` with the room id, the token and the mode. Use named arguments.

```kotlin
  val room: Room = VideoSDK.createRoom(
      roomId = meetingId,
      token = token,
      participantName = "John Doe",
      micEnabled = true,
      camEnabled = true,
      mode = Mode.SEND_AND_RECV, // Mode.SIGNALLING_ONLY for a viewer
  )
```

## [Join Meeting](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/start-join-meeting)

`join()` and the other actions are `suspend` functions that throw `VideoSDKException`.

```kotlin
lifecycleScope.launch {
    try {
        room.join()
    } catch (e: VideoSDKException) {
        Log.e("VideoSDK", "Join failed: ${e.code} ${e.name}")
    }
}
```

## [Leave or End Meeting](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/leave-end-meeting)

```kotlin
lifecycleScope.launch {
    // Only one participant will leave/exit the meeting; the rest of the participants will remain.
    room.leave()

    // The meeting will come to an end for each and every participant. So, use this function in accordance with your requirements.
    room.end()
}
```

## [Setup RoomEventListener](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/quick-start)

By implementing `RoomEventListener`, VideoSDK sends callbacks to the client app whenever there is a change or update in the meeting after a user joins.

```kotlin
  val roomEventListener: RoomEventListener = object : RoomEventListener() {

        override fun onRoomJoined(switchRoomId: String?) {
           // This event will be emitted when a localParticipant(you) successfully joined the meeting.
        }

        override fun onRoomLeft(reason: LeaveReason) {
           // This event will be emitted when a localParticipant(you) left the meeting.
        }

        override fun onParticipantJoined(participant: Participant) {
           // This event will be emitted when a new participant joined the meeting.
           // [participant]: new participant who joined the meeting
        }

        override fun onParticipantLeft(participant: Participant, reason: LeaveReason) {
           // This event will be emitted when a joined participant left the meeting.
           // [participant]: participant who left the meeting
        }

        override fun onHlsStateChanged(state: HLSState, hlsUrl: HLSUrl?) {
           // This event will be emitted whenever meeting HLS state changes.
           // [state] : state of HLS, [hlsUrl] : the playback urls once it is HLS_PLAYABLE
        }
    }
```

<br/>

## Event associated with HLS

- **onHlsStateChanged** - Whenever meeting HLS state changes, then `onHlsStateChanged` event will trigger.

- You can get the **`playbackHlsUrl`** and **`livestreamUrl`** of the HLS to play it on the Viewer side when the state changes to **`HLS_PLAYABLE`**.

```kotlin
private val roomEventListener: RoomEventListener = object : RoomEventListener() {

  override fun onHlsStateChanged(state: HLSState, hlsUrl: HLSUrl?) {
    when (state) {
        HLSState.HLS_STARTING -> Log.d("onHlsStateChanged", "Meeting hls is starting")
        HLSState.HLS_STARTED -> Log.d("onHlsStateChanged", "Meeting hls is started")
        HLSState.HLS_PLAYABLE -> {
            Log.d("onHlsStateChanged", "Meeting hls is playable now")
            // on hls playable you will receive playbackHlsUrl
            val playbackHlsUrl = hlsUrl?.playbackHlsUrl
        }
        HLSState.HLS_STOPPING -> Log.d("onHlsStateChanged", "Meeting hls is stopping")
        HLSState.HLS_STOPPED -> Log.d("onHlsStateChanged", "Meeting hls is stopped")
    }
  }

}
```

## Methods and Listeners for Conference(Speaker) mode

## [Mute/Unmute Local Audio](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/mic-controls)

```kotlin
lifecycleScope.launch {
    // unmute mic
    room.unmuteMic()

    // mute mic
    room.muteMic()
}
```

## [Enable/Disable Local Webcam](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/camera-controls)

```kotlin
lifecycleScope.launch {
    // enable webcam
    room.enableCam()

    // disable webcam
    room.disableCam()
}
```

## [Switch Local Webcam](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/camera-controls)

```kotlin
lifecycleScope.launch {
    // move to the next camera
    room.changeCam()
}
```

## [Start/Stop HLS]()

```kotlin
lifecycleScope.launch {
    // start HLS
    room.startHLS(config = HLSConfig(layout = ConfigLayout(type = ConfigLayoutGrid.SPOTLIGHT)))

    // stop HLS
    room.stopHLS()
}
```

## [Pin/Unpin Participant]()

```kotlin
lifecycleScope.launch {
    // pin local participant
    room.localParticipant.pin(PinType.SHARE_AND_CAM)

    // unpin local participant
    room.localParticipant.unpin(PinType.SHARE_AND_CAM)
}
```

## [Setup ParticipantEventListener](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/quick-start)

By implementing `ParticipantEventListener`, VideoSDK sends callbacks to the client app whenever a participant's video, audio, or screen share stream is enabled or disabled.

```kotlin
 val participantEventListener: ParticipantEventListener = object : ParticipantEventListener() {
       override fun onStreamEnabled(stream: RTCStream) {
          // This event will be triggered whenever a participant's video, audio or screen share stream is enabled.
          // It is also called right away, for the streams the participant already has, when you add the listener.
       }

       override fun onStreamDisabled(stream: RTCStream) {
          // This event will be triggered whenever a participant's video, audio or screen share stream is disabled.
       }
   }

```

If you want to learn more about, read the complete documentation of [Android VideoSDK](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/getting-started)

<br/>

## HLS Player

- Here, we're using `ExoPlayer` to show the viewer interactive live streaming. [Click here](https://github.com/google/ExoPlayer) to know more about `ExoPlayer`.

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
