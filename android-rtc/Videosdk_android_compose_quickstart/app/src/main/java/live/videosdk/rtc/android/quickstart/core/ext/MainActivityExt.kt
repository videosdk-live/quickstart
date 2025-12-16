package live.videosdk.rtc.android.quickstart.core.ext

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import live.videosdk.rtc.android.quickstart.MainActivity
import live.videosdk.rtc.android.quickstart.MainActivity.Companion.REQUESTED_PERMISSIONS

fun MainActivity.checkSelfPermission(permission: String, requestCode: Int): Boolean {
    if (ContextCompat.checkSelfPermission(this, permission) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(this,
            REQUESTED_PERMISSIONS, requestCode)
        return false
    }
    return true
}

