package com.brigadka.app.domain.session

import com.brigadka.app.data.api.BrigadkaApiServiceUnauthorized
import com.brigadka.app.data.api.models.LoginRequest
import com.brigadka.app.data.api.models.RegisterRequest
import com.brigadka.app.data.api.push.PushTokenRegistrator
import com.brigadka.app.data.repository.AuthTokenRepository
import com.brigadka.app.data.repository.PushTokenRepository
import com.brigadka.app.data.repository.Token
import com.brigadka.app.data.repository.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


sealed class LoggingState {
    object LoggingIn : LoggingState()
    object LoggedIn : LoggingState()
    object LoggedOut : LoggingState()
}

typealias LogoutObserver = suspend () -> Unit

interface SessionManager {
    val loggingState: StateFlow<LoggingState>

    fun registerLogoutObserver(observer: LogoutObserver)
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(
        email: String,
        password: String,
    ): AuthResult
    suspend fun logout()
}

class SessionManagerImpl(
    private val scope: CoroutineScope,
    private val apiService: BrigadkaApiServiceUnauthorized,
    private val authTokenRepository: AuthTokenRepository,
    private val userDataRepository: UserDataRepository,
) : SessionManager {

    private val _loggingState: MutableStateFlow<LoggingState> = MutableStateFlow(
        if (userDataRepository.isLoggedIn) {
            LoggingState.LoggedIn
        } else {
            LoggingState.LoggedOut
        }
    )

    override val loggingState: StateFlow<LoggingState> = _loggingState

    private val logoutObservers = mutableListOf<LogoutObserver>()

    override fun registerLogoutObserver(observer: LogoutObserver) {
        logoutObservers += observer
    }

    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            _loggingState.value = LoggingState.LoggingIn
            val response = apiService.login(LoginRequest(email, password))
            val token = Token(
                accessToken = response.token,
                refreshToken = response.refresh_token
            )
            authTokenRepository.saveToken(token)
            userDataRepository.setCurrentUserId(response.user_id)
            _loggingState.value = LoggingState.LoggedIn

            AuthResult(
                success = true,
                token = response.token,
                userId = response.user_id
            )
        } catch (e: Exception) {
            AuthResult(
                success = false,
                error = e.message ?: "Login failed"
            )
        }
    }

    // Similar update for register method
    override suspend fun register(
        email: String,
        password: String,
    ): AuthResult {
        return try {
            val response = apiService.register(RegisterRequest(email = email, password = password))
            val token = Token(
                accessToken = response.token,
                refreshToken = response.refresh_token
            )
            authTokenRepository.saveToken(token)
            userDataRepository.setCurrentUserId(response.user_id)
            _loggingState.value = LoggingState.LoggedIn

            AuthResult(
                success = true,
                token = response.token,
                userId = response.user_id
            )
        } catch (e: Exception) {
            AuthResult(
                success = false,
                error = e.message ?: "Registration failed"
            )
        }
    }

    // Also update logout to unregister the push token
    override suspend fun logout() {
        authTokenRepository.clearToken()
        userDataRepository.clearCurrentUserId()


        scope.launch {
            // Call all observers in parallel
            logoutObservers.map { observer ->
                async { observer.invoke() }
            }.awaitAll()

            // Now it's safe to clear token and session
            authTokenRepository.clearToken()
            userDataRepository.clearCurrentUserId()
            _loggingState.value = LoggingState.LoggedOut
        }
    }
}

data class AuthResult(
    val success: Boolean,
    val token: String? = null,
    val userId: Int? = null,
    val error: String? = null
)