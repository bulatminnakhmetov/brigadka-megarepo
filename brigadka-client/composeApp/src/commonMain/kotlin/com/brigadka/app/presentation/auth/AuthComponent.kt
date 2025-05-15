package com.brigadka.app.presentation.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.brigadka.app.domain.session.SessionManager
import com.brigadka.app.presentation.auth.login.LoginComponent
import com.brigadka.app.presentation.auth.register.RegisterComponent
import kotlinx.serialization.Serializable

class AuthComponent(
    componentContext: ComponentContext,
    private val sessionManager: SessionManager,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Configuration>()

    private val stack = childStack(
        source = navigation,
        serializer = Configuration.serializer(),
        initialConfiguration = Configuration.Login,
        handleBackButton = true,
        childFactory = ::createChild
    )

    val childStack: Value<ChildStack<*, Child>> = stack

    private fun createChild(
        configuration: Configuration,
        componentContext: ComponentContext
    ): Child = when (configuration) {
        is Configuration.Login -> Child.Login(
            LoginComponent(
                componentContext = componentContext,
                navigateToRegister = { navigateTo(Configuration.Register) },
                sessionManager = sessionManager
            )
        )
        is Configuration.Register -> Child.Register(
            RegisterComponent(
                componentContext = componentContext,
                onLoginClick = { navigateTo(Configuration.Login) },
                sessionManager = sessionManager
            )
        )
    }

    // TODO: same fuctionality in MainComponent, consider moving to base class
    fun navigateTo(screen: Configuration) {
        val stackItems = childStack.value.items
        val existingIndex = stackItems.indexOfFirst { it.configuration == screen }

        if (childStack.value.active.configuration == screen) {
            // Already on this screen, do nothing
            return
        }

        if (existingIndex != -1) {
            // Screen is in stack, bring to front
            navigation.bringToFront(screen)
        } else {
            // Not in stack, push it
            navigation.pushNew(screen)
        }
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        object Login : Configuration()

        @Serializable
        object Register : Configuration()
    }

    sealed class Child {
        data class Login(val component: LoginComponent) : Child()
        data class Register(val component: RegisterComponent) : Child()
    }
}