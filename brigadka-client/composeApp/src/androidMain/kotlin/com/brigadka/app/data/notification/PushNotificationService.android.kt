package com.brigadka.app.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.brigadka.app.domain.notification.PushNotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PushNotificationServiceAndroid(
    private val context: Context,
) : PushNotificationService {

    override fun requestNotificationPermission() {
        // Only needed for Android 13+ (API level 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED

            // Permission is already handled externally (in activity)
            if (!permissionGranted) {
                // We'll handle the actual permission request in the MainActivity
                // This method will signal that we need to request permission
                _needsPermission.value = true
            }
        }
    }

    companion object {
        // Flow to signal that permission needs to be requested
        val _needsPermission = MutableStateFlow(false)
        val needsPermission: Flow<Boolean> = _needsPermission

        // Call this when permission is granted
        fun onPermissionGranted() {
            _needsPermission.value = false
        }
    }
}