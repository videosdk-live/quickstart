//
//  JoinStreamView.swift
//  quick-start-videosdk-lis
//
//  Created by Deep Bhupatkar on 31/12/24.
//

import SwiftUI

struct JoinStreamView: View {
    @State var streamId: String
    @State var name: String
    
    var body: some View {
        NavigationView {
            ZStack {
                // Background gradient
                LinearGradient(colors: [.blue.opacity(0.1), .white],
                             startPoint: .topLeading,
                             endPoint: .bottomTrailing)
                    .ignoresSafeArea()
                
                VStack(spacing: 24) {
                    Text("VideoSDK")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .padding(.top,80)
                    
                    Text("ILS - QuickStart")
                        .font(.title)
                        .fontWeight(.semibold)
                        .padding(.bottom,50)
            
                    // Create Room Button
                    HStack {
                        NavigationLink(
                            destination: LiveStreamView(userName: name ?? "Deep", mode: .SEND_AND_RECV)
                                .navigationBarBackButtonHidden(true)
                        ) {
                            ActionButton(title: "Create Live Stream", icon: "plus.circle.fill")
                        }
                        .padding(.horizontal)
                    }

                    // Stream ID input
                    HStack {
                        
                        TextField("Enter StreamId", text: $streamId)
                            .textFieldStyle(.plain)
                            .autocorrectionDisabled()
                        
                        if !streamId.isEmpty {
                            Button(action: { streamId = "" }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(.gray)
                            }
                        }
                    }
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(.white)
                            .shadow(color: .black.opacity(0.1), radius: 5)
                    )
                    .padding(.horizontal)
                    
                    // Buttons Stack
                    VStack(spacing: 16) {
                        
                        HStack(spacing: 16) {
                            NavigationLink(destination: {
                                if !streamId.isEmpty {
                                    LiveStreamView(streamId: streamId,
                                              userName: name ?? "Guest",
                                                     mode: .SEND_AND_RECV)
                                        .navigationBarBackButtonHidden(true)
                                }
                            }) {
                                ActionButton(title: "Join As Host", icon: "person.fill")
                                    .opacity(streamId.isEmpty ? 0.6 : 1)
                            }
                            .disabled(streamId.isEmpty)
                            
                            NavigationLink(
                                destination: LiveStreamView(
                                    streamId: streamId,
                                    userName: name.isEmpty ? "Guest" : name,
                                    mode: .RECV_ONLY
                                )
                                .navigationBarBackButtonHidden(true)
                            ) {
                                ActionButton(title: "Join As Audience", icon: "person.2.fill")
                                    .opacity(streamId.isEmpty ? 0.6 : 1)
                            }
                            .disabled(streamId.isEmpty)
                        }
                    }
                    .padding(.horizontal)
                    
                    Spacer()
                }
            }
        }
    }
}

struct ActionButton: View {
    let title: String
    let icon: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .frame(width: 24, height: 24)
            Text(title)
                .font(.system(size: 16))
                .fontWeight(.medium)
                .multilineTextAlignment(.center)
                .lineLimit(2)
        }
        .frame(maxWidth: .infinity, minHeight: 60) 
        .padding(.horizontal)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color.blue)
        )
        .foregroundColor(.white)
    }
}
