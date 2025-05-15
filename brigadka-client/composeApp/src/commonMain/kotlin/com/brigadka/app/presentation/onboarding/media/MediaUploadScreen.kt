package com.brigadka.app.presentation.onboarding.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.brigadka.app.data.api.models.MediaItem
import com.brigadka.app.presentation.common.rememberFilePickerLauncher
import com.brigadka.app.presentation.profile.common.Avatar
import com.brigadka.app.presentation.profile.common.LoadableValue
import com.brigadka.app.presentation.profile.common.VideoSection

@Composable
fun MediaUploadScreen(component: MediaUploadComponent, onError : (String) -> Unit) {
    val profileData by component.profileData.subscribeAsState()

    val avatarPickerLauncher = rememberFilePickerLauncher(
        fileType = "image/*",
        onFilePicked = { bytes, fileName ->
            component.uploadAvatar(bytes, fileName)
        },
        onError = { error ->
            onError("Failed to pick avatar: $error")
        }
    )

    val videoPickerLauncher = rememberFilePickerLauncher(
        fileType = "video/*",
        onFilePicked = { bytes, fileName ->
            component.uploadVideo(bytes, fileName)
        },
        onError = { error ->
            onError("Failed to pick video: $error")
        }
    )

    MediaUploadScreen(
        avatar = profileData.avatar,
        videos = profileData.videos,
        pickImage = { avatarPickerLauncher.launch() },
        pickVideo = { videoPickerLauncher.launch() },
        removeAvatar = { component.removeAvatar() },
        removeVideo = { component.removeVideo(it) },
        onBack = { component.back() },
        onFinish = { component.finish() },
        onError = onError
    )
}

@Composable
fun MediaUploadScreenPreview() {
    val avatar = LoadableValue<MediaItem>()
    val videos = listOf<LoadableValue<MediaItem>>(
        LoadableValue(),
        LoadableValue(),
        LoadableValue()
    )

    MediaUploadScreen(
        avatar = avatar,
        videos = videos,
        pickImage = {},
        pickVideo = {},
        removeAvatar = {},
        removeVideo = {},
        onBack = {},
        onFinish = {},
        onError = {}
    )
}

@Composable
fun MediaUploadScreen(
    avatar : LoadableValue<MediaItem>,
    videos : List<LoadableValue<MediaItem>>,
    pickImage : () -> Unit,
    pickVideo : () -> Unit,
    removeAvatar: () -> Unit,
    removeVideo: (Int?) -> Unit,
    onBack : () -> Unit,
    onFinish : () -> Unit,
    onError: (String) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Добавь фото и видео",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Фото",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Avatar(
            mediaItem = avatar.value,
            isUploading = avatar.isLoading,
            onError = { error ->
                onError("Failed to get avatar: $error")
            },
            onClick = pickImage,
            onRemove = removeAvatar,
            modifier = Modifier.align(Alignment.CenterHorizontally).size(180.dp)
        )
        
        Spacer(modifier = Modifier.height(36.dp))

        // Avatar upload section
        Text(
            text = "Видео",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Загрузите видео с джемов или выступлений, чтобы другие импровизаторы могли увидеть вашу игру",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        VideoSection(
            videos = videos,
            pickVideo = pickVideo,
            removeVideo = removeVideo,
            onError = onError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilledTonalButton(
                onClick = { onBack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Назад")
            }

            Button(
                onClick = { onFinish() },
                modifier = Modifier.weight(1f),
            ) {
                Text("Завершить")
            }
        }
    }
}