package com.brigadka.app

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.defaultComponentContext
import com.brigadka.app.data.notification.PushNotificationServiceAndroid
import com.brigadka.app.data.repository.PushTokenRepository
import com.brigadka.app.domain.notification.PushNotificationService
import com.brigadka.app.presentation.root.RootComponent
import com.brigadka.app.presentation.root.RootContent
import com.brigadka.app.domain.push.PushTokenRegistrationManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            PushNotificationServiceAndroid.onPermissionGranted()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val koin = GlobalContext.get()

        val rootComponent: RootComponent by koin.inject { parametersOf(defaultComponentContext()) }

        val pushTokenRepository: PushTokenRepository by koin.inject()

        // Setup notification permission observer
        lifecycleScope.launch {
            PushNotificationServiceAndroid.needsPermission.collectLatest { needsPermission ->
                if (needsPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    pushTokenRepository.saveToken(token)
                    Log.d("FCM", "Fetched token manually: $token")
                } else {
                    Log.w("FCM", "Fetching FCM token failed", task.exception)
                }
            }

        setContent {
            RootContent(rootComponent, modifier = Modifier.fillMaxSize())
        }

        setLightStatusBar(window)

        window.navigationBarColor = Color.parseColor("#fef7ff")

        koin.get<PushNotificationService>().requestNotificationPermission()
    }
}

fun setLightStatusBar(window: Window) {
    // Set a light background color for the status bar

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // API 30+
        window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
    } else {
        // TODO: check if it works
        // API 28 to 29

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.WHITE

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}