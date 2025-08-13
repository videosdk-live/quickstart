package com.example.videosdk_android_modes_quickstart.model

import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONException
import org.json.JSONObject

object NetworkManager {
    fun createStreamId(token: String, onStreamIdCreated: (String) -> Unit) {
        AndroidNetworking.post("https://dev-api.videosdk.live/v2/rooms")
            .addHeaders("Authorization", token)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val streamId = response.getString("roomId")
                        onStreamIdCreated(streamId)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                override fun onError(anError: ANError) {
                    anError.printStackTrace()
                    Log.d("TAG", "onError: $anError")
                }
            })
    }
}