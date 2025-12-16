package live.videosdk.rtc.android.quickstart.core

import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import live.videosdk.rtc.android.quickstart.MainApplication
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.ApplicationContextProvider.getApplicationContext

object NetworkManager {
    fun createMeetingId(onMeetingIdCreated: (String) -> Unit) {

        val context = getApplicationContext() as MainApplication
        val token = context.sampleToken

        AndroidNetworking.post("https://api.videosdk.live/v2/rooms")
            .addHeaders("Authorization", token)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        val meetingId = response.getString("roomId")
                        onMeetingIdCreated(meetingId)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onError(anError: ANError) {
                    val errorMessage = if (anError.errorBody != null) {
                        try {
                            // Parse the JSON error response
                            val errorJson = JSONObject(anError.errorBody)
                            val statusCode = errorJson.getInt("statusCode")
                            val error = errorJson.getString("error")

                            // this will return String form ths if Block
                            "Error $statusCode: $error"

                        } catch (e: JSONException) {
                            anError.message ?: "Unknown error"
                        }
                    } else {
                        anError.message ?: "Unknown error"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    anError.printStackTrace()
                }
            })
    }
}
