package live.videosdk.rtc.android.quickstart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONException
import org.json.JSONObject

class JoinActivity : AppCompatActivity() {
    //Replace with the token you generated from the VideoSDK Dashboard
    var sampleToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)
        val btnCreate = findViewById<Button>(R.id.btnCreateMeeting)
        val btnJoin = findViewById<Button>(R.id.btnJoinMeeting)
        val etMeetingId = findViewById<EditText>(R.id.etMeetingId)
        checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID)
        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)
        btnCreate.setOnClickListener { v: View? ->
            createMeeting(
                sampleToken
            )
        }
        btnJoin.setOnClickListener { v: View? ->
            val intent = Intent(this@JoinActivity, MeetingActivity::class.java)
            intent.putExtra("token", sampleToken)
            intent.putExtra("meetingId", etMeetingId.text.toString())
            startActivity(intent)
        }
    }

    private fun createMeeting(token: String) {
        // we will make an API call to VideoSDK Server to get a roomId
        AndroidNetworking.post("https://api.videosdk.live/v2/rooms")
            .addHeaders("Authorization", token) //we will pass the token in the Headers
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        // response will contain `roomId`
                        val meetingId = response.getString("roomId")

                        // starting the MeetingActivity with received roomId and our sampleToken
                        val intent = Intent(this@JoinActivity, MeetingActivity::class.java)
                        intent.putExtra("token", sampleToken)
                        intent.putExtra("meetingId", meetingId)
                        startActivity(intent)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onError(anError: ANError) {
                    anError.printStackTrace()
                    Toast.makeText(this@JoinActivity, anError.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode)
            return false
        }
        return true
    }

    companion object {
        private const val PERMISSION_REQ_ID = 22
        private val REQUESTED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
    }
}

