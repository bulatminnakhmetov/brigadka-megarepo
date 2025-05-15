package com.brigadka.app.presentation.root

import MainComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.brigadka.app.common.coroutineScope
import com.brigadka.app.data.api.models.Profile
import com.brigadka.app.domain.session.SessionManager
import com.brigadka.app.data.repository.ProfileRepository
import com.brigadka.app.data.repository.UserDataRepository
import com.brigadka.app.domain.session.LoggingState
import com.brigadka.app.presentation.auth.AuthComponent
import com.brigadka.app.presentation.onboarding.OnboardingComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class RootComponent(
    componentContext: ComponentContext,
    private val sessionManager: SessionManager,
    private val profileRepository: ProfileRepository,
    private val createOnboardingComponent: (ComponentContext, () -> Unit) -> OnboardingComponent,
    private val createAuthComponent: (ComponentContext) -> AuthComponent,
    private val createMainComponent: (ComponentContext) -> MainComponent,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Configuration>()

    private val coroutineScope = coroutineScope()

    private val stack = childStack(
        source = navigation,
        initialConfiguration = Configuration.Auth,
        serializer = Configuration.serializer(),
        handleBackButton = true,
        childFactory = ::createChild
    )

    val childStack: Value<ChildStack<Configuration, Child>> = stack

    init {
        // Observe the current user ID and profile ID
        coroutineScope.launch {
            // TODO: if profile is loading, onboarding will be shown, but it should be loading screen
            sessionManager.loggingState.combine(profileRepository.currentUserProfile) { loggingState, profile ->
                var result: Configuration? = null
                if (loggingState is LoggingState.LoggedIn) {
                    result = if (profile.isLoading) {
                        Configuration.Loading
                    } else if (profile.value != null) {
                        Configuration.Main
                    } else {
                        Configuration.Onboarding
                    }
                } else if (loggingState is LoggingState.LoggedOut) {
                    result = Configuration.Auth
                }
                result
            }.collect { configuration ->
                if (configuration != null) {
                    withContext(Dispatchers.Main) {
                        navigation.replaceAll(configuration)
                    }
                }
            }
        }
    }

    private fun createChild(
        configuration: Configuration,
        componentContext: ComponentContext
    ): Child = when (configuration) {
        is Configuration.Auth -> Child.Auth(
            createAuthComponent(componentContext)
        )
        is Configuration.Main -> Child.Main(
            createMainComponent(componentContext)
        )
        is Configuration.Onboarding -> Child.Onboarding(
            createOnboardingComponent(
                componentContext,
                { navigation.replaceAll(Configuration.Main) },
            )
        )
        is Configuration.Loading -> Child.Loading
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object Auth : Configuration()

        @Serializable
        data object Main : Configuration()

        @Serializable
        data object Onboarding : Configuration()

        @Serializable
        data object Loading : Configuration()
    }

    sealed class Child {
        data class Auth(val component: AuthComponent) : Child()
        data class Main(val component: MainComponent) : Child()
        data class Onboarding(val component: OnboardingComponent) : Child()
        object Loading : Child()
    }
}