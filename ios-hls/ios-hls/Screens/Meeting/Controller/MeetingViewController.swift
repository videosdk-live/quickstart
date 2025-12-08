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
        print("👉 Initializing meeting with ID:", meetingId)
        
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
        print("🎥 Starting HLS…")
        
        DispatchQueue.main.async {
            self.meeting?.startHLS()
        }
    }
    
    func stopHLS() {
        print("🛑 Stopping HLS…")
        
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
        print("🎤 Mic toggled →", isMicOn)
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
        print("📷 Webcam toggled →", isWebcamOn)
    }
    
    // MARK: - Leave Meeting
    func leaveMeeting() {
        print("🚪 Leaving meeting…")
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
        if (!isExist  && localParticipant.mode != .SIGNALLING_ONLY) {
            participants.append(localParticipant)
        }
        // add event listener
        localParticipant.addEventListener(self)
    }
    
    func onParticipantJoined(_ participant: Participant) {
        let isExist = participants.first { $0.id == participant.id } != nil
        if (!isExist && participant.mode != .SIGNALLING_ONLY) {
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
                print("")
        }
    }
    
    func onHlsStateChanged(state: HLSState, hlsUrl: HLSUrl?) {
        hlsState = state
        switch (state) {
        case .HLS_PLAYABLE:
            playbackURL = hlsUrl?.playbackHlsUrl ?? ""
            print("HLS URL: \(String(describing: hlsUrl?.toJson() ?? nil))")
        default:
            print("HLS State: \(state.rawValue) || hlsUrl: \(String(describing: hlsUrl ?? nil))")
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
