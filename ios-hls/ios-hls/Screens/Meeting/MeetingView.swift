//
//  MeetingView.swift
//  ios-hls
//
//  Created by Parth Asodariya on 04/12/25.
//

import SwiftUI
import VideoSDKRTC

struct MeetingView: View {
    
    @StateObject private var controller: MeetingViewController
    @Environment(\.dismiss) var dismiss
    
    init(meetingId: String, role: UserRole) {
        _controller = StateObject(
            wrappedValue: MeetingViewController(meetingId: meetingId, role: role)
        )
    }
    
    // Grid layout
    private let columns = [
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10),
    ]
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 20) {
                
                // HEADER
                header
                
                if controller.role == .host {
                    // HLS State
                    Text("Current HLS State : \(controller.hlsState.rawValue)")
                        .foregroundColor(.white)
                        .font(.headline)
                    
                    // Participants Grid
                    ScrollView {
                        LazyVGrid(columns: columns) {
                            ForEach(controller.participants, id: \.id) { participant in
                                ParticipantContainerView(
                                    participant: participant,
                                    controller: controller
                                )
                                .frame(height: 200, alignment: .center)
                                
                            }
                        }
                    }
                    
                    Spacer()
                } else {
                    if ((controller.hlsState == .HLS_STARTED || controller.hlsState == .HLS_PLAYABLE) && controller.playbackURL != nil) {
                        HLSVideoPlayer(
                            url: URL(string: controller.playbackURL ?? "")!,
                            width: .infinity,
                            height: .infinity
                        )
                        .cornerRadius(12)
                        .padding()
                       
                        Spacer()
                    } else {
                        Spacer()
                        Text("Waiting for host\nto start the live streaming")
                            .font(.title)
                            .foregroundStyle(.white)
                            .bold(true)
                            .multilineTextAlignment(.center)
                        Spacer()
                    }
                }
                
                // Host Controls
                if controller.role == .host {
                    hostControls
                }
            }
        }
        .navigationBarBackButtonHidden(true)
        .navigationBarHidden(true)
    }
    
    
    // MARK: Header UI
    private var header: some View {
        HStack {
            Text("Meeting : \(controller.meetingId)")
                .foregroundColor(.white)
                .font(.title3.bold())
            
            Spacer()
            
            Button {
                controller.leaveMeeting()
                dismiss()
            } label: {
                Text("Leave")
                    .font(.headline)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 10)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
        }
        .padding(.horizontal)
        .padding(.top)
    }
    
    
    // MARK: Host bottom controls
    private var hostControls: some View {
        HStack(spacing: 14) {
            
            if (controller.hlsState == .HLS_STARTING || controller.hlsState == .HLS_STARTED || controller.hlsState == .HLS_PLAYABLE) {
                    controlButton(title: "Stop HLS") {
                        controller.stopHLS()
                    }
            } else {
                controlButton(title: "Start HLS") {
                    controller.startHLS()
                }
            }
            
            controlButton(title: "Toggle Webcam") {
                controller.toggleWebcam()
            }
            
            controlButton(title: "Toggle Mic") {
                controller.toggleMic()
            }
        }
        .padding(.bottom, 20)
    }
    
    
    // MARK: Reusable UI Elements
    func controlButton(title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.headline)
                .padding(.horizontal, 14)
                .padding(.vertical, 12)
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
        }
    }
}
