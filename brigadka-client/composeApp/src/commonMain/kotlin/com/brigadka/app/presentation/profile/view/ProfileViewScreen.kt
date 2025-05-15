package com.brigadka.app.presentation.profile.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.brigadka.app.data.api.models.MediaItem
import com.brigadka.app.presentation.profile.common.Avatar
import com.brigadka.app.presentation.profile.common.LoadableValue
import com.brigadka.app.data.repository.ProfileView
import com.brigadka.app.presentation.common.getYearsPostfix
import com.brigadka.app.presentation.profile.common.VideoSection

@Composable
fun ProfileViewScreen(component: ProfileViewComponent, onError: (String) -> Unit) {
    val profileViewState by component.profileView.subscribeAsState()
    ProfileViewScreen(
        profileView = profileViewState.value,
        isLoading = profileViewState.isLoading,
        onError = onError,
        onEditProfile = component.onEditProfile,
        onContactClick = component::onContactClick,
        isContactable = component.isContactable,
        isEditable = component.isEditable
    )
}

@Composable
fun HomeProfileViewScreenPreview() {
    val profileView = ProfileView(
        userID = 1,
        fullName = "John Doe",
        age = 30,
        genderLabel = "Мужчина",
        cityLabel = "Москва",
        bio = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        goalLabel = "Хобби",
        improvStylesLabels = listOf("Длинная форма", "Реп"),
        lookingForTeam = true,
        avatar = MediaItem(
            id = 1,
            url = "https://example.com/avatar.jpg",
            thumbnail_url = "https://example.com/avatar_thumbnail.jpg"
        ),
        videos = listOf(
            MediaItem(id = 0, url = "https://example.com/video1.mp4", thumbnail_url = "https://example.com/video"),
            MediaItem(id = 1, url = "https://example.com/video1.mp4", thumbnail_url = "https://example.com/video"),
            MediaItem(id = 2, url = "https://example.com/video1.mp4", thumbnail_url = "https://example.com/video")
        )
    )
    ProfileViewScreen(
        profileView,
        isLoading = false,
        onError = {},
        onEditProfile = {},
        onContactClick = {},
        isContactable = false,
        isEditable = true
    )
}


@Composable
fun OtherProfileViewScreenPreview() {
    val profileView = ProfileView(
        userID = 1,
        fullName = "John Doe",
        age = 30,
        genderLabel = "Мужчина",
        cityLabel = "Москва",
        bio = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        goalLabel = "Хобби",
        improvStylesLabels = listOf("Длинная форма", "Реп"),
        lookingForTeam = true,
        avatar = MediaItem(
            id = 1,
            url = "https://example.com/avatar.jpg",
            thumbnail_url = "https://example.com/avatar_thumbnail.jpg"
        ),
        videos = listOf(
            MediaItem(id = 0, url = "https://example.com/video1.mp4", thumbnail_url = "https://example.com/video"),
            MediaItem(id = 1, url = "https://example.com/video1.mp4", thumbnail_url = "https://example.com/video"),
            MediaItem(id = 2, url = "https://example.com/video1.mp4", thumbnail_url = "https://example.com/video")
        )
    )
    ProfileViewScreen(
        profileView,
        isLoading = false,
        onError = {},
        onContactClick = {},
        onEditProfile = {},
        isContactable = true,
        isEditable = false
    )
}

@Composable
fun ProfileViewScreen(
    profileView: ProfileView?,
    isLoading: Boolean,
    onError: (String) -> Unit,
    onEditProfile: () -> Unit,
    onContactClick: () -> Unit,
    isEditable: Boolean,
    isContactable: Boolean,
) {

    val scrollState = rememberScrollState()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (profileView == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Profile not found")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile header with avatar
        Avatar(
            mediaItem = profileView.avatar,
            isUploading = false,
            onError = onError,
            modifier = Modifier.padding(vertical = 16.dp).size(150.dp)
        )

        // Name and basic info
        Text(
            text = profileView.fullName,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Additional profile info (city, age)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // TODO: лейблы в дб должны быть парень/девушка
            profileView.genderLabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            profileView.age?.let {
                Text(
                    text = " • ${profileView.age} ${getYearsPostfix(profileView.age)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            profileView.cityLabel?.let {
                Text(
                    text = " • $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isContactable) {
            Spacer(modifier = Modifier.height(24.dp))

            // Contact button
            if (onContactClick != null) {
                Button(
                    onClick = { onContactClick() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Написать", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bio section
        if (profileView.bio.isNotEmpty()) {
            SectionTitle(title = "О себе")
            Text(
                text = profileView.bio,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }



        // Videos section
        if (profileView.videos.isNotEmpty()) {
            SectionTitle(title = "Видео")
            Spacer(modifier = Modifier.height(16.dp))
            VideoSection(
                videos = profileView.videos.map { LoadableValue(value = it) },
                onError = onError,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Improv details section
        SectionTitle(title = "Импровизация")

        // Goal
        profileView.goalLabel?.let {
            ProfileTextField("Цель", it)
        }

        ProfileTextField("Ищу команду", if (profileView.lookingForTeam) "Да" else "Нет")

        // Improv styles
        if (profileView.improvStylesLabels.isNotEmpty()) {
            ProfileTextField("Стили", profileView.improvStylesLabels.joinToString(", "))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable fun ProfileTextField(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = "$label: $value")
    }
}

// Add to ProfileViewScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewTopBar(state: ProfileViewTopBarState) {
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = { Text("Профиль") },
        navigationIcon = {
            if (!state.isCurrentUser) {
                IconButton(onClick = state.onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (state.isCurrentUser) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Profile") },
                        onClick = {
                            showMenu = false
                            state.onEditProfile()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            showMenu = false
                            state.onLogout()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    )
                }
            }
        }
    )
}