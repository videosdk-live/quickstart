//
//  APIService.swift
//  iOSQuickStartDemo
//
//  Created by Parth Asodariya on 13/03/23.
//

import Foundation

//Replace with the token you generated from VideoSDK Dashboard
let TOKEN_STRING: String = "<Token Generated From Videosdk Dashboard>"

class APIService {

    class func createMeeting(token: String, completion: @escaping (Result<String, Error>) -> Void) {
        let url = URL(string: "https://api.videosdk.live/v2/rooms")!
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue(TOKEN_STRING, forHTTPHeaderField: "authorization")

        URLSession.shared.dataTask(with: request, completionHandler: { (data: Data?, response: URLResponse?, error: Error?) in
            DispatchQueue.main.async {
                if let data = data, let _ = String(data: data, encoding: .utf8)
                {
                    do{
                        let dataArray = try JSONDecoder().decode(RoomsStruct.self,from: data)
                        completion(.success(dataArray.roomID ?? ""))
                    } catch {
                        print("Error while creating a meeting: \(error)")
                        completion(.failure(error))
                    }
                }
            }
        }).resume()
    }
}

struct RoomsStruct: Codable {
    let createdAt, updatedAt, roomID: String?
    let links: Links?
    let id: String?

    enum CodingKeys: String, CodingKey {
        case createdAt, updatedAt
        case roomID = "roomId"
        case links, id
    }
}

// MARK: - Links
struct Links: Codable {
    let getRoom, getSession: String?

    enum CodingKeys: String, CodingKey {
        case getRoom = "get_room"
        case getSession = "get_session"
    }
}
