package com.brigadka.app.di

import com.brigadka.app.domain.notification.PushNotificationService
import com.brigadka.app.data.notification.PushNotificationServiceIOS
import com.brigadka.app.data.api.push.DeviceIdProvider
import com.brigadka.app.data.repository.DeviceIdProviderIOS
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

actual val platformModule: Module
    get() = module {
        single<Settings> {
            NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
        }
        single<DeviceIdProvider> {
            DeviceIdProviderIOS()
        }
        single<PushNotificationService> {
            PushNotificationServiceIOS()
        }
    }