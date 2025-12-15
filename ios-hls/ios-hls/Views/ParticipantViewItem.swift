//
//  ParticipantViewItem.swift
//  ios-hls
//
//  Created by Parth Asodariya on 04/12/25.
//

import VideoSDKRTC
import SwiftUI
internal import Mediasoup

struct ParticipantContainerView: View {
    let participant: Participant

    @ObservedObject var controller: MeetingViewController
    
    var body: some View {
        ZStack {
            // Main participant view
            participantView(participant: participant, controller: controller)
            
            // Overlay for name and mic status
            VStack {
                Spacer()
                HStack {

                    // Participant name
                    Text(participant.displayName)
                        .foregroundColor(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.black.opacity(0.5))
                        .cornerRadius(4)
                    
                    // Mic status indicator
                    Image(systemName: controller.participantMicStatus[participant.id] ?? false ? "mic.fill" : "mic.slash.fill")
                        .foregroundColor(controller.participantMicStatus[participant.id] ?? false ? .green : .red)
                        .padding(4)
                        .background(Color.black.opacity(0.5))
                        .clipShape(Circle())

                    
                    Spacer()
                }
                .padding(8)
            }
        }
        // Add border, background, shadow, and rounded corners
        .background(Color.black.opacity(0.9)) // Background color
        .cornerRadius(10) // Rounded corners
        .shadow(color: Color.gray.opacity(0.7), radius: 10, x: 0, y: 5) // Shadow effect
        .overlay(
            RoundedRectangle(cornerRadius: 10) // Rounded border
                .stroke(Color.gray.opacity(0.9), lineWidth: 1)
        )

    }
    
    private func participantView(participant: Participant, controller: MeetingViewController) -> some View {
        ZStack {
            ParticipantView(participant: participant, controller: controller)
        }
    }
}

/// VideoView for participant's video
class VideoView: UIView {
    var videoView: RTCMTLVideoView = {
        let view = RTCMTLVideoView()
        view.videoContentMode = .scaleAspectFill
        view.backgroundColor = UIColor.black
        view.clipsToBounds = true
        view.transform = CGAffineTransform(scaleX: 1, y: 1)

        return view
    }()
    
    init(track: RTCVideoTrack?, frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .clear
        
        // Set videoView frame to match parent view
        videoView.frame = bounds
        
        DispatchQueue.main.async {
            self.addSubview(self.videoView)
            self.bringSubviewToFront(self.videoView)
            track?.add(self.videoView)
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        // Update videoView frame when parent view size changes
        videoView.frame = bounds
    }
}

/// ParticipantView for showing and hiding VideoView
struct ParticipantView: View {
    let participant: Participant
    @ObservedObject var controller: MeetingViewController
    
    var body: some View {
        ZStack {
            if let track = controller.participantVideoTracks[participant.id] {
                VideoStreamView(track: track)
            } else {
                Color.white.opacity(1.0)
                Text("No media")
                    .font(.largeTitle)
                    .foregroundStyle(.black)
            }
        }
    }
}

struct VideoStreamView: UIViewRepresentable {
    let track: RTCVideoTrack
    
    func makeUIView(context: Context) -> VideoView {
        let view = VideoView(track: track, frame: .zero)
        return view
    }
    
    func updateUIView(_ uiView: VideoView, context: Context) {
        track.add(uiView.videoView)
        uiView.videoView.videoContentMode = .scaleAspectFill
    }
}

