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

- VideoSDK also allows you to configure the interactive livestream layouts in numerous ways like by simply setting different prebuilt layouts in the configuration or by providing your own [custom template](https://docs.videosdk.live/android/guide/interactive-live-streaming/custom-template) to do the livestream according to your layout choice.

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
- A physical or virtual mobile device running Android 5.0 or later
- Valid [Video SDK Account](https://app.videosdk.live/)

<br/>

## Run the Sample Project

### 1. Clone the sample project

Clone the repository to your local environment.

```js
git clone https://github.com/videosdk-live/quickstart.git
```

### 2. Change Directory.

```js
cd quickstart/android-hls/Videosdk_android_kotlin_hls_quickstart
```

### 3. Update the JoinActivity.kt file

Generate temporary token from [Video SDK Account](https://app.videosdk.live/signup).

```js title="JoinActivity.kt"
private var sampleToken = "TEMPORARY-TOKEN";
```

### 4. Run the sample app

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
- `Mode` - There are 2 types of modes:
  1. `CONFERENCE`: Both audio and video streams will be produced and consumed in this mode.
  2. `VIEWER`: Audio and video streams will not be produced or consumed in this mode.

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

## [Join Meeting](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/start-join-meeting)

```js
meeting!!.join()
```

## [Leave or End Meeting](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/leave-end-meeting)

```js
// Only one participant will leave/exit the meeting; the rest of the participants will remain.
meeting!!.leave();

// The meeting will come to an end for each and every participant. So, use this function in accordance with your requirements.
meeting!!.end();
```

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

        override fun onHlsStateChanged(HlsState: JSONObject) {
           // This event will be emitted whenever meeting HLS state changes.
           // [HlsState] : state of HLS
        }
    };
```

<br/>

## Event associated with HLS

- **onHlsStateChanged** - Whenever meeting HLS state changes, then `onHlsStateChanged` event will trigger.

- You can get the **`playbackHlsUrl`** and **`livestreamUrl`** of the HLS to play it on the Viewer side when the state changes to **`HLS_PLAYABLE`**.

```js
private val meetingEventListener: MeetingEventListener = object : MeetingEventListener() {

  override fun onHlsStateChanged(HlsState: JSONObject) {
    when (HlsState.getString("status")) {
        "HLS_STARTING" -> Log.d("onHlsStateChanged", "Meeting hls is starting")
        "HLS_STARTED" -> Log.d("onHlsStateChanged", "Meeting hls is started")
        "HLS_PLAYABLE" -> {
            Log.d("onHlsStateChanged", "Meeting hls is playable now")
            // on hls playable you will receive playbackHlsUrl
            val playbackHlsUrl = HlsState.getString("playbackHlsUrl")
        }
        "HLS_STOPPING" -> Log.d("onHlsStateChanged", "Meeting hls is stopping")
        "HLS_STOPPED" -> Log.d("onHlsStateChanged", "Meeting hls is stopped")
    }
  }

}
```

## Methods and Listeners for Conference(Speaker) mode

## [Mute/Unmute Local Audio](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/mic-controls)

```js
// unmute mic
meeting!!.unmuteMic()

// mute mic
meeting!!.muteMic()
```

## [Enable/Disable Local Webcam](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/camera-controls)

```js
// enable webcam
meeting!!.enableWebcam()

// disable webcam
meeting!!.disableWebcam()
```

## [Switch Local Webcam](https://docs.videosdk.live/android/guide/video-and-audio-calling-api-sdk/features/camera-controls)

```js
// switch webcam
meeting!!.changeWebcam()
```

## [Start/Stop HLS]()

```js
// start HLS
meeting!!.startHls(config: JSONObject?);

// stop HLS
meeting!!.stopHls();
```

## [Pin/Unpin Participant]()

```js
// pin local participant
meeting!!.getLocalParticipant().pin(type: String?);

// unpin local participant
meeting!!.getLocalParticipant().unpin(type: String?);
```

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
