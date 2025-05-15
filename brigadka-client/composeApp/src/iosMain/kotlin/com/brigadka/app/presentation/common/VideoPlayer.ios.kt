package com.brigadka.app.presentation.profile.common

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.AVKit.AVPlayerViewController
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL
import platform.UIKit.UIView

actual interface VideoPlayerController {
    actual fun play()
    actual fun pause()
    actual fun seekTo(position: Long)
    actual fun release()
    fun getPlayer(): AVPlayer // Add accessor method for player
}

class IOSVideoPlayerController(private val player: AVPlayer) : VideoPlayerController {
    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun seekTo(position: Long) {
        val time = CMTimeMakeWithSeconds(position / 1000.0, 1000)
        player.seekToTime(time)
    }

    override fun release() {
        player.pause()
        player.replaceCurrentItemWithPlayerItem(null)
    }

    // Add accessor method to allow access to the player
    override fun getPlayer(): AVPlayer = player
}

@Composable
actual fun rememberVideoPlayerController(url: String): VideoPlayerController {
    val player = remember {
        val nsURL = NSURL.URLWithString(url) ?: error("Invalid URL: $url")
        val playerItem = AVPlayerItem.playerItemWithURL(nsURL)
        AVPlayer(playerItem)
    }

    DisposableEffect(Unit) {
        onDispose {
            player.pause()
            player.replaceCurrentItemWithPlayerItem(null)
        }
    }

    return remember { IOSVideoPlayerController(player) }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    controller: VideoPlayerController,
    modifier: Modifier
) {
    val iosController = controller as IOSVideoPlayerController
    val player = remember { iosController.getPlayer() } // Use the accessor method

    UIKitView<UIView>(
        factory = {
            val playerViewController = AVPlayerViewController()
            playerViewController.player = player
            playerViewController.showsPlaybackControls = true // Using native controls
            playerViewController.view
        },
        modifier = modifier,
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        )
    )

    // Start playback
    LaunchedEffect(Unit) {
        controller.play()
    }
}