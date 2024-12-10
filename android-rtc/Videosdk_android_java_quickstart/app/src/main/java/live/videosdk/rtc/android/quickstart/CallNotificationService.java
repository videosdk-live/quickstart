package live.videosdk.rtc.android.quickstart;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class CallNotificationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Show incoming call UI here, or use a Notification
        showIncomingCallUI(intent);  // Trigger UI to accept/reject call
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showIncomingCallUI(Intent intent) {
        // Create a full-screen incoming call UI, similar to a phone call
        Intent incomingCallIntent = new Intent(this, IncomingCallActivity.class);
        incomingCallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(incomingCallIntent);
    }
}
