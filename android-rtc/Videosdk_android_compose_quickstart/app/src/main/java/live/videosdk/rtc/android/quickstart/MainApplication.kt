package live.videosdk.rtc.android.quickstart

import android.app.Application
import io.github.crow_misia.mediasoup.Logger
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.lib.tracing.LogLevel

class MainApplication : Application() {

    // Generate a token at https://app.videosdk.live/api-keys and paste it here.
    val sampleToken = "YOUR_TOKEN"

    override fun onCreate() {
        super.onCreate()
        VideoSDK.setLogLevel(LogLevel.ALL)
        VideoSDK.initialize(applicationContext)
    }
}
