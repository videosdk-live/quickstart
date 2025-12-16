package live.videosdk.rtc.android.quickstart.feature.navigation

import androidx.compose.runtime.Composable
import live.videosdk.rtc.android.quickstart.feature.meeting.MeetingViewModel

@Composable
fun MyApp(viewModel: MeetingViewModel) {
    NavigationGraph(meetingViewModel = viewModel)
}