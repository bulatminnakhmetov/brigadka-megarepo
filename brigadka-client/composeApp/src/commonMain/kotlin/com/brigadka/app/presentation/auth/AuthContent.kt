package com.brigadka.app.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.brigadka.app.presentation.auth.login.LoginScreen
import com.brigadka.app.presentation.auth.register.RegisterScreen

@Composable
fun AuthContent(component: AuthComponent) {
    val childStack by component.childStack.subscribeAsState()

    Children(
        stack = childStack,
        animation = stackAnimation(fade() + slide()),
    ) { child ->
        when (val instance = child.instance) {
            is AuthComponent.Child.Login -> LoginScreen(instance.component)
            is AuthComponent.Child.Register -> RegisterScreen(instance.component)
        }
    }
}