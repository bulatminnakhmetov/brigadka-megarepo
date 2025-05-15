package com.brigadka.app.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class MediaItem(
    val id: Int,
    val thumbnail_url: String,
    val url: String
)