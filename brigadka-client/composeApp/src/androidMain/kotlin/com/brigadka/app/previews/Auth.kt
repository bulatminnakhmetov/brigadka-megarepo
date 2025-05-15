package com.brigadka.app.previews

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.brigadka.app.presentation.AppTheme
import com.brigadka.app.presentation.auth.login.LoginScreenPreview
import com.brigadka.app.presentation.auth.register.RegisterScreen
import com.brigadka.app.presentation.auth.register.RegisterScreenPreview

@Preview
@Composable
fun LoginScreenPreviewPreview() {
    AppTheme {
        Surface {
            LoginScreenPreview()
        }
    }
}


@Preview
@Composable
fun RegisterScreenPreviewPreview() {
    AppTheme {
        Surface {
            RegisterScreenPreview()
        }
    }
}