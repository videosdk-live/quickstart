package com.example.videosdk_android_modes_quickstart

import android.app.Application
import live.videosdk.rtc.android.VideoSDK

class MainApplication: Application() {


    val sampleToken = "SAMPLE TOKEN" //Sample Token From VideoSDK

    override fun onCreate() {
        super.onCreate()
        VideoSDK.initialize(applicationContext)
    }
}
