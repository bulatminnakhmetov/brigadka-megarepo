package com.brigadka.app.presentation.profile.common

import com.brigadka.app.data.api.models.MediaItem
import kotlinx.datetime.LocalDate

data class LoadableValue<T> (
    val value: T? = null,
    val isLoading: Boolean = false,
)

data class ProfileData(
    val fullName: String = "",
    val birthday: LocalDate? = null,
    val gender: String? = null,
    val cityId: Int? = null,
    val bio: String = "",
    val goal: String = "",
    val improvStyles: List<String> = emptyList(),
    val lookingForTeam: Boolean = false,
    val avatar: LoadableValue<MediaItem> = LoadableValue(),
    val videos: List<LoadableValue<MediaItem>> = emptyList()
)