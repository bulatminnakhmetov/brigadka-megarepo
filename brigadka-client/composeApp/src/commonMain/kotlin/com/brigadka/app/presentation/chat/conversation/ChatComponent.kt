package com.brigadka.app.presentation.chat.conversation

import com.arkivanov.decompose.ComponentContext
import com.brigadka.app.common.coroutineScope
import com.brigadka.app.data.api.BrigadkaApiService
import com.brigadka.app.data.api.models.ChatMessage as ChatMessageApi
import com.brigadka.app.data.api.websocket.ChatMessage as ChatMessageWS
import com.brigadka.app.data.api.websocket.ChatWebSocketClient
import com.brigadka.app.data.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatComponent(
    componentContext: ComponentContext,
    private val userDataRepository: UserDataRepository,
    private val chatID: String,
    private val api: BrigadkaApiService,
    private val webSocketClient: ChatWebSocketClient,
    private val onBackClick: () -> Unit
) : ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _topBarState = MutableStateFlow(
        ChatTopBarState(
            chatName = _uiState.value.chatName,
            isOnline = _uiState.value.isOnline,
            onBackClick = onBackClick
        )
    )
    val topBarState: StateFlow<ChatTopBarState> = _topBarState.asStateFlow()

    init {
        scope.launch {
            uiState.collect { state ->
                _topBarState.value = ChatTopBarState(
                    chatName = state.chatName,
                    isOnline = state.isOnline,
                    onBackClick = onBackClick
                )
            }
        }
    }
    // Keep track of pending messages
    private val pendingMessages = mutableMapOf<String, Message>()

    init {
        // TODO: handle exception
        _uiState.update { it.copy(currentUserId = userDataRepository.requireUserId()) }

        scope.launch {

            // Get chat
            try {
                val chat = api.getChat(chatID)
                _uiState.update { it.copy(chatName = chat.chat_name) }
            } catch (e: Exception) {
                // TODO: handler error
            }

            // Load messages history
            try {
                val messages = api.getChatMessages(chatID, 50, 0)
                _uiState.update { it.copy(messages = messages.map { it.toUiModel() }) }
            } catch (e: Exception) {
                // TODO: Handle error
            }

            // Connect WebSocket
            webSocketClient.connect()

            // Update connection state
            launch {
                webSocketClient.connectionState.collect { state ->
                    _uiState.update { it.copy(
                        isConnected = state == ChatWebSocketClient.ConnectionState.CONNECTED
                    ) }
                }
            }

            launch {
                webSocketClient.chatMessages.collect { wsMessage ->
                    if (wsMessage.chat_id == chatID) {
                        // Convert websocket message to app model

                        val message = wsMessage.toUiModel()

                        _uiState.update { state ->
                            val updatedMessages = state.messages.toMutableList()

                            // If this was a pending message that we sent, replace it
                            val pendingIndex = updatedMessages.indexOfFirst {
                                it.message_id == message.message_id
                            }

                            if (pendingIndex >= 0) {
                                updatedMessages[pendingIndex] = message
                            } else {
                                updatedMessages.add(message)
                            }

                            state.copy(messages = updatedMessages)
                        }

                        // Remove from pending
                        pendingMessages.remove(message.message_id)

                        // Mark message as read
                        webSocketClient.sendReadReceipt(chatID, message.message_id)
                    }
                }
            }
        }
    }

    fun onBack() {
        onBackClick.invoke()
    }

    suspend fun sendMessage(content: String) {
        // TODO: handle
        val chatId = chatID ?: return

        try {
            val senderID = userDataRepository.requireUserId()
            val messageId = webSocketClient.sendChatMessage(senderID, chatId, content)

            // Add as pending message
            val pendingMessage = Message(
                message_id = messageId,
                content = content,
                sender_id = senderID,
                sent_at = null
            )

            pendingMessages[messageId] = pendingMessage

            // Add to UI immediately with "pending" state
            _uiState.update { state ->
                val updatedMessages = state.messages.toMutableList()
                updatedMessages.add(pendingMessage)
                state.copy(messages = updatedMessages)
            }



        } catch (e: Exception) {
            // TODO: Handle sending error, try to send with http
        }
    }

    data class ChatUiState(
        val isConnected: Boolean = false,
        val isOnline: Boolean = false,
        val chatName: String = "",
        val participants: List<Int> = emptyList(),
        val currentUserId: Int = 0,  // This should be set from your auth state
        val messages: List<Message> = emptyList(),
        val typingUsers: Set<Int> = emptySet(),
        val isBroken: Boolean = false,
    )

    data class Message(
        val sender_id: Int,
        val message_id: String,
        val content: String,
        val sent_at: String?,
    )
}

fun ChatMessageWS.toUiModel() = ChatComponent.Message(
    message_id = message_id,
    content = content,
    sent_at = sent_at,
    sender_id = sender_id
)

fun ChatMessageApi.toUiModel() = ChatComponent.Message(
    message_id = message_id,
    content = content,
    sent_at = sent_at,
    sender_id = sender_id
)