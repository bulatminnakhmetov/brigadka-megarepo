package com.brigadka.app.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val chat_id: String,
    val chat_name: String,
    val created_at: String,
    val is_group: Boolean,
    val participants: List<Int>
)

@Serializable
data class ChatMessage(
    val message_id: String,
    val chat_id: String,
    val sender_id: Int,
    val content: String,
    val sent_at: String?
)

@Serializable
data class SendMessageRequest(
    val message_id: String? = null,
    val content: String
)

@Serializable
data class AddParticipantRequest(
    val user_id: Int
)

@Serializable
data class AddReactionRequest(
    val reaction_code: String,
    val reaction_id: String? = null
)

@Serializable
data class GetOrCreateDirectChatRequest(
    val user_id: Int
)

@Serializable
data class ChatIDResponse(
    val chat_id: String
)