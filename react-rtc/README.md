# VideoSDK for React JS

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/react/guide/video-and-audio-calling-api-sdk/concept-and-architecture)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.gg/kgAvyxtTxv)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs

## Setup Guide

- Sign up on [VideoSDK](https://app.videosdk.live/) and visit [API Keys](https://app.videosdk.live/api-keys) section to get your API key and Secret key.

- Get familiarized with [Authentication and tokens](https://docs.videosdk.live/react/guide/video-and-audio-calling-api-sdk/authentication-and-token)

<br/>

### Prerequisites

- React Js 16 or later
- Node 10 or later
- Valid [Video SDK Account](https://app.videosdk.live/signup)

## Run the Sample App

### Step 1: Clone the sample project

Clone the repository to your local environment.

```js
git clone https://github.com/videosdk-live/quickstart.git
cd quickstart/react
```

### Step 2: Update the `API.js` file.

Update the `API.js` file with your Authentication Token generated from [VideoSDK Dashboard](https://app.videosdk.live/api-keys).

### Step 3: Install the dependecies

Install all the dependecies to run the project.

```js
npm install
```

### Step 4: Run the Sample App

Bingo, it's time to push the launch button.

```js
npm run start
```

<br/>

## Key Concepts

- `Meeting` - A Meeting represents Real time audio and video communication.

  **`Note : Don't confuse with Room and Meeting keyword, both are same thing 😃`**

- `Sessions` - A particular duration you spend in a given meeting is a referred as session, you can have multiple session of a particular meetingId.
- `Participant` - Participant represents someone who is attending the meeting's session, `local partcipant` represents self (You), for this self, other participants are `remote participants`.
- `Stream` - Stream means video or audio media content that is either published by `local participant` or `remote participants`.

<br/>

## Token Generation

Token is used to create and validate a meeting using API and also initialise a meeting.

🛠️ `Development Environment`:

- You may use a temporary token for development. To create a temporary token, go to VideoSDK [dashboard](https://app.videosdk.live/api-keys) .

🌐 `Production Environment`:

- You must set up an authentication server to authorise users for production. To set up an authentication server, refer to our official example repositories. [videosdk-rtc-api-server-examples](https://github.com/videosdk-live/videosdk-rtc-api-server-examples)

<br/>

## API: Create and Validate meeting

- `create meeting` - Please refer this [documentation](https://docs.videosdk.live/api-reference/realtime-communication/create-room) to create meeting.
- `validate meeting`- Please refer this [documentation](https://docs.videosdk.live/api-reference/realtime-communication/validate-room) to validate the meetingId.

<br/>

## [Initialize a Meeting](https://docs.videosdk.live/react/api/sdk-reference/meeting-provider)

- You can initialize the meeting using `MeetingProvider`. Meeting Provider simplifies configuration of meeting with by wrapping up core logic with `react-context`.

```js
<MeetingProvider
  config={{
    meetingId: "meeting-id",
    micEnabled: true,
    webcamEnabled: true,
    name: "Participant Name",
  }}
  token={"token"}
  joinWithoutUserInteraction // Boolean
></MeetingProvider>
```

<br/>

## [Enable/Disable Local Webcam](https://docs.videosdk.live/react/guide/video-and-audio-calling-api-sdk/handling-media/on-off-camera)

```js
const { toggleWebcam } = useMeeting();

const onClick = () => {
  // Enable/Disable Webcam in Meeting
  toggleWebcam();
};
```

<br/>

## [Mute/Unmute Local Audio](https://docs.videosdk.live/react/guide/video-and-audio-calling-api-sdk/handling-media/mute-unmute-mic)

```js
const { toggleMic } = useMeeting();

const onClick = () => {
  // Enable/Disable Mic in Meeting
  toggleMic();
};
```

<br/>

## [Leave or End Meeting](https://docs.videosdk.live/react/guide/video-and-audio-calling-api-sdk/setup-call/leave-end-meeting)

```js
const { leave, end } = useMeeting();

const onClick = () => {
  // Leave Meeting
  leave();

  //End Meeting
  end();
};
```

<br/>

## [Meeting Event callbacks](https://docs.videosdk.live/react/api/sdk-reference/use-meeting/events)

By registering callback handlers, VideoSDK sends callbacks to the client app whenever there is a change or update in the meeting after a user joins.

```js
function onMeetingJoined() {
  // This event will be emitted when a localParticipant(you) successfully joined the meeting.
  console.log("onMeetingJoined");
}
function onMeetingLeft() {
  // This event will be emitted when a localParticipant(you) left the meeting.
  console.log("onMeetingLeft");
}

const { meetingId, meeting, localParticipant } = useMeeting({
  onMeetingJoined,
  onMeetingLeft,
});
```

<br/>

If you want to learn more about the SDK, read the Complete Documentation of [React VideoSDK](https://docs.videosdk.live/react/api/sdk-reference/setup)

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

[Read the documentation](https://docs.videosdk.live/) to start using VideoSDK.

## Community

- [Discord](https://discord.gg/Gpmj6eCq5u) - To get involved with the Video SDK community, ask questions and share tips.
- [Twitter](https://twitter.com/video_sdk) - To receive updates, announcements, blog posts, and general Video SDK tips.
