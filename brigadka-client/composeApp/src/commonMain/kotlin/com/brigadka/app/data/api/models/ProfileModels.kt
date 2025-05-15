package com.brigadka.app.data.api.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable


@Serializable
data class ProfileCreateRequest(
    val user_id: Int,
    val full_name: String,
    val bio: String,
    val birthday: LocalDate,
    val city_id: Int,
    val gender: String,
    val goal: String,
    val improv_styles: List<String>,
    val looking_for_team: Boolean,
    val avatar: Int?,
    val videos: List<Int>?
)

@Serializable
data class ProfileUpdateRequest(
    val full_name: String? = null,
    val bio: String? = null,
    val birthday: LocalDate? = null,
    val city_id: Int? = null,
    val gender: String? = null,
    val goal: String? = null,
    val improv_styles: List<String>? = null,
    val looking_for_team: Boolean? = null,
    val avatar: Int? = null,
    val videos: List<Int>? = null
)

@Serializable
data class Profile(
    val user_id: Int,
    val full_name: String,
    val bio: String,
    val birthday: LocalDate,
    val city_id: Int,
    val gender: String,
    val goal: String,
    val improv_styles: List<String> = emptyList(),
    val looking_for_team: Boolean,
    val avatar: MediaItem? = null,
    val videos: List<MediaItem> = emptyList()
)

@Serializable
data class StringItem(
    val code: String,
    val label: String,
)

@Serializable
data class City(
    val id: Int,
    val name: String
)