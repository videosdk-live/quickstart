//
//  ViewController.swift
//  iOSQuickStartDemo
//
//  Created by Parth Asodariya on 13/03/23.
//

import UIKit
import VideoSDKRTC
import WebRTC
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
    
    /// current meeting reference
    private var meeting: Meeting?
    
    // MARK: - video participants including self to show in UI
    private var participants: [Participant] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Utils.loaderShow(viewControler: self)
        
        // config
        VideoSDK.config(token: meetingData.token)
        
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
        DispatchQueue.main.async {
            self.meeting?.leave()
            self.dismiss(animated: true)
        }
    }
    
    
    @IBAction func btnToggleMicTapped(_ sender: Any) {
        if micEnabled {
            micEnabled = !micEnabled // false
            self.meeting?.muteMic()
        } else {
            micEnabled = !micEnabled // true
            self.meeting?.unmuteMic()
        }
    }
    
    
    @IBAction func btnToggleVideoTapped(_ sender: Any) {
        if videoEnabled {
            videoEnabled = !videoEnabled // false
            self.meeting?.disableWebcam()
        } else {
            videoEnabled = !videoEnabled // true
            self.meeting?.enableWebcam()
        }
    }
    
    // MARK: - Meeting
    
    private func initializeMeeting() {
        
        // initialize
        meeting = VideoSDK.initMeeting(
            meetingId: meetingData.meetingId,
            participantName: meetingData.name,
            micEnabled: meetingData.micEnabled,
            webcamEnabled: meetingData.cameraEnabled
        )
        
        // listener
        meeting?.addEventListener(self)
        
        // join
        meeting?.join()
    }

}

private extension MeetingViewController {
    
    func updateUI(participant: Participant, forStream stream: MediaStream, enabled: Bool) { // true
        switch stream.kind {
        case .state(value: .video):
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
            
        case .state(value: .audio):
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

// MARK: - MeetingEventListener

extension MeetingViewController: MeetingEventListener {
    
    /// Meeting started
    func onMeetingJoined() {
        
        // handle local participant on start
        guard let localParticipant = self.meeting?.localParticipant else { return }
            // add to list
        participants.append(localParticipant)
        
        // add event listener
        localParticipant.addEventListener(self)
        
        localParticipant.setQuality(.high)
        
        Utils.loaderDismiss(viewControler: self)
        
        if(localParticipant.isLocal){
            self.localParticipantViewContainer.isHidden = false
        } else {
            self.remoteParticipantViewContainer.isHidden = false
        }
    }
    
    /// Meeting ended
    func onMeetingLeft() {
        
        // remove listeners
        meeting?.localParticipant.removeEventListener(self)
        meeting?.removeEventListener(self)
        
    }
    
    /// A new participant joined
    func onParticipantJoined(_ participant: Participant) {
        participants.append(participant)
        
        // add listener
        participant.addEventListener(self)
        
        participant.setQuality(.high)
        
        if(participant.isLocal){
            self.localParticipantViewContainer.isHidden = false
        } else {
            self.remoteParticipantViewContainer.isHidden = false
        }
    }
    
    /// A participant left from the meeting
    /// - Parameter participant: participant object
    func onParticipantLeft(_ participant: Participant) {
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
    /// - Parameter participantId: participant id of the speaker, nil when no one is speaking.
    func onSpeakerChanged(participantId: String?) {
        
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
}

// MARK: - ParticipantEventListener

extension MeetingViewController: ParticipantEventListener {
    
    /// Participant has enabled mic, video or screenshare
    /// - Parameters:
    ///   - stream: enabled stream object
    ///   - participant: participant object
    func onStreamEnabled(_ stream: MediaStream, forParticipant participant: Participant) {
        updateUI(participant: participant, forStream: stream, enabled: true)
    }
    
    /// Participant has disabled mic, video or screenshare
    /// - Parameters:
    ///   - stream: disabled stream object
    ///   - participant: participant object
    func onStreamDisabled(_ stream: MediaStream, forParticipant participant: Participant) {
        updateUI(participant: participant, forStream: stream, enabled: false)
    }
}
