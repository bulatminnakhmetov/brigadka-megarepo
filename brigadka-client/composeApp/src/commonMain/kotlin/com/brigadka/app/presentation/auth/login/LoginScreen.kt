package com.brigadka.app.presentation.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(component: LoginComponent) {
    val state by component.state.collectAsState()
    LoginScreen(
        state = state,
        onEmailChanged = component::onEmailChanged,
        onPasswordChanged = component::onPasswordChanged,
        onLoginClick = component::onLoginClick,
        onRegisterClick = component::onRegisterClick
    )
}

@Composable
fun LoginScreenPreview() {
    LoginScreen(
        state = LoginComponent.LoginState(
            email = "",
            password = "",
            emailError = null,
            passwordError = null,
            error = null,
            isLoading = false
        ),
        onEmailChanged = {},
        onPasswordChanged = {},
        onLoginClick = {},
        onRegisterClick = {}
    )
}

@Composable
fun LoginScreen(
    state: LoginComponent.LoginState,
    onEmailChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Добро пожаловать",
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            label = { Text("Email") },
            isError = state.emailError != null,
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium  // TODO: make consistent with other text fields
        )

        state.emailError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style =MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth()
                    .align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            label = { Text("Пароль") },
            isError = state.passwordError != null,
            enabled = !state.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium // TODO: make consistent with other text fields
        )

        state.passwordError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style =MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth()
                    .align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = onLoginClick,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text("Войти")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onRegisterClick,
            enabled = !state.isLoading,
        ) {
            Text("Нет аккаунта? Зарегистрируйтесь")
        }
    }
}