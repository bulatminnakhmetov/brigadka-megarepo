package com.brigadka.app.presentation.profile.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect interface VideoPlayerController {
    fun play()
    fun pause()
    fun seekTo(position: Long)
    fun release()
}

@Composable
expect fun rememberVideoPlayerController(url: String): VideoPlayerController

@Composable
expect fun VideoPlayer(
    url: String,
    controller: VideoPlayerController,
    modifier: Modifier = Modifier
)