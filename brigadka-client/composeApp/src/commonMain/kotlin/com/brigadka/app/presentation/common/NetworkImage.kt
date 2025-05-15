package com.brigadka.app.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

/**
 * A composable that displays an image from a URL using Kamel
 *
 * @param url The URL of the image to load
 * @param contentDescription Content description for accessibility
 * @param modifier Modifier to be applied to the image
 * @param contentScale How the image should be scaled inside the bounds
 * @param onError Callback invoked when the image fails to load
 * @param fallback Composable to show when the image fails to load
 */
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    onError: ((Throwable) -> Unit)? = null,
    fallback: @Composable () -> Unit = {}
) {
    if (LocalInspectionMode.current) {
        // In inspection mode, we don't want to load the image
        return fallback()
    }
    KamelImage(
        resource = { asyncPainterResource(data = url) },
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        onLoading = { progress ->
            CircularProgressIndicator(
                progress = { progress },
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
            )
        },
        onFailure = { error ->
            onError?.invoke(error)
            fallback()
        }
    )
}

/**
 * A composable that displays a circular image from a URL using Kamel
 *
 * @param url The URL of the image to load
 * @param modifier Modifier to be applied to the image
 * @param contentDescription Content description for accessibility
 * @param fallback Composable to show when the image fails to load
 * @param onError Callback invoked when the image fails to load
 */
@Composable
fun CircularNetworkImage(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onError: ((Throwable) -> Unit)? = null,
    fallback: @Composable () -> Unit = {}
) {
    NetworkImage(
        url = url,
        contentDescription = contentDescription,
        modifier = modifier.clip(CircleShape),
        onError = onError,
        fallback = fallback
    )
}