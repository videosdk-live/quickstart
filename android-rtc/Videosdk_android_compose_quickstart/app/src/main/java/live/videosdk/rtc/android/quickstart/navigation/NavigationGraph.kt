package live.videosdk.rtc.android.quickstart.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import live.videosdk.rtc.android.quickstart.model.MeetingViewModel
import live.videosdk.rtc.android.quickstart.screens.JoinScreen
import live.videosdk.rtc.android.quickstart.screens.MeetingScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationGraph(navController: NavHostController = rememberNavController(),context: Context, meetingViewModel: MeetingViewModel) {
    NavHost(navController = navController, startDestination = "join_screen") {
        composable("join_screen") {
            JoinScreen(navController,context)
        }
        composable("meeting_screen?meetingId={meetingId}") { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId")
            meetingId?.let {
                MeetingScreen(viewModel = meetingViewModel,navController, meetingId, context)
            }
        }
    }
}