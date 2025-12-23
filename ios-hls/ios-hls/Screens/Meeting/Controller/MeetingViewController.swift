//
//  MeetingViewController.swift
//  ios-hls
//
//  Created by Parth Asodariya on 04/12/25.
//

import Foundation
import SwiftUI
import Combine
import VideoSDKRTC
internal import Mediasoup

// MARK: - MeetingViewController (ViewModel)
class MeetingViewController: ObservableObject {
    
    @Published var participants: [Participant] = []
    @Published var hlsState: HLSState = .HLS_STOPPED
    @Published var isMicOn: Bool = true
    @Published var isWebcamOn: Bool = true
    @Published var meeting: Meeting? = nil
    @Published var participantVideoTracks: [String: RTCVideoTrack] = [:]
    @Published var participantMicStatus: [String: Bool] = [:]
    @Published var playbackURL: String? = nil
    
    private var cancellables = Set<AnyCancellable>()
    let meetingId: String
    let role: UserRole
    
    init(meetingId: String, role: UserRole) {
        self.meetingId = meetingId
        self.role = role
        
        // Auto-start meeting logic when created
        initializeMeeting()
    }
    
    // MARK: - Meeting Initialization
    func initializeMeeting() {
        VideoSDK.config(token: AUTH_TOKEN)
        
        let videoMediaTrack = try? VideoSDK.createCameraVideoTrack(
            encoderConfig: .h720p_w1280p,
            facingMode: .front,
            multiStream: true
        )
        meeting = VideoSDK.initMeeting(
            meetingId: meetingId,
            participantName: "John doe",
            micEnabled: self.isMicOn,
            webcamEnabled: self.isWebcamOn,
            customCameraVideoStream: videoMediaTrack,
            multiStream: true,
            mode: self.role == .viewer ? .SIGNALLING_ONLY : .SEND_AND_RECV
        )
        
        // Add event listeners and join the meeting
        meeting?.addEventListener(self)
        meeting?.join()
    }
    
    // MARK: - HLS Handling
    func startHLS() {
        DispatchQueue.main.async {
            self.meeting?.startHLS()
        }
    }
    
/// NOTE: This function will be used when using the custom template to display the HLS preview,
///    If you are using custom template then uncomment below function and use it when you want to start HLS
//    func startHLS(meetingId: String, token: String) {
//        let templateUrl = "TEMPLATE_URL"
//        // "https://lab.videosdk.live/react-custom-template-demo?meetingId=\(meetingId)&token=\(token)"
//
//        let body: [String: Any] = [
//            "roomId": meetingId,
//            "templateUrl": templateUrl,
//            "config": [
//                "orientation": "portrait"
//            ]
//        ]
//
//        guard let url = URL(string: "https://api.videosdk.live/v2/hls/start") else { return }
//
//        var request = URLRequest(url: url)
//        request.httpMethod = "POST"
//        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
//        request.setValue(token, forHTTPHeaderField: "Authorization")
//        request.httpBody = try? JSONSerialization.data(withJSONObject: body)
//        
//        URLSession.shared.dataTask(with: request) { data, response, error in
//            if let error = error {
//                print("API Error:", error.localizedDescription)
//                return
//            }
//
//            guard let data = data else {
//                print("No response data")
//                return
//            }
//
//            do {
//                let json = try JSONSerialization.jsonObject(with: data, options: [])
//                print("HLS Response:", json)
//            } catch {
//                print("JSON Parse Error:", error)
//            }
//        }.resume()
//    }
    
    func stopHLS() {
        DispatchQueue.main.async {
            self.meeting?.stopHLS()
        }
    }
    
    // MARK: - Media Toggles
    func toggleMic() {
        if (isMicOn) {
            DispatchQueue.main.async {
                self.meeting?.muteMic()
            }
        } else {
            DispatchQueue.main.async {
                self.meeting?.unmuteMic()
            }
        }
        isMicOn.toggle()
    }
    
    func toggleWebcam() {
        if (isWebcamOn) {
            DispatchQueue.main.async {
                self.meeting?.disableWebcam()
            }
        } else {
            DispatchQueue.main.async {
                self.meeting?.enableWebcam()
            }
        }
        isWebcamOn.toggle()
    }
    
    // MARK: - Leave Meeting
    func leaveMeeting() {
        if (role == .host) {
            if (self.hlsState == .HLS_STARTED || self.hlsState == .HLS_PLAYABLE || self.hlsState == .HLS_STARTING) {
                self.meeting?.stopHLS()
            }
        }
        self.meeting?.leave()
    }
}

extension MeetingViewController: MeetingEventListener {
    func onMeetingJoined() {
        guard let localParticipant = self.meeting?.localParticipant else { return }
        let isExist = participants.first { $0.id == localParticipant.id } != nil
        if (!isExist  && (localParticipant.mode != .SIGNALLING_ONLY && localParticipant.mode != .VIEWER)) {
            participants.append(localParticipant)
        }
        // add event listener
        localParticipant.addEventListener(self)
        /// NOTE:  This will only work in the custom-template scenario
        Task {
            await meeting?.pubsub.subscribe(topic: "CHANGE_BACKGROUND", forListener: self)
            await meeting?.pubsub.subscribe(topic: "VIEWER_MESSAGE", forListener: self)
        }
    }
    
    func onParticipantJoined(_ participant: Participant) {
        let isExist = participants.first { $0.id == participant.id } != nil
        if (!isExist && (participant.mode != .SIGNALLING_ONLY && participant.mode != .VIEWER)) {
            participants.append(participant)
        }
        // add listener
        participant.addEventListener(self)
    }
    
    func onParticipantLeft(_ participant: Participant) {
        participants = participants.filter({ $0.id != participant.id })
    }
    
    func onMeetingLeft() {
        meeting?.localParticipant.removeEventListener(self)
        meeting?.removeEventListener(self)
        participants.removeAll()
    }
    
    func onMeetingStateChanged(meetingState: MeetingState) {
        switch meetingState {
        case .DISCONNECTED:
                participants.removeAll()
            default:
            print("meeting state: \(meetingState.rawValue)")
        }
    }
    
    func onHlsStateChanged(state: HLSState, hlsUrl: HLSUrl?) {
        hlsState = state
        switch (state) {
        case .HLS_PLAYABLE:
            playbackURL = hlsUrl?.playbackHlsUrl ?? ""
        default:
            print("HLS State: \(state.rawValue)")
        }
    }
    
}

extension MeetingViewController: ParticipantEventListener {
    func onStreamEnabled(_ stream: MediaStream, forParticipant participant: Participant) {
        if let track = stream.track as? RTCVideoTrack {
            DispatchQueue.main.async {
                if case .state(let mediaKind) = stream.kind, mediaKind == .video {
                    self.participantVideoTracks[participant.id] = track
                }
            }
        }
        
        if case .state(let mediaKind) = stream.kind, mediaKind == .audio {
            self.participantMicStatus[participant.id] = true // Mic enabled
        }
    }

    func onStreamDisabled(_ stream: MediaStream, forParticipant participant: Participant) {
        DispatchQueue.main.async {
            if case .state(let mediaKind) = stream.kind, mediaKind == .video {
                self.participantVideoTracks.removeValue(forKey: participant.id)
            }
        }
        if case .state(let mediaKind) = stream.kind, mediaKind == .audio {
            // Update microphone state for this participant
            self.participantMicStatus[participant.id] = false // Mic disabled

        }
    }
}

extension MeetingViewController: PubSubMessageListener {
    
    func onMessageReceived(_ message: VideoSDKRTC.PubSubMessage) {
        /// NOTE:  This will only work in the custom-template scenario
        print("Message received: \(message.message) for topic: \(message.topic)")
    }
    
}
