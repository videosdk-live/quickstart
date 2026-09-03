package live.videosdk.rtc.android.quickstart

import android.app.Application
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.lib.tracing.LogLevel

class MainApplication : Application() {

    // Generate a token at https://app.videosdk.live/api-keys and paste it here.
    val sampleToken = "YOUR_TOKEN"

    override fun onCreate() {
        super.onCreate()
        // SDK logs stay silent until a level is set; DEBUG shows what the SDK does while you build.
        VideoSDK.setLogLevel(LogLevel.DEBUG)
        VideoSDK.initialize(applicationContext)
    }
}
