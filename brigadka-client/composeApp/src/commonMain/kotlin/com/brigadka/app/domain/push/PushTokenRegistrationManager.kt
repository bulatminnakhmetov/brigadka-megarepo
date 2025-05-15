package com.brigadka.app.domain.push

import co.touchlab.kermit.Logger
import com.brigadka.app.data.api.push.PushTokenRegistrator
import com.brigadka.app.data.repository.PushTokenRepository
import com.brigadka.app.domain.session.LoggingState
import com.brigadka.app.domain.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private val logger = Logger.withTag("PushTokenRegistrationManager")

interface PushTokenRegistrationManager

class PushTokenRegistrationManagerImpl(
    coroutineScope: CoroutineScope,
    private val sessionManager: SessionManager,
    private val pushTokenRepository: PushTokenRepository,
    private val pushTokenRegistrator: PushTokenRegistrator,
) : PushTokenRegistrationManager {

    init {
        coroutineScope.launch {
            sessionManager.loggingState.combine(pushTokenRepository.token) { loggingState, token -> loggingState to token }
                .collect { (loggingState, token) ->
                    logger.d("Received token: $token, loggingState: $loggingState")

                    if (token != null && loggingState is LoggingState.LoggedIn) {
                        pushTokenRegistrator.registerPushToken(token)
                    }
                }
        }
        sessionManager.registerLogoutObserver {
            logger.d("Unregistering push token")
            val token = pushTokenRepository.token.value
            if (token != null) {
                pushTokenRegistrator.unregisterPushToken(token)
            }
        }
    }
}