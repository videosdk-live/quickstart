package com.example.videosdk_android_modes_quickstart

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.videosdk_android_modes_quickstart.model.StreamViewModel
import com.example.videosdk_android_modes_quickstart.screens.JoinScreen
import com.example.videosdk_android_modes_quickstart.screens.StreamingScreen
import com.example.videosdk_android_modes_quickstart.ui.theme.Videosdk_android_modes_quickstartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID)
        checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)

        setContent {
            Videosdk_android_modes_quickstartTheme {
                MyApp(this) } }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode)
            return false
        }
        return true
    }

    companion object {
        private const val PERMISSION_REQ_ID = 22
        private val REQUESTED_PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
    }
}

@Composable
fun MyApp(context: Context) {
    NavigationGraph(context = context)
}

@Composable
fun NavigationGraph(navController: NavHostController = rememberNavController(), context: Context) {
    NavHost(navController = navController, startDestination = "join_screen") {
        composable("join_screen") { JoinScreen(navController, context) }
        composable("stream_screen?streamId={streamId}&mode={mode}") { backStackEntry ->
            val streamId = backStackEntry.arguments?.getString("streamId")
            val modeStr = backStackEntry.arguments?.getString("mode")
            val mode = StreamingMode.valueOf(modeStr ?: StreamingMode.SendAndReceive.name)

            streamId?.let {
                StreamingScreen(viewModel = StreamViewModel(), navController, streamId, mode, context)
            }
        }
    }
}