package live.videosdk.rtc.android.quickstart.ui.theme.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import live.videosdk.rtc.android.quickstart.model.MeetingViewModel
import live.videosdk.rtc.android.quickstart.ui.theme.screens.JoinScreen
import live.videosdk.rtc.android.quickstart.ui.theme.screens.MeetingScreen


@Composable
fun NavigationGraph(navController: NavHostController = rememberNavController(),context: Context) {
    NavHost(navController = navController, startDestination = "join_screen") {
        composable("join_screen") {
            JoinScreen(navController,context)
        }
        composable("meeting_screen?meetingId={meetingId}") { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId") ?: "N/A"
            MeetingScreen(viewModel = MeetingViewModel(),navController, meetingId, context)
        }
    }
}