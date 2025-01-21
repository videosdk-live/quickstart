//
//  LiveStreamView.swift
//  quick-start-videosdk-lis
//
//  Created by Deep Bhupatkar on 31/12/24.
//

import SwiftUI
import VideoSDKRTC
import WebRTC

struct LiveStreamView: View {
    @Environment(\.presentationMode) var presentationMode
    @ObservedObject var liveStreamViewController = LiveStreamViewController()
    
    @State var streamId: String?
    @State var userName: String?
    @State var isUnMute: Bool = true
    @State var camEnabled: Bool = true
    @State private var currentMode: Mode // meeting mode
    
    init(streamId: String? = nil, userName: String? = nil, mode: Mode) {
        self.streamId = streamId
        self.userName = userName
        self._currentMode = State(initialValue: mode)
    }
    
    private var isAudienceMode: Bool {
        // Derive audience mode from the current participant's mode
        if let localParticipant = liveStreamViewController.participants.first(where: { $0.isLocal }) {
            return localParticipant.mode == .RECV_ONLY
        }
        return currentMode == .RECV_ONLY
    }
    
    var body: some View {
        VStack {
            if liveStreamViewController.participants.isEmpty {
                Text("Stream Initializing")
            } else {
                VStack {
                    // Stream ID Tile
                    HStack {
                        Text("Stream ID: \(liveStreamViewController.streamID)")
                            .padding(.vertical)
                        
                        Button(action: {
                            UIPasteboard.general.string = liveStreamViewController.streamID
                        }) {
                            Image(systemName: "doc.on.doc")
                                .foregroundColor(.blue)
                        }
                    }
                    
                    // Participant List
                    List {
                        ForEach(liveStreamViewController.participants.indices, id: \.self) { index in
                            let participant = liveStreamViewController.participants[index]
                            if participant.mode != .RECV_ONLY {
                                Text("Participant Name: \(participant.displayName)")
                                ZStack {
                                    ParticipantView(track: participant.streams.first(where: { $1.kind == .state(value: .video) })?.value.track as? RTCVideoTrack)
                                        .frame(height: 250)
                                    if participant.streams.first(where: { $1.kind == .state(value: .video) }) == nil {
                                        Color.white.opacity(1.0)
                                            .frame(width: UIScreen.main.bounds.width, height: 250)
                                        Text("No media")
                                    }
                                }
                            }
                        }
                    }
                    
                    // Media Control Buttons
                   if !isAudienceMode {
                        HStack(spacing: 15) {
                            // Button to toggle microphone mute/unmute
                             Button {
                                      isUnMute.toggle()
                                        if isUnMute {
                                            liveStreamViewController.meeting?.unmuteMic()
                                        } else {
                                            liveStreamViewController.meeting?.muteMic()
                                        }
                                      } label: {
                                        ModeButton(
                                            text: isUnMute ? "Mute Mic" : "Unmute Mic",
                                            color: .blue
                                        )
                                    }
                                        
                                        // Button to enable/disable webcam
                              Button {
                                       camEnabled.toggle()
                                         if camEnabled {
                                              liveStreamViewController.meeting?.enableWebcam()
                                         } else {
                                              liveStreamViewController.meeting?.disableWebcam()
                                        }
                                    } label: {
                                        ModeButton(
                                            text: camEnabled ? "Disable Webcam" : "Enable Webcam",
                                            color: .blue
                                        )
                                   }
                         }
                     }
                                
                                // Mode Control Buttons for switching modes
                     HStack(spacing: 15) {
                            // Button to leave the call
                            Button {
                                    liveStreamViewController.meeting?.leave()
                                    presentationMode.wrappedValue.dismiss()
                                    } label: {
                                    ModeButton(text: "Leave Call", color: .red)
                        }
                                    
                        // Button to switch between audience and host modes
                        Button {
                            let newMode: Mode = isAudienceMode ? .SEND_AND_RECV : .RECV_ONLY
                               Task{
                                  await liveStreamViewController.meeting?.changeMode(newMode)
                               }
                                currentMode = newMode
                            } label: {
                                ModeButton(
                                    text: isAudienceMode ? "Switch to Host" : "Switch to Audience",
                                            color: .indigo
                            )
                       }
                   }
                    
                }
            }
        }
        .onAppear {
            VideoSDK.config(token: liveStreamViewController.token)
            if let streamId = streamId, !streamId.isEmpty {
                liveStreamViewController.joinStream(streamId: streamId, userName: userName ?? "Guest", mode: currentMode)
            } else {
                liveStreamViewController.joinRoom(userName: userName ?? "Guest" , mode: currentMode)
            }
        }
    }
}

// Helper View for consistent button styling
struct ModeButton: View {
    let text: String
    let color: Color
    
    var body: some View {
        Text(text)
            .foregroundStyle(Color.white)
            .font(.caption)
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 15)
                    .fill(color)
            )
    }
}

/// VideoView for participant's video
class VideoView: UIView {

    var videoView: RTCMTLVideoView = {
        let view = RTCMTLVideoView()
        view.videoContentMode = .scaleAspectFill
        view.backgroundColor = UIColor.black
        view.clipsToBounds = true
        view.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 250)

        return view
    }()

    init(track: RTCVideoTrack?) {
        super.init(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: 250))
        backgroundColor = .clear
        DispatchQueue.main.async {
            self.addSubview(self.videoView)
            self.bringSubviewToFront(self.videoView)
            track?.add(self.videoView)
        }
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

/// ParticipantView for showing and hiding VideoView
struct ParticipantView: UIViewRepresentable {
    var track: RTCVideoTrack?

    func makeUIView(context: Context) -> VideoView {
        let view = VideoView(track: track)
        view.frame = CGRect(x: 0, y: 0, width: 250, height: 250)
        return view
    }

    func updateUIView(_ uiView: VideoView, context: Context) {
        if track != nil {
            track?.add(uiView.videoView)
        } else {
            track?.remove(uiView.videoView)
        }
    }
}
