package com.brigadka.app.presentation.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(component: RegisterComponent) {
    val state by component.state.collectAsState()
    RegisterScreen(
        state = state,
        onEmailChanged = component::onEmailChanged,
        onPasswordChanged = component::onPasswordChanged,
        onRegisterClick = component::onRegisterClick,
        onLoginClick = component.onLoginClick,
    )
}

@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        state = RegisterComponent.RegisterState(
            email = "",
            password = "",
            emailError = null,
            passwordError = null,
            error = null,
            isLoading = false
        ),
        onEmailChanged = {},
        onPasswordChanged = {},
        onRegisterClick = {},
        onLoginClick = {}
    )
}

@Composable
fun RegisterScreen(
    state: RegisterComponent.RegisterState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Создать аккаунт",
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
            onClick = onRegisterClick,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text("Создать аккаунт")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onLoginClick,
            enabled = !state.isLoading
        ) {
            Text("Уже есть аккаунт? Войдите")
        }
    }
}