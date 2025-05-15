package com.brigadka.app.presentation.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.brigadka.app.presentation.onboarding.improv.ImprovInfoScreen
import com.brigadka.app.presentation.onboarding.basic.BasicInfoScreen
import com.brigadka.app.presentation.onboarding.media.MediaUploadScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingContent(component: OnboardingComponent) {
    val childStack by component.childStack.subscribeAsState()
    val currentIndex = when (childStack.active.configuration) {
        is OnboardingComponent.Config.BasicInfo -> 0
        is OnboardingComponent.Config.ImprovInfo -> 1
        is OnboardingComponent.Config.MediaUpload -> 2
    }
    val totalSteps = 3

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / totalSteps },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Children(
                stack = childStack,
                animation = stackAnimation(slide()),
            ) { child ->
                when (val instance = child.instance) {
                    is OnboardingComponent.Child.BasicInfo -> BasicInfoScreen(instance.component)
                    is OnboardingComponent.Child.ImprovInfo -> ImprovInfoScreen(instance.component)
                    is OnboardingComponent.Child.MediaUpload -> MediaUploadScreen(instance.component, onError = {
                        scope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    })
                }
            }
        }
    }
}