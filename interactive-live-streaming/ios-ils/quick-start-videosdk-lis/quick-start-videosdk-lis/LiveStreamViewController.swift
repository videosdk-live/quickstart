//
//  LiveStreamViewController.swift
//  quick-start-videosdk-lis
//
//  Created by Deep Bhupatkar on 31/12/24.
//
import Foundation
import VideoSDKRTC

struct RoomStruct: Codable {
    let roomID: String?

    enum CodingKeys: String, CodingKey {
        case roomID = "roomId"
    }
}

class LiveStreamViewController: ObservableObject {

    var token = "YOUR_TOKEN"
    var streamId: String = ""
    var name: String = ""
    
    @Published var meeting: Room? = nil
    @Published var localParticipantView: VideoView? = nil
    @Published var videoTrack: RTCVideoTrack?
    @Published var participants: [Participant] = []
    @Published var streamID: String = ""
   
    private var needsRefresh = false

    func initializeStream(streamId: String, userName: String, mode: Mode) {
        do {
            meeting = try VideoSDK.createRoom(
                roomId: streamId,
                token: token,
                participantName: userName,
                micEnabled: true,
                camEnabled: true,
                mode: mode // Pass the mode here
            )
        } catch {
            print("Setup failed: \(error)")
            return
        }
        // Add event listeners before joining: onRoomJoined can arrive before join() returns
        meeting?.addEventListener(self)
        // Join the meeting
        Task {
            do {
                try await meeting?.join()
            } catch {
                print("Join failed: \(error)")
            }
        }
    }
}

extension LiveStreamViewController: RoomEventListener {

    func onRoomJoined() {
        guard let localParticipant = self.meeting?.localParticipant else { return }
        // add to list
        participants.append(localParticipant)
        
        // add event listener
        localParticipant.addEventListener(self)
    }

    func onParticipantJoined(_ participant: Participant) {

        participants.append(participant)
        // add listener
        participant.addEventListener(self)

        Task { try? await participant.setQuality(.high) }
    }

    func onParticipantLeft(_ participant: Participant, reason: LeaveReason) {
        participants = participants.filter({ $0.id != participant.id })
        participants.removeAll()
    }

    func onRoomLeft(reason: LeaveReason) {
        meeting?.localParticipant.removeEventListener(self)
        meeting?.removeEventListener(self)
    }

    func onRoomStateChanged(roomState: RoomState) {
        switch roomState {

        case .DISCONNECTED:
            participants.removeAll()

        default:
            print("")
        }
    }
    
    func onParticipantModeChanged(participantId: String, mode: Mode) {
           DispatchQueue.main.async { [weak self] in
               if var currentParticipants = self?.participants {
                   // Find and update the participant whose mode changed
                   // the SDK has already updated the participant's mode; republish to trigger a refresh
                   if currentParticipants.contains(where: { $0.id == participantId }) {
                       self?.participants = currentParticipants
                   }
               }
           }
       }
}

extension LiveStreamViewController: ParticipantEventListener {
    func onStreamEnabled(_ stream: RTCStream, forParticipant participant: Participant) {
        if participant.isLocal   {
            if let track = stream.track as? RTCVideoTrack {
                DispatchQueue.main.async {
                    self.videoTrack = track
                }
            }
        } else {
            if let track = stream.track as? RTCVideoTrack {
                DispatchQueue.main.async {
                    self.videoTrack = track
                }
            }
        }
    }

    func onStreamDisabled(_ stream: RTCStream, forParticipant participant: Participant) {

        if participant.isLocal {
            if let _ = stream.track as? RTCVideoTrack {
                DispatchQueue.main.async {
                    self.videoTrack = nil
                }
            }
        } else {
            self.videoTrack = nil
        }
    }
}

extension LiveStreamViewController {
    // create a new stream id
    func joinRoom(userName: String ,mode : Mode) {

        let urlString = "https://api.videosdk.live/v2/rooms"
        let session = URLSession.shared
        let url = URL(string: urlString)!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue(self.token, forHTTPHeaderField: "Authorization")

        session.dataTask(with: request, completionHandler: { (data: Data?, response: URLResponse?, error: Error?) in

            if let data = data, let utf8Text = String(data: data, encoding: .utf8)
            {
                print("UTF =>=>\(utf8Text)") // original server data as UTF8 string
                do{
                    let dataArray = try JSONDecoder().decode(RoomStruct.self,from: data)
                    DispatchQueue.main.async {
                        print(dataArray.roomID)
                        self.streamID = dataArray.roomID!
                        self.joinStream(streamId: dataArray.roomID!, userName: userName, mode: mode)
                    }
                    print(dataArray)
                } catch {
                    print(error)
                }
            }
        }
        ).resume()
    }

    // initialise a stream with give stream id (either new or existing)
    func joinStream(streamId: String, userName: String, mode: Mode) {
        if !token.isEmpty {
            self.streamID = streamId
            self.initializeStream(streamId: streamId, userName: userName, mode: mode)
        } else {
            print("Auth token required")
        }
    }
}
