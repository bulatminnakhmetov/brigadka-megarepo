package com.brigadka.app.data.api.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// Base interface for all WebSocket messages
@Serializable
sealed interface WebSocketMessage {
    val chat_id: String
}

enum class MessageType {
    CHAT_MESSAGE,
    JOIN_CHAT,
    LEAVE_CHAT,
    REACTION,
    REMOVE_REACTION,
    TYPING,
    READ_RECEIPT
}

@Serializable
@SerialName("chat_message")
data class ChatMessage(
    @SerialName("chat_id")
    override val chat_id: String,
    @SerialName("message_id")
    val message_id: String,
    @SerialName("sender_id")
    val sender_id: Int,
    @SerialName("content")
    val content: String,
    @SerialName("sent_at")
    val sent_at: String? = null
) : WebSocketMessage

@Serializable
@SerialName("join_chat")
data class JoinChatMessage(
    @SerialName("chat_id")
    override val chat_id: String,
    @SerialName("user_id")
    val user_id: Int,
    @SerialName("joined_at")
    val joined_at: String? = null
) : WebSocketMessage

@Serializable
@SerialName("leave_chat")
data class LeaveChatMessage(
    @SerialName("chat_id")
    override val chat_id: String,
    @SerialName("user_id")
    val user_id: Int,
    @SerialName("left_at")
    val left_at: String? = null
) : WebSocketMessage

@Serializable
@SerialName("reaction")
data class ReactionMessage(
    @SerialName("chat_id")
    override val chat_id: String,
    @SerialName("reaction_id")
    val reaction_id: String,
    @SerialName("message_id")
    val message_id: String,
    @SerialName("user_id")
    val user_id: Int? = null,
    @SerialName("reaction_code")
    val reaction_code: String,
    @SerialName("reacted_at")
    val reacted_at: String? = null
) : WebSocketMessage

@Serializable
@SerialName("remove_reaction")
data class ReactionRemovedMessage(
    @SerialName("chat_id")
    override val chat_id: String,
    @SerialName("reaction_id")
    val reaction_id: String,
    @SerialName("message_id")
    val message_id: String,
    @SerialName("user_id")
    val user_id: Int? = null,
    @SerialName("reaction_code")
    val reaction_code: String,
    @SerialName("removed_at")
    val removed_at: String? = null
) : WebSocketMessage

@Serializable
@SerialName("typing")
data class TypingMessage(
    @SerialName("chat_id")
    override val chat_id: String,
    @SerialName("user_id")
    val user_id: Int? = null,
    @SerialName("is_typing")
    val is_typing: Boolean,
    @SerialName("timestamp")
    val timestamp: String? = null
) : WebSocketMessage

@Serializable
@SerialName("read_receipt")
data class ReadReceiptMessage(
    @SerialName("chat_id")
    override val chat_id: String,
    @SerialName("user_id")
    val user_id: Int? = null,
    @SerialName("message_id")
    val message_id: String,
    @SerialName("read_at")
    val read_at: String? = null
) : WebSocketMessage
