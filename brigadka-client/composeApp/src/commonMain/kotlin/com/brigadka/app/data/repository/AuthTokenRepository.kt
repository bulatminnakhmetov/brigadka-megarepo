package com.brigadka.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

@Serializable
data class Token(
    val accessToken: String? = null,
    val refreshToken: String? = null,
)

interface AuthTokenRepository {
    val token: StateFlow<Token>
    suspend fun saveToken(token: Token)
    suspend fun clearToken()
}

class AuthTokenRepositoryImpl(
    private val settings: Settings
) : AuthTokenRepository {
    private val tokenKey: String = "auth_token"

    private val json = Json { ignoreUnknownKeys = true }

    private val _token = MutableStateFlow(getStoredToken())

    override val token: StateFlow<Token> = _token.asStateFlow()

    override suspend fun saveToken(token: Token) {
        val tokenJson = json.encodeToString(token)
        settings[tokenKey] = tokenJson
        _token.value = token
    }

    override suspend fun clearToken() {
        settings.remove(tokenKey)
        _token.value = Token()
    }

    private fun getStoredToken(): Token {
        val tokenJson = settings.getStringOrNull(tokenKey)
        return if (tokenJson != null) {
            try {
                json.decodeFromString<Token>(tokenJson)
            } catch (e: Exception) {
                Token()
            }
        } else {
            Token()
        }
    }
}