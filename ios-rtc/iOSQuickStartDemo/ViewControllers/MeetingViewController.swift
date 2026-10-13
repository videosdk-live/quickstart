//
//  ViewController.swift
//  iOSQuickStartDemo
//
//  Created by Parth Asodariya on 13/03/23.
//

import UIKit
import VideoSDKRTC
import AVFoundation

class MeetingViewController: UIViewController {
    // outlet for local participant container view
    @IBOutlet weak var localParticipantViewContainer: UIView!
    // outlet for label for meeting Id
    @IBOutlet weak var lblMeetingId: UILabel!
    // outlet for local participant video view
    @IBOutlet weak var localParticipantVideoView: RTCMTLVideoView!
    // outlet for remote participant video view
    @IBOutlet weak var remoteParticipantVideoView: RTCMTLVideoView!
    // outlet for remote participant no media label
    @IBOutlet weak var lblRemoteParticipantNoMedia: UILabel!
    // outlet for remote participant container view
    @IBOutlet weak var remoteParticipantViewContainer: UIView!
    // outlet for local participant no media label
    @IBOutlet weak var lblLocalParticipantNoMedia: UILabel!
    // outlet for leave button
    @IBOutlet weak var btnLeave: UIButton!
    // outlet for toggle video button
    @IBOutlet weak var btnToggleVideo: UIButton!
    // outlet for toggle audio button
    @IBOutlet weak var btnToggleMic: UIButton!
    
    // bool for mic
    var micEnabled = true
    // bool for video
    var videoEnabled = true
    
    /// Meeting data - required to start
    var meetingData: MeetingData!
    
    /// current room reference
    private var room: Room?
    
    // MARK: - video participants including self to show in UI
    private var participants: [Participant] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Utils.loaderShow(viewControler: self)
        
        // init meeting
        initializeMeeting()
        
        // set meeting id in button text
        lblMeetingId.text = "Meeting Id: \(meetingData.meetingId)"
        
//        localParticipantVideoView.frame = CGRect(x: 10, y: 0, width: localParticipantViewContainer.frame.width, height: localParticipantViewContainer.frame.height)
//        localParticipantVideoView.bounds = CGRect(x: 10, y: 0, width: localParticipantViewContainer.frame.width, height: localParticipantViewContainer.frame.height)
//        localParticipantVideoView.clipsToBounds = true
//
//        remoteParticipantVideoView.frame = CGRect(x: 10, y: 0, width: remoteParticipantViewContainer.frame.width, height: remoteParticipantViewContainer.frame.height)
//        remoteParticipantVideoView.bounds = CGRect(x: 10, y: 0, width: remoteParticipantViewContainer.frame.width, height: remoteParticipantViewContainer.frame.height)
//        remoteParticipantVideoView.clipsToBounds = true
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        navigationController?.navigationBar.isHidden = true
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        navigationController?.navigationBar.isHidden = false
        NotificationCenter.default.removeObserver(self)
    }
    
    @IBAction func btnLeaveTapped(_ sender: Any) {
        Task { @MainActor in
            do {
                try await self.room?.leave()
            } catch {
                print("Leave failed: \(error)")
            }
            self.dismiss(animated: true)
        }
    }
    
    
    @IBAction func btnToggleMicTapped(_ sender: Any) {
        guard let room = room else { return }
        Task {
            do {
                // read the SDK's state instead of flipping a local flag
                if room.isMicEnabled {
                    try await room.muteMic()
                } else {
                    try await room.unmuteMic()
                }
            } catch {
                print("Mic toggle failed: \(error)")
            }
            micEnabled = room.isMicEnabled
        }
    }
    
    
    @IBAction func btnToggleVideoTapped(_ sender: Any) {
        guard let room = room else { return }
        Task {
            do {
                if room.isCamEnabled {
                    try await room.disableCam()
                } else {
                    try await room.enableCam()
                }
            } catch {
                print("Webcam toggle failed: \(error)")
            }
            videoEnabled = room.isCamEnabled
        }
    }
    
    // MARK: - Meeting
    
    private func initializeMeeting() {
        
        // initialize
        do {
            room = try VideoSDK.createRoom(
                roomId: meetingData.meetingId,
                token: meetingData.token,
                participantName: meetingData.name,
                micEnabled: meetingData.micEnabled,
                camEnabled: meetingData.cameraEnabled
            )
        } catch {
            Utils.loaderDismiss(viewControler: self)
            print("Setup failed: \(error)")   // ERROR_INVALID_PARAMETER for an empty token or room ID
            return
        }
        
        // listener
        room?.addEventListener(self)
        
        // join
        Task {
            do {
                try await room?.join()
            } catch {
                print("Join failed: \(error)")
            }
        }
    }

}

private extension MeetingViewController {
    
    func updateUI(participant: Participant, forStream stream: RTCStream, enabled: Bool) { // true
        switch stream.kind {
        case .video:
            if let videotrack = stream.track as? RTCVideoTrack {
                if enabled {
                    DispatchQueue.main.async {
                        UIView.animate(withDuration: 0.5){
                            if(participant.isLocal){
                                self.localParticipantViewContainer.isHidden = false
                                self.localParticipantVideoView.isHidden = false
                                self.localParticipantVideoView.videoContentMode = .scaleAspectFill
                                self.localParticipantViewContainer.bringSubviewToFront(self.localParticipantVideoView)
                                videotrack.add(self.localParticipantVideoView)
                                self.lblLocalParticipantNoMedia.isHidden = true
                            } else {
                                self.remoteParticipantViewContainer.isHidden = false
                                self.remoteParticipantVideoView.isHidden = false
                                self.remoteParticipantVideoView.videoContentMode = .scaleAspectFill
                                self.remoteParticipantViewContainer.bringSubviewToFront(self.remoteParticipantVideoView)
                                videotrack.add(self.remoteParticipantVideoView)
                                self.lblRemoteParticipantNoMedia.isHidden = true
                            }
                        }
                    }
                } else {
                    UIView.animate(withDuration: 0.5){
                        if(participant.isLocal){
                            self.localParticipantViewContainer.isHidden = false
                            self.localParticipantVideoView.isHidden = true
                            self.lblLocalParticipantNoMedia.isHidden = false
                            videotrack.remove(self.localParticipantVideoView)
                        } else {
                            self.remoteParticipantViewContainer.isHidden = false
                            self.remoteParticipantVideoView.isHidden = true
                            self.lblRemoteParticipantNoMedia.isHidden = false
                            videotrack.remove(self.remoteParticipantVideoView)
                        }
                    }
                }
            }
            
        case .audio:
            if participant.isLocal {
                localParticipantViewContainer.layer.borderWidth = 4.0
                localParticipantViewContainer.layer.borderColor = enabled ? UIColor.clear.cgColor : UIColor.red.cgColor
            } else {
                remoteParticipantViewContainer.layer.borderWidth = 4.0
                remoteParticipantViewContainer.layer.borderColor = enabled ? UIColor.clear.cgColor : UIColor.red.cgColor
            }
            
        default:
            break
        }
    }
}

// MARK: - RoomEventListener

extension MeetingViewController: RoomEventListener {
    
    /// Meeting started
    func onRoomJoined() {
        
        // handle local participant on start
        guard let localParticipant = self.room?.localParticipant else { return }
            // add to list
        participants.append(localParticipant)
        
        // add event listener
        localParticipant.addEventListener(self)
        
        Utils.loaderDismiss(viewControler: self)
        
        if(localParticipant.isLocal){
            self.localParticipantViewContainer.isHidden = false
        } else {
            self.remoteParticipantViewContainer.isHidden = false
        }
    }
    
    /// Meeting ended
    func onRoomLeft(reason: LeaveReason) {
        
        // remove listeners
        room?.localParticipant.removeEventListener(self)
        room?.removeEventListener(self)
        
    }
    
    /// A new participant joined
    func onParticipantJoined(_ participant: Participant) {
        participants.append(participant)
        
        // add listener
        participant.addEventListener(self)
        
        Task {
            do {
                try await participant.setQuality(.high)
            } catch {
                print("Error in setQuality: \(error)")
            }
        }
        
        if(participant.isLocal){
            self.localParticipantViewContainer.isHidden = false
        } else {
            self.remoteParticipantViewContainer.isHidden = false
        }
    }
    
    /// A participant left from the meeting
    /// - Parameter participant: participant object
    func onParticipantLeft(_ participant: Participant, reason: LeaveReason) {
        participant.removeEventListener(self)
        guard let index = self.participants.firstIndex(where: { $0.id == participant.id }) else {
            return
        }
        // remove participant from list
        participants.remove(at: index)
        // hide from ui
        UIView.animate(withDuration: 0.5){
            if(!participant.isLocal){
                self.remoteParticipantViewContainer.isHidden = true
            }
        }
    }
    
    /// Called when speaker is changed
    /// - Parameter activeSpeakerId: participant id of the speaker, nil when no one is speaking.
    func onSpeakerChanged(activeSpeakerId participantId: String?) {
        
        // show indication for active speaker
        if let participant = participants.first(where: { $0.id == participantId }) {
            self.showActiveSpeakerIndicator(participant.isLocal ? localParticipantViewContainer : remoteParticipantViewContainer, true)
        }
        
        // hide indication for others participants
        let otherParticipants = participants.filter { $0.id != participantId }
        for participant in otherParticipants {
            if participants.count > 1 && participant.isLocal {
                showActiveSpeakerIndicator(localParticipantViewContainer, false)
            } else {
                showActiveSpeakerIndicator(remoteParticipantViewContainer, false)
            }
        }
    }
    
    func showActiveSpeakerIndicator(_ view: UIView, _ show: Bool) {
        view.layer.borderWidth = 4.0
        view.layer.borderColor = show ? UIColor.blue.cgColor : UIColor.clear.cgColor
    }
    
    func onQualityLimitation(type: VideoSDKRTC.QualityLimitationType, state: VideoSDKRTC.QualityLimitationState, timestamp: Int) {
        print("Quality limitation of type \(type) is now \(state)")
    }
}

// MARK: - ParticipantEventListener

extension MeetingViewController: ParticipantEventListener {
    
    /// Participant has enabled mic, video or screenshare
    /// - Parameters:
    ///   - stream: enabled stream object
    ///   - participant: participant object
    func onStreamEnabled(_ stream: RTCStream, forParticipant participant: Participant) {
        updateUI(participant: participant, forStream: stream, enabled: true)
    }
    
    /// Participant has disabled mic, video or screenshare
    /// - Parameters:
    ///   - stream: disabled stream object
    ///   - participant: participant object
    func onStreamDisabled(_ stream: RTCStream, forParticipant participant: Participant) {
        updateUI(participant: participant, forStream: stream, enabled: false)
    }
}
