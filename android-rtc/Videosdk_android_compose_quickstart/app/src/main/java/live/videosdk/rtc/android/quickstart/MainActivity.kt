package live.videosdk.rtc.android.quickstart

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import live.videosdk.rtc.android.quickstart.core.ext.checkSelfPermission
import live.videosdk.rtc.android.quickstart.feature.meeting.MeetingViewModel
import live.videosdk.rtc.android.quickstart.feature.navigation.MyApp
import live.videosdk.rtc.android.quickstart.feature.navigation.NavigationGraph
import live.videosdk.rtc.android.quickstart.ui.theme.Videosdk_android_compose_quickstartTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val PERMISSION_REQ_ID = 22
        val REQUESTED_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
    }

    private val viewModel: MeetingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID)
        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)

        setContent {
            Videosdk_android_compose_quickstartTheme {
                MyApp(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.meeting?.enableWebcam()
        viewModel.meeting?.unmuteMic()
    }

    override fun onStop() {
        super.onStop()
        viewModel.meeting?.disableWebcam()
        viewModel.meeting?.muteMic()
        viewModel.participants.clear()
    }


}
