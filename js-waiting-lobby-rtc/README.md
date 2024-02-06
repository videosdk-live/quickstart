# VideoSDK for Javascript

[![Documentation](https://img.shields.io/badge/Read-Documentation-blue)](https://docs.videosdk.live/react/guide/video-and-audio-calling-api-sdk/getting-started)
[![Discord](https://img.shields.io/discord/876774498798551130?label=Join%20on%20Discord)](https://discord.gg/kgAvyxtTxv)
[![Register](https://img.shields.io/badge/Contact-Know%20More-blue)](https://app.videosdk.live/signup)

At Video SDK, we’re building tools to help companies create world-class collaborative products with capabilities of live audio/videos, compose cloud recordings/rtmp/hls and interaction APIs

## Setup Guide

- Sign up on [VideoSDK](https://app.videosdk.live/) and visit [API Keys](https://app.videosdk.live/api-keys) section to get your API key and Secret key.

<br/>

### Prerequisites

- Valid [Video SDK Account](https://app.videosdk.live/signup)

## Run the Sample App

### Step 1: Clone the repository to your local environment.

```js
git clone https://github.com/videosdk-live/quickstart.git
cd quickstart/js-waiting-lobby-rtc/
```

### Step 2: Create a new file config.js and Copy the config.example.js file's data to config.js file

### Step 3: Update the `config.js` file with your Authentication Token generated from your backend.

Generate token with `crawler` roles and paste that token in `TOKEN`
Generate one token with `allow_join` permission and paste that token in `HOST_TOKEN`.
Generate another token with `ask_join` permission and paste that token in `GUEST_TOKEN`.

- To learn more about Authentication and token in detail you can follow this [guide](https://docs.videosdk.live/javascript/guide/video-and-audio-calling-api-sdk/authentication-and-token).

```
TOKEN = "CRAWLER_ROLE_TOKEN";
HOST_TOKEN = "ALLOW_JOIN_PERMISSION_TOKEN";
GUEST_TOKEN = "ASK_JOIN_PERMISSION_TOKEN";
```

### Step 4: Run the app

```sh
npm install -g live-server
live-server --port=8000
```

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
