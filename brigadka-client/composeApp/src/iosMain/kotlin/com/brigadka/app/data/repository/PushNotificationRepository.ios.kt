package com.brigadka.app.data.repository

import com.brigadka.app.data.api.push.DeviceIdProvider
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID

class DeviceIdProviderIOS : DeviceIdProvider {
    companion object {
        private const val DEVICE_ID_KEY = "brigadka_device_id"
    }

    override fun getDeviceId(): String {
        val userDefaults = NSUserDefaults.standardUserDefaults
        var deviceId = userDefaults.stringForKey(DEVICE_ID_KEY)

        if (deviceId == null) {
            deviceId = NSUUID.UUID().UUIDString
            userDefaults.setObject(deviceId, DEVICE_ID_KEY)
            userDefaults.synchronize()
        }

        return deviceId
    }
}