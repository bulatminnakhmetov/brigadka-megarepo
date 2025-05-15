// In composeApp/src/commonMain/kotlin/com/brigadka/app/data/repository/PushNotificationRepository.kt
package com.brigadka.app.data.api.push

import co.touchlab.kermit.Logger
import com.brigadka.app.data.api.BrigadkaApiServiceAuthorized
import com.brigadka.app.data.api.models.RegisterPushTokenRequest
import com.brigadka.app.data.api.models.UnregisterPushTokenRequest
import com.brigadka.app.getPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val logger = Logger.withTag("PushTokenRegistrator")

interface PushTokenRegistrator {
    fun registerPushToken(token: String)
    fun unregisterPushToken(token: String)
}

class PushTokenRegistratorImpl(
    private val coroutineScope: CoroutineScope,
    private val apiService: BrigadkaApiServiceAuthorized,
    private val deviceIdProvider: DeviceIdProvider,
) : PushTokenRegistrator {

    override fun registerPushToken(token: String) {
        val deviceId = deviceIdProvider.getDeviceId()
        val platform = getPlatform().name

        val request = RegisterPushTokenRequest(
            device_id = deviceId,
            platform = platform,
            token = token
        )

        coroutineScope.launch {
            try{
                apiService.registerPushToken(request)
                logger.d("Push token registered successfully: $token")
            } catch (e: Exception) {
                logger.e("Failed to register push token: $token", e)
                // TODO: handler error
            }
        }
    }

    override fun unregisterPushToken(token: String) {
        val request = UnregisterPushTokenRequest(
            token = token,
        )

        coroutineScope.launch {
            try {
                apiService.unregisterPushToken(request)
                logger.d("Push token unregistered successfully: $token")
            } catch (e: Exception) {
                logger.e("Failed to unregister push token: $token", e)
                // TODO: Handle error if needed
            }
        }
    }
}

// Device ID provider interface and implementation
interface DeviceIdProvider {
    fun getDeviceId(): String
}