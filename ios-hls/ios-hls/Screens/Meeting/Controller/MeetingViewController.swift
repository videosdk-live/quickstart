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

// MARK: - MeetingViewController (ViewModel)
class MeetingViewController: ObservableObject {
    
    @Published var participants: [Participant] = []
    @Published var hlsState: HLSState = .HLS_STOPPED
    
    @Published var isMicOn: Bool = true
    @Published var isWebcamOn: Bool = true
    @Published var meeting: Room? = nil
    @Published var participantVideoTracks: [String: RTCVideoTrack] = [:]
    @Published var participantMicStatus: [String: Bool] = [:]
    @Published var playbackURL: String? = nil

    // Navigation trigger to exit meeting screen
    @Published var shouldExitMeeting: Bool = false
    
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
        Task { @MainActor in
            // the camera track needs camera permission first; without it the SDK opens the default camera
            let isHost = self.role != .viewer
            let videoMediaTrack = isHost ? try? await VideoSDK.createCameraVideoTrack(
                encoderConfig: .h720p_w1280p,
                facingMode: .front,
                multiStream: true
            ) : nil
            do {
                self.meeting = try VideoSDK.createRoom(
                    roomId: self.meetingId,
                    token: AUTH_TOKEN,
                    participantName: "John doe",
                    micEnabled: isHost && self.isMicOn,
                    camEnabled: isHost && self.isWebcamOn,
                    mode: isHost ? .SEND_AND_RECV : .SIGNALLING_ONLY,
                    multiStream: true,
                    customCameraTrack: videoMediaTrack
                )
            } catch {
                print("Setup failed: \(error)")
                self.shouldExitMeeting = true
                return
            }

            // Add event listeners and join the meeting
            self.meeting?.addEventListener(self)
            do {
                try await self.meeting?.join()
            } catch {
                print("Join failed: \(error)")
                self.shouldExitMeeting = true
            }
        }
    }
    
    
    // MARK: - HLS Handling
    func startHLS() {
        Task { try? await self.meeting?.startHLS() }
    }
    
    func stopHLS() {
        Task { try? await self.meeting?.stopHLS() }
    }
    
    
    // MARK: - Media Toggles
    func toggleMic() {
        guard let room = meeting else { return }
        Task { @MainActor in
            do {
                if room.isMicEnabled { try await room.muteMic() } else { try await room.unmuteMic() }
            } catch {
                print("Mic toggle failed: \(error)")
            }
            self.isMicOn = room.isMicEnabled
        }
    }
    
    func toggleWebcam() {
        guard let room = meeting else { return }
        Task { @MainActor in
            do {
                if room.isCamEnabled { try await room.disableCam() } else { try await room.enableCam() }
            } catch {
                print("Webcam toggle failed: \(error)")
            }
            self.isWebcamOn = room.isCamEnabled
        }
    }
    
    // MARK: - Leave Meeting
    func leaveMeeting() {
        if (role == .host) {
            if (self.hlsState == .HLS_STARTED || self.hlsState == .HLS_PLAYABLE || self.hlsState == .HLS_STARTING) {
                stopHLS()
            }
        }
        Task { try? await self.meeting?.leave() }
    }
}

extension MeetingViewController: RoomEventListener {
    
    func onRoomJoined() {
        guard let localParticipant = self.meeting?.localParticipant else { return }
        let isExist = participants.first { $0.id == localParticipant.id } != nil
        if (!isExist  && (localParticipant.mode != .SIGNALLING_ONLY)) {
            participants.append(localParticipant)
        }
        // add event listener
        localParticipant.addEventListener(self)
    }
    
    func onParticipantJoined(_ participant: Participant) {
        let isExist = participants.first { $0.id == participant.id } != nil
        if (!isExist && (participant.mode != .SIGNALLING_ONLY)) {
            participants.append(participant)
        }
        // add listener
        participant.addEventListener(self)
    }
    
    func onParticipantLeft(_ participant: Participant, reason: LeaveReason) {
        participants = participants.filter({ $0.id != participant.id })
    }
    
    func onRoomLeft(reason: LeaveReason) {
        meeting?.localParticipant.removeEventListener(self)
        meeting?.removeEventListener(self)
        participants.removeAll()
        // Trigger exit when meeting is left
        DispatchQueue.main.async {
            self.shouldExitMeeting = true
        }
    }
    
    func onRoomStateChanged(roomState meetingState: RoomState) {
        switch meetingState {
        case .DISCONNECTED, .FAILED:
            print("meeting state: \(meetingState.rawValue)")
            
            participants.removeAll()
            // Trigger exit on disconnect/failure
            DispatchQueue.main.async {
                self.shouldExitMeeting = true
            }
        default:
            print("Meeting State: \(meetingState.rawValue)")
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
    
    func onQualityLimitation(type: VideoSDKRTC.QualityLimitationType, state: VideoSDKRTC.QualityLimitationState, timestamp: Int) {
        print("Quality limitation of \(type) is \(state)")
    }
    
}

extension MeetingViewController: ParticipantEventListener {
    func onStreamEnabled(_ stream: RTCStream, forParticipant participant: Participant) {
        if let track = stream.track as? RTCVideoTrack {
            DispatchQueue.main.async {
                if stream.kind == .video {
                    self.participantVideoTracks[participant.id] = track
                }
            }
        }
        
        if stream.kind == .audio {
            self.participantMicStatus[participant.id] = true // Mic enabled
        }
    }

    func onStreamDisabled(_ stream: RTCStream, forParticipant participant: Participant) {
        DispatchQueue.main.async {
            if stream.kind == .video {
                self.participantVideoTracks.removeValue(forKey: participant.id)
            }
        }
        if stream.kind == .audio {
            // Update microphone state for this participant
            self.participantMicStatus[participant.id] = false // Mic disabled

        }
    }
}

