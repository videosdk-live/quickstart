//
//  HLSPlayer.swift
//  ios-hls
//
//  Created by Parth Asodariya on 05/12/25.
//

import SwiftUI
import AVKit

struct HLSVideoPlayer: View {
    let url: URL
    let width: CGFloat?
    let height: CGFloat?

    @State private var player: AVPlayer = AVPlayer()
    
    var body: some View {
        ZStack {
            VideoPlayer(player: player)
                .frame(width: width ?? 400, height: height ?? 400)
                .background(Color.black)
                .onAppear {
                    setupPlayer()
                }
                .onDisappear {
                    player.pause()
                }
            
            // Custom Overlay Controls
            HStack(spacing: 20) {
//                Button(action: { player.play() }) {
//                    Image(systemName: "play.fill")
//                        .foregroundColor(.white)
//                        .font(.system(size: 24))
//                        .padding(12)
//                        .background(Color.black.opacity(0.6))
//                        .clipShape(Circle())
//                }
//                
//                Button(action: { player.pause() }) {
//                    Image(systemName: "pause.fill")
//                        .foregroundColor(.white)
//                        .font(.system(size: 24))
//                        .padding(12)
//                        .background(Color.black.opacity(0.6))
//                        .clipShape(Circle())
//                }
            }
            .padding(.bottom, 20)
            .frame(maxHeight: .infinity, alignment: .bottom)
        }
    }

    private func setupPlayer() {
        let item = AVPlayerItem(url: url)
        player.replaceCurrentItem(with: item)
        player.play()
    }
}
