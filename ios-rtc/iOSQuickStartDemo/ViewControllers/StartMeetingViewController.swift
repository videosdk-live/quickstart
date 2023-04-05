//
//  StartMeetingViewController.swift
//  iOSQuickStartDemo
//
//  Created by Parth Asodariya on 17/03/23.
//

import Foundation
import UIKit

class StartMeetingViewController: UIViewController, UITextFieldDelegate {
    
    private var serverToken = ""
    
    @IBOutlet weak var btnCreateMeeting: UIButton!
    @IBOutlet weak var btnJoinMeeting: UIButton!
    @IBOutlet weak var txtMeetingId: UITextField!
    
    override func viewDidLoad() {
        txtMeetingId.delegate = self
        serverToken = TOKEN_STRING
        txtMeetingId.text = "pbow-6vec-vahn"
    }
    
    func joinMeeting() {
        txtMeetingId.resignFirstResponder()
        
        if !serverToken.isEmpty {
            DispatchQueue.main.async {
                self.dismiss(animated: true) {
                    self.performSegue(withIdentifier: "StartMeeting", sender: nil)
                }
            }
        } else {
            self.showAlert(title: "Auth Token Required", message: "Please provide auth token to start the meeting.")
        }
    }
    
    @IBAction func btnCreateMeetingTapped(_ sender: Any) {
        Utils.loaderShow(viewControler: self)
        joinRoom()
    }
    
    
    @IBAction func btnJoinMeetingTapped(_ sender: Any) {
        if((txtMeetingId.text ?? "").isEmpty){
            self.showAlert(title: "Meeting id Required", message: "Please provide meeting id to start the meeting.")
            txtMeetingId.resignFirstResponder()
        } else {
            joinMeeting()
        }
    }
    
    // MARK: - Navigation
    
    func joinRoom() {
        
        APIService.createMeeting(token: self.serverToken) { result in
            if case .success(let meetingId) = result {
                DispatchQueue.main.async {
                    self.txtMeetingId.text = meetingId
                    self.joinMeeting()
                }
            }
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
        guard let navigation = segue.destination as? UINavigationController,
              let meetingViewController = navigation.topViewController as? MeetingViewController else {
              return
          }
        
        meetingViewController.meetingData = MeetingData(
            token: serverToken,
            name: txtMeetingId.text ?? "Guest",
            meetingId: txtMeetingId.text ?? "",
            micEnabled: true,
            cameraEnabled: true
        )
    }
}

struct MeetingData {
    let token: String
    let name: String
    let meetingId: String
    let micEnabled: Bool
    let cameraEnabled: Bool
}


