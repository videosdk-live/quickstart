# VideoSDK RTC iOS SDK

Official iOS SDK of [videosdk.live](https://videosdk.live/)


## Features

- VideoSDK enables opportunity to integrate immersive video experiences into your application.
- Real-time comunication SDK is built with blend of webRTC and optimised UDP protocol. Our SDK helps developers to add real-time audio and video calls to any iOS mobile app.

## Requirements

- iOS 11.0+
- Xcode 12.0+
- Swift 5.0+

## Installation

### CocoaPods

[CocoaPods](https://cocoapods.org) is a dependency manager for Cocoa projects. For usage and installation instructions, visit their website. To integrate VideoSDK into your Xcode project using CocoaPods, specify it in your `Podfile`:

```ruby
pod 'VideoSDKRTC'
or 
pod 'VideoSDKRTC', :git => 'https://github.com/videosdk-live/videosdk-rtc-ios-sdk.git'
```

## Important

- You will need to set 'Enable Bitcode' to false.

## Usage

### Import

```swift
import VideoSDKRTC
```

### Configure VideoSDK

```swift
VideoSDK.config(token: <server token here>)
```

- JWT server token needs to be generated from your server.
- If you don't have your server setup yet, follow this on [How to setup a local server]().

### Initialize Meeting

```swift
let meeting = VideoSDK.initMeeting(
    meetingId: <meetingId>, participantName: <your name>, micEnabled: true, webcamEnabled: true)
```

- First, you need to generate the meeting id or get it from server to initialize the meeting instance.
- For participantName -> provide your name to be displayed in the meeting.
- Optionally set true/false for mic and webcam settings.

### Add Listeners

```swift
meeting?.addEventListener(self)
```

- Implement `MeetingEventListener` in your ViewController to get notified on various meeting events.

### Join

```swift
meeting?.join()
```

## Listeners

### MeetingEventListener

1. `onMeetingJoined()` Called when meeting starts.
2. `onMeetingLeft()` Called when meeting ends.
3. `onParticipantJoined(_ participant: Participant)` Called when new participant joins.
4. `onParticipantLeft(_ participant: Participant)` Called when participant leaves.
5. `onRecordingStarted()` Called when meeting recording starts.
6. `onRecordingStoppped` Called after meeting recording stops.
7. `onLivestreamStarted` Called when livestream starts.
8. `onLivestreamStopped` Called after livestream stops.
9. `onSpeakerChanged(participantId: String?)` Called when active speaker changes.
10. `onMicRequested(participantId: String?, accept: @escaping () -> Void, reject: @escaping () -> Void)` Called when someone requests to enable mic.
11. `onWebcamRequested(participantId: String?, accept: @escaping () -> Void, reject: @escaping () -> Void)` Called when someone requests to enable camera.

### ParticipantEventListener

1. `onStreamEnabled(_ stream: MediaStream, forParticipant participant: Participant)` Called when participant turns on the mic or camera.
2. `onStreamDisabled(_ stream: MediaStream, forParticipant participant: Participant)` Called when participant turns off the mic or camera.

## Permissions

- Your app needs to add permissions to use microphone and camera. Add below code your app's info.plist.

```swift
<key>NSCameraUsageDescription</key>
<string>Allow camera access to start video.</string>

<key>NSMicrophoneUsageDescription</key>
<string>Allow microphone access to start audio.</string>
```

## Example

See [Example](https://github.com/videosdk-live/videosdk-rtc-ios-sdk-example) for more details.
