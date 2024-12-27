package live.videosdk.rtc.android.quickstart

import android.app.Application
import live.videosdk.rtc.android.VideoSDK

class MainApplication: Application() {

    val sampleToken = "YOUR_TOKEN" //Sample Token From VideoSDK
    override fun onCreate() {
        super.onCreate()
        VideoSDK.initialize(applicationContext)
    }
}