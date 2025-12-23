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
        }
    }

    private func setupPlayer() {
        let item = AVPlayerItem(url: url)
        player.replaceCurrentItem(with: item)
        player.play()
    }
}
