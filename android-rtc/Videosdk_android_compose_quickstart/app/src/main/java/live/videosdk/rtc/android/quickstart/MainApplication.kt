package live.videosdk.rtc.android.quickstart

import android.app.Application
import live.videosdk.rtc.android.VideoSDK
import live.videosdk.rtc.android.quickstart.model.MeetingHelper

class MainApplication: Application() {

    val sampleToken = "" //Sample Token From VideoSDK
    val meetingHelper = MeetingHelper()
    override fun onCreate() {
        super.onCreate()
        VideoSDK.initialize(applicationContext)
    }
}