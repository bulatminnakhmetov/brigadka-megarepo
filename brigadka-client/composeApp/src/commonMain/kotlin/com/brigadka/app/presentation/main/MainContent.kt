package com.brigadka.app.presentation.main

import MainComponent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.brigadka.app.presentation.chat.conversation.ChatContent
import com.brigadka.app.presentation.chat.conversation.ChatTopBar
import com.brigadka.app.presentation.chat.list.ChatListContent
import com.brigadka.app.presentation.chat.list.ChatListTopBar
import com.brigadka.app.presentation.profile.view.ProfileViewScreen
import com.brigadka.app.presentation.profile.view.ProfileViewTopBar
import com.brigadka.app.presentation.search.SearchScreen
import com.brigadka.app.presentation.search.SearchTopBar
import kotlinx.coroutines.launch

@Composable
fun MainContent(component: MainComponent) {
    val childStack by component.childStack.subscribeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val activeChild = childStack.active.instance

    val topBar: @Composable () -> Unit = {
        when (activeChild) {
            is Child.Search -> SearchTopBar(state = activeChild.component.topBarState)
            is Child.Profile -> ProfileViewTopBar(state = activeChild.component.topBarState)
            is Child.ChatList -> ChatListTopBar()
            is Child.Chat -> {
                val topBarState by activeChild.component.topBarState.collectAsState()
                ChatTopBar(state = topBarState)
            }
            else -> {}
        }
    }

    MainContent(
        topBar = topBar,
        snackbarHostState = snackbarHostState,
        onProfileClick = component::navigateToProfile,
        onSearchClick = component::navigateToSearch,
        onChatListClick = component::navigateToChatList,
        content = {
            Children(
                stack = childStack,
                animation = stackAnimation(fade() + scale()),
            ) { child ->
                when (val instance = child.instance) {
                    is Child.Profile ->
                        ProfileViewScreen(instance.component) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(it)
                            }
                        }
                    is Child.Search ->
                        SearchScreen(instance.component)
                    is Child.ChatList ->
                        ChatListContent(instance.component)
                    is Child.Chat ->
                        ChatContent(instance.component)
                    else -> {}
                }
            }
        }
    )
}

@Composable
fun MainContent(
    topBar: @Composable () -> Unit,
    snackbarHostState: SnackbarHostState,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChatListClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = topBar,
        bottomBar = {
            BrigadkaBottomBar(
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick,
                onChatListClick = onChatListClick,
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
}

@Composable
fun BrigadkaBottomBar(
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onChatListClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = {
                selectedTab = 0
                onProfileClick()
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Профиль") }
        )

        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = {
                selectedTab = 1
                onSearchClick()
            },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Поиск") }
        )

        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = {
                selectedTab = 2
                onChatListClick()
            },
            icon = { Icon(Icons.Default.Email, contentDescription = "Chat") },
            label = { Text("Чат") }
        )
    }
}

@Composable
fun MainContentPreview() {
    val snackbarHostState = remember { SnackbarHostState() }

    MainContent(
        topBar = ({ Text("Top Bar") }),
        snackbarHostState = snackbarHostState,
        onProfileClick = {},
        onSearchClick = {},
        onChatListClick = {},
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                Text("Content goes here")
            }
        }
    )
}