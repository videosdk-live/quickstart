package live.videosdk.rtc.android.quickstart.feature.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import live.videosdk.rtc.android.quickstart.feature.meeting.MeetingViewModel
import live.videosdk.rtc.android.quickstart.feature.join.JoinScreen
import live.videosdk.rtc.android.quickstart.feature.meeting.MeetingScreen


@Composable
fun NavigationGraph(navController: NavHostController = rememberNavController(), meetingViewModel: MeetingViewModel) {
    NavHost(navController = navController, startDestination = "join_screen") {

        composable("join_screen") {
            JoinScreen(navController)
        }

        composable("meeting_screen?meetingId={meetingId}") { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId")
            meetingId?.let {
                MeetingScreen(
                    viewModel = meetingViewModel,
                    navController = navController,
                    meetingId = meetingId
                )
            }
        }
    }
}