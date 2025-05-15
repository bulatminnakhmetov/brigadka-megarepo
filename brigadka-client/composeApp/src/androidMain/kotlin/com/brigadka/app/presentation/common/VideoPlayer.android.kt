// composeApp/src/androidMain/kotlin/com/brigadka/app/presentation/profile/common/VideoPlayer.android.kt
package com.brigadka.app.presentation.profile.common

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.core.net.toUri

actual interface VideoPlayerController {
    actual fun play()
    actual fun pause()
    actual fun seekTo(position: Long)
    actual fun release()
}

class AndroidVideoPlayerController(val player: ExoPlayer) : VideoPlayerController {
    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun seekTo(position: Long) {
        player.seekTo(position)
    }

    override fun release() {
        player.release()
    }
}

@Composable
actual fun rememberVideoPlayerController(url: String): VideoPlayerController {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(url.toUri())
            setMediaItem(mediaItem)
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    return remember { AndroidVideoPlayerController(exoPlayer) }
}

@Composable
actual fun VideoPlayer(
    url: String,
    controller: VideoPlayerController,
    modifier: Modifier
) {
    val context = LocalContext.current
    val androidController = controller as AndroidVideoPlayerController

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = androidController.player
                useController = true // Using native controls
            }
        },
        modifier = modifier
    )

    // Start playback
    LaunchedEffect(Unit) {
        controller.play()
    }
}