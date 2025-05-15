package com.brigadka.app.data.repository

import android.content.Context
import android.provider.Settings
import com.brigadka.app.data.api.push.DeviceIdProvider
import java.util.UUID

class DeviceIdProviderAndroid(private val context: Context) : DeviceIdProvider {
    override fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) // TODO: fix
            ?: UUID.randomUUID().toString()
    }
}