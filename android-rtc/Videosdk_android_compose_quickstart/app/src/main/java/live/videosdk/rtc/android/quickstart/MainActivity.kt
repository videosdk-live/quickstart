package live.videosdk.rtc.android.quickstart

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import live.videosdk.rtc.android.quickstart.model.MeetingViewModel
import live.videosdk.rtc.android.quickstart.navigation.NavigationGraph
import live.videosdk.rtc.android.quickstart.ui.theme.Videosdk_android_compose_quickstartTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MeetingViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MeetingViewModel::class.java]

        checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID)
        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)

        setContent {
            Videosdk_android_compose_quickstartTheme {
                MyApp(this, viewModel)
            }
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                REQUESTED_PERMISSIONS, requestCode)
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

@Composable
fun MyApp(context: Context, viewModel: MeetingViewModel) {
    NavigationGraph(context = context, meetingViewModel = viewModel)
}
