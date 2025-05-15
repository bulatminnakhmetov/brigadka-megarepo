package com.brigadka.app.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val full_name: String? = null,
    val age_min: Int? = null,
    val age_max: Int? = null,
    val city_id: Int? = null,
    val genders: List<String>? = null,
    val goals: List<String>? = null,
    val improv_styles: List<String>? = null,
    val looking_for_team: Boolean? = null,
    val has_avatar: Boolean? = null,
    val has_video: Boolean? = null,
    val page: Int? = null,
    val page_size: Int? = null
)

@Serializable
data class SearchResponse(
    val profiles: List<Profile>,
    val page: Int,
    val page_size: Int,
    val total_count: Int
)