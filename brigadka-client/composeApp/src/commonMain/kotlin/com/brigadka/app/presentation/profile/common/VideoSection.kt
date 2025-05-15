package com.brigadka.app.presentation.profile.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.brigadka.app.data.api.models.MediaItem

@Composable
fun VideoSection(
    videos: List<LoadableValue<MediaItem>>,
    pickVideo: (() -> Unit)? = null,
    removeVideo: ((Int?) -> Unit)? = null,
    onError: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyRow (
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(videos.size) { index ->
            val video = videos[index]
            VideoThumbnail(
                mediaItem = video.value,
                isUploading = video.isLoading,
                onRemove = removeVideo?.let {
                    { it(video.value?.id) }
                },
                onError = { error ->
                    onError("Failed to get video thumbnail: $error")
                },
                modifier = Modifier
                    .width(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = !video.isLoading) {
                        // TODO: open video
                    }
            )
        }
    }

    if (pickVideo != null) {
        // upload button outlined button
        OutlinedButton(
            onClick = { pickVideo() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (videos.any { it.isLoading }) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upload Video",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Загрузить")
            }
        }
    }
}