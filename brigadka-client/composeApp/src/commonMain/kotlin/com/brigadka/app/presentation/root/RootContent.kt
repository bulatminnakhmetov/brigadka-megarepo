package com.brigadka.app.presentation.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.brigadka.app.presentation.AppTheme
import com.brigadka.app.presentation.auth.AuthContent
import com.brigadka.app.presentation.loading.StartupLoadingScreen
import com.brigadka.app.presentation.main.MainContent
import com.brigadka.app.presentation.onboarding.OnboardingContent

@Composable
fun RootContent(component: RootComponent, modifier: Modifier) {
    val childStack by component.childStack.subscribeAsState()

    AppTheme {
        Column(modifier = modifier) {
            Children(
                stack = childStack,
                animation = stackAnimation(fade() + scale())
            ) { child ->
                when (val instance = child.instance) {
                    is RootComponent.Child.Auth -> AuthContent(instance.component)
                    is RootComponent.Child.Main -> MainContent(instance.component)
                    is RootComponent.Child.Onboarding -> OnboardingContent(instance.component)
                    is RootComponent.Child.Loading -> StartupLoadingScreen()
                }
            }
        }
    }
}