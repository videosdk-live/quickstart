//
//  SettingView.swift
//  ios-hls
//
//  Created by Parth Asodariya on 23/12/25.
//

import SwiftUI
import VideoSDKRTC

struct SettingView: View {
    var meeting: Meeting? = nil
    @Environment(\.dismiss) private var dismiss
    @State private var hexInput: String = ""
    @State private var isPublishingColor: Bool = false
    @State private var colorErrorMessage: String?
    @State private var viewerMessage: String = ""
    @State private var isPublishingMessage: Bool = false
    @State private var messageErrorMessage: String?
    
    var body: some View {
        VStack(spacing: 20) {
            // Header
            HStack {
                Spacer()
                Text("Settings")
                    .font(.title2)
                    .bold()
                Spacer()
            }
            .padding(.top, 8)
            
            // Background Color Section
            VStack(alignment: .leading, spacing: 5) {
                Text("Background Color (Hex)")
                    .font(.headline)
                
                HStack(spacing: 10) {
                    TextField("#RRGGBB or #RRGGBBAA", text: $hexInput)
                        .textInputAutocapitalization(.never)
                        .disableAutocorrection(true)
                        .keyboardType(.asciiCapable)
                        .padding(10)
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(8)
                    
                    // Color preview swatch
                    RoundedRectangle(cornerRadius: 6)
                        .fill(parsedColor ?? Color.clear)
                        .frame(width: 36, height: 36)
                        .overlay(
                            RoundedRectangle(cornerRadius: 6)
                                .stroke(Color.gray.opacity(0.4), lineWidth: 1)
                        )
                }
                
                if let error = colorErrorMessage {
                    Text(error)
                        .font(.footnote)
                        .foregroundColor(.red)
                }
                
                Button {
                    Task { await setBackgroundColor() }
                } label: {
                    HStack {
                        if isPublishingColor {
                            ProgressView()
                                .progressViewStyle(.circular)
                        }
                        Text("Set BG Color")
                            .font(.headline)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
                }
                .disabled(isPublishingColor)
            }
            .padding(.horizontal)
            
            Divider().opacity(0.3)
            
            // Notify Viewers Section
            VStack(alignment: .leading, spacing: 5) {
                Text("Notify Viewers")
                    .font(.headline)
                
                TextField("Enter message for viewers", text: $viewerMessage, axis: .vertical)
                    .textInputAutocapitalization(.sentences)
                    .disableAutocorrection(false)
                    .padding(10)
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(8)
                
                if let error = messageErrorMessage {
                    Text(error)
                        .font(.footnote)
                        .foregroundColor(.red)
                }
                
                Button {
                    Task { await notifyViewers() }
                } label: {
                    HStack {
                        if isPublishingMessage {
                            ProgressView()
                                .progressViewStyle(.circular)
                        }
                        Text("Notify Viewers")
                            .font(.headline)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.orange)
                    .foregroundColor(.white)
                    .cornerRadius(10)
                }
                .disabled(isPublishingMessage)
            }
            .padding(.horizontal)
            
            Spacer()
        }
        .onAppear {
            colorErrorMessage = nil
            messageErrorMessage = nil
        }
    }
    
    // MARK: - Helpers
    
    private var normalizedHex: String? {
        let trimmed = hexInput.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return nil }
        return trimmed.hasPrefix("#") ? trimmed : "#\(trimmed)"
    }
    
    private var parsedColor: Color? {
        guard let hex = normalizedHex, let uiColor = UIColor(fromHex: hex) else { return nil }
        return Color(uiColor)
    }
    
    private func setBackgroundColor() async {
        colorErrorMessage = nil
        
        guard let meeting = meeting else {
            colorErrorMessage = "Meeting not available."
            return
        }
        guard let hex = normalizedHex, UIColor(fromHex: hex) != nil else {
            colorErrorMessage = "Enter a valid hex color like #FF0000 or #RRGGBBAA."
            return
        }
        
        isPublishingColor = true
        defer { isPublishingColor = false }
        
        do {
            try await meeting.pubsub.publish(topic: "CHANGE_BACKGROUND", message: hex)
        } catch {
            await MainActor.run {
                colorErrorMessage = "Failed to publish color: \(error.localizedDescription)"
            }
        }
    }
    
    private func notifyViewers() async {
        messageErrorMessage = nil
        
        guard let meeting = meeting else {
            messageErrorMessage = "Meeting not available."
            return
        }
        let message = viewerMessage.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !message.isEmpty else {
            messageErrorMessage = "Message cannot be empty."
            return
        }
        
        isPublishingMessage = true
        defer { isPublishingMessage = false }
        
        do {
            try await meeting.pubsub.publish(topic: "VIEWER_MESSAGE", message: message)
            await MainActor.run {
                viewerMessage = ""
            }
        } catch {
            await MainActor.run {
                messageErrorMessage = "Failed to notify viewers: \(error.localizedDescription)"
            }
        }
    }
}

// MARK: - UIColor Hex initializer
private extension UIColor {
    convenience init?(fromHex hexString: String) {
        var hex = hexString.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
        if hex.hasPrefix("#") { hex.removeFirst() }
        
        let chars = Array(hex)
        let r, g, b, a: CGFloat
        
        func component(_ start: Int, _ length: Int) -> CGFloat? {
            let end = start + length
            guard start >= 0, end <= chars.count else { return nil }
            let slice = String(chars[start..<end])
            var value: UInt64 = 0
            guard Scanner(string: slice).scanHexInt64(&value) else { return nil }
            return length == 1 ? CGFloat(value) / 15.0 : CGFloat(value) / 255.0
        }
        
        switch chars.count {
        case 3: // RGB (12-bit)
            guard let rr = component(0, 1),
                  let gg = component(1, 1),
                  let bb = component(2, 1) else { return nil }
            r = rr; g = gg; b = bb; a = 1.0
        case 4: // RGBA (16-bit)
            guard let rr = component(0, 1),
                  let gg = component(1, 1),
                  let bb = component(2, 1),
                  let aa = component(3, 1) else { return nil }
            r = rr; g = gg; b = bb; a = aa
        case 6: // RRGGBB (24-bit)
            guard let rr = component(0, 2),
                  let gg = component(2, 2),
                  let bb = component(4, 2) else { return nil }
            r = rr; g = gg; b = bb; a = 1.0
        case 8: // RRGGBBAA (32-bit)
            guard let rr = component(0, 2),
                  let gg = component(2, 2),
                  let bb = component(4, 2),
                  let aa = component(6, 2) else { return nil }
            r = rr; g = gg; b = bb; a = aa
        default:
            return nil
        }
        
        self.init(red: r, green: g, blue: b, alpha: a)
    }
}
