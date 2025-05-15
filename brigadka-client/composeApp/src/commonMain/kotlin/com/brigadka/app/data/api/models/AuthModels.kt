package com.brigadka.app.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val refresh_token: String,
    val user_id: Int
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequest(
    val refresh_token: String
)