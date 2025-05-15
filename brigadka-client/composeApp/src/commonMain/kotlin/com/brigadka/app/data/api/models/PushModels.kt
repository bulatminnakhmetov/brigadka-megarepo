package com.brigadka.app.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterPushTokenRequest(
    val device_id: String,
    val platform: String,
    val token: String
)

@Serializable
data class UnregisterPushTokenRequest(
    val token: String
)

