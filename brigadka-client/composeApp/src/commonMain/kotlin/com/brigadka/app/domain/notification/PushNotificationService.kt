package com.brigadka.app.domain.notification

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface PushNotificationService {
    fun requestNotificationPermission()
}