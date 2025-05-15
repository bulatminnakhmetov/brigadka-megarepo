package com.brigadka.app.presentation.auth.register

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.brigadka.app.common.coroutineScope
import com.brigadka.app.domain.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterComponent(
    componentContext: ComponentContext,
    val onLoginClick: () -> Unit,
    private val sessionManager: SessionManager
) : ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    init {
        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun onRegisterClick() {
        if (!validateInput()) return

        _state.update { it.copy(isLoading = true, error = null) }

        scope.launch {
            try {
                sessionManager.register(
                    email = _state.value.email,
                    password = _state.value.password
                )
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val emailError = when {
            _state.value.email.isBlank() -> "Email cannot be empty"
            !_state.value.email.contains("@") -> "Invalid email format"
            else -> null
        }

        val passwordError = when {
            _state.value.password.isBlank() -> "Password cannot be empty"
            _state.value.password.length < 8 -> "Password must be at least 8 characters"
            else -> null
        }

        _state.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError,
            )
        }

        return emailError == null && passwordError == null
    }

    data class RegisterState(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null
    )
}