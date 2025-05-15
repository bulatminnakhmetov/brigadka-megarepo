package com.brigadka.app.data.notification

import com.brigadka.app.domain.notification.PushNotificationService
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSData
import platform.Foundation.NSMutableArray
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.componentsJoinedByString
import platform.Foundation.create
import platform.Foundation.getBytes
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications

class PushNotificationServiceIOS : PushNotificationService {
    override fun requestNotificationPermission() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val options = UNAuthorizationOptionAlert or
                      UNAuthorizationOptionBadge or
                      UNAuthorizationOptionSound

        center.requestAuthorizationWithOptions(options) { granted, error ->
            if (granted) {
                // Permission granted, register for remote notifications
                UIApplication.sharedApplication.registerForRemoteNotifications()
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    fun setToken(tokenData: NSData) {
        val tokenParts = NSMutableArray()
        val length = tokenData.length.toInt()
        val bytes = ByteArray(length)

        bytes.usePinned { pinned ->
            tokenData.getBytes(pinned.addressOf(0), length.toULong())
        }

        bytes.forEach {
            tokenParts.addObject(NSString.create(string = toHexByteString(it.toInt())))
        }

        val token = tokenParts.componentsJoinedByString("")
    }
}

fun toHexByteString(value: Int): String {
    val hex = value.toString(16) // Convert to hex string (base 16)
    return if (hex.length == 1) "0$hex" else hex
}