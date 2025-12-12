//
//  StartMeeting.swift
//  ios-hls
//
//  Created by Parth Asodariya on 04/12/25.
//

import SwiftUI

let AUTH_TOKEN: String = ""

// MARK: - Meeting Role Enum
enum UserRole: String {
    case host = "Host"
    case viewer = "Viewer"
}

struct StartMeetingView: View {
    
    @State private var meetingId: String = ""
    @State private var navigateToMeeting = false
    @State private var selectedRole: UserRole = .host
    @State private var animateButtons = false
    @State private var isLoading = false
    
    var body: some View {
        
        NavigationStack {
            
            ZStack {
                // Background
                LinearGradient(
                    colors: [Color.black, Color.black.opacity(0.9)],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
                
                VStack(spacing: 30) {
                    
                    // Logo
                    Image("logo")
                        .resizable()
                        .renderingMode(.original)
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 175, height: 175)
                        .cornerRadius(25)
                        .shadow(color: .white.opacity(0.5), radius: 20)
                    
                    Spacer()
                    
                    // Create Meeting Button
                    animatedButton(title: "Create Meeting", bgColor: .blue) {
                        if (!isLoading) {
                            Task {
                                await createMeeting()
                            }
                        }
                    }
                    
                    // Meeting ID TextField
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Enter Meeting ID")
                            .font(.headline)
                            .foregroundColor(.white.opacity(0.8))
                        
                        ZStack {
                            RoundedRectangle(cornerRadius: 14)
                                .fill(Color.white)
                                .frame(height: 55)
                            
                            TextField("", text: $meetingId)
                                .foregroundColor(.black)
                                .padding(.horizontal)
                                .textInputAutocapitalization(.never)
                        }
                    }
                    .padding(.horizontal)
                    
                    // Host + Viewer Buttons
                    HStack(spacing: 5) {
                        animatedButton(title: "Join as Host", bgColor: .green) {
                            selectedRole = .host
                            navigateToMeeting = true
                        }
                        
                        animatedButton(title: "Join as Viewer", bgColor: .orange) {
                            selectedRole = .viewer
                            navigateToMeeting = true
                        }
                    }
                    
                    Spacer()
                }
                .padding()
            }
            .navigationDestination(isPresented: $navigateToMeeting) {
                MeetingView(meetingId: meetingId, role: selectedRole)
            }
        }
    }
    
    // MARK: - API: Create Meeting
    func createMeeting() async {
        guard let url = URL(string: "https://api.videosdk.live/v2/rooms") else { return }
        
        isLoading = true
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue(AUTH_TOKEN, forHTTPHeaderField: "Authorization")
        
        do {
            let (data, _) = try await URLSession.shared.data(for: request)
            let decoded = try JSONDecoder().decode(RoomsStruct.self, from: data)
            
            await MainActor.run {
                if let roomId = decoded.roomID {
                    self.meetingId = roomId
                    self.selectedRole = .host
                    self.navigateToMeeting = true
                }
            }
            
        } catch {
            print("❌ Create meeting failed:", error)
        }
        
        isLoading = false
    }
    
    
    // MARK: - Reusable Animated Button
    @ViewBuilder
    func animatedButton(title: String, bgColor: Color, action: @escaping () -> Void) -> some View {
        Button(action: {
            withAnimation(.spring(response: 0.25, dampingFraction: 0.5)) {
                action()
            }
        }) {
            Text(title)
                .font(.headline)
                .foregroundColor(.white)
                .padding(.horizontal, 5)
                .frame(height: 55)
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(bgColor.opacity(0.85))
                        .shadow(color: bgColor.opacity(0.6), radius: 10, x: 0, y: 4)
                )
        }
        .scaleEffect(animateButtons ? 1 : 0.85)
        .animation(.spring(response: 0.5, dampingFraction: 0.6), value: animateButtons)
        .padding(.horizontal, 5)
    }
}

#Preview {
    StartMeetingView()
}
