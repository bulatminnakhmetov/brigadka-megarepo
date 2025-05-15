package com.brigadka.app.presentation.chat.list

import com.arkivanov.decompose.ComponentContext
import com.brigadka.app.common.coroutineScope
import com.brigadka.app.data.api.BrigadkaApiService
import com.brigadka.app.data.api.models.MediaItem
import com.brigadka.app.data.api.websocket.ChatWebSocketClient
import com.brigadka.app.data.repository.ProfileRepository
import com.brigadka.app.data.repository.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime


class ChatListComponent(
    componentContext: ComponentContext,
    private val api: BrigadkaApiService,
    private val profileRepository: ProfileRepository,
    private val userDataRepository: UserDataRepository,
    private val webSocketClient: ChatWebSocketClient,
    private val onChatSelected: (String) -> Unit
) : ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadChats()

        // Listen for new messages
        scope.launch {
            webSocketClient.chatMessages.collect { message ->
                // Refresh chat list when new message arrives
                loadChats()
            }
        }
    }

    private fun loadChats() {
        _uiState.update { it.copy(isLoading = true) }

        scope.launch {
            try {
                val chats = api.getChats()
                val chatPreviews = mutableListOf<ChatPreview>()

                for (chat in chats) {
                    // Get last message
                    val messages = api.getChatMessages(chat.chat_id, 1, 0)
                    val lastMessage = messages.firstOrNull()

                    // Get other participant profile for direct chats
                    var name = chat.chat_name
                    var avatar: MediaItem? = null

                    if (!chat.is_group && chat.participants.isNotEmpty()) {
                        val currentUserId = userDataRepository.requireUserId()
                        val otherParticipants = chat.participants.filter { it != currentUserId }
                        if (otherParticipants.isNotEmpty()) {
                            try {
                                val otherProfile = profileRepository.getProfileView(otherParticipants.first())
                                name = otherProfile.fullName
                                avatar = otherProfile.avatar
                            } catch (e: Exception) {
                                // Use default name if profile fetch fails
                            }
                        }
                    }

                    chatPreviews.add(
                        ChatPreview(
                            chatId = chat.chat_id,
                            name = name,
                            avatar = avatar,
                            lastMessage = lastMessage?.content,
                            lastMessageTime = lastMessage?.sent_at?.let { formatMessageTime(it) },
                            unreadCount = 0 // We'll implement unread count later
                        )
                    )
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    chats = chatPreviews.sortedByDescending {
                        it.lastMessageTime // Sort by last message time
                    }
                ) }

            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e
                ) }
            }
        }
    }

    fun onChatSelected(chatId: String) {
        onChatSelected.invoke(chatId)
    }

    fun onError(error: String) {
        // TODO: Log or handle error
    }

    private fun formatMessageTime(timestamp: String): String {
        try {
            val instant = Instant.parse(timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            return when {
                // Today
                localDateTime.date == now.date -> {
                    "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
                }
                // Yesterday
                localDateTime.date.daysUntil(now.date) == 1 -> {
                    "Yesterday"
                }
                // This week
                localDateTime.date.daysUntil(now.date) < 7 -> {
                    when (localDateTime.dayOfWeek) {
                        DayOfWeek.MONDAY -> "Mon"
                        DayOfWeek.TUESDAY -> "Tue"
                        DayOfWeek.WEDNESDAY -> "Wed"
                        DayOfWeek.THURSDAY -> "Thu"
                        DayOfWeek.FRIDAY -> "Fri"
                        DayOfWeek.SATURDAY -> "Sat"
                        DayOfWeek.SUNDAY -> "Sun"
                        else -> ""
                    }
                }
                // Older
                else -> {
                    "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}"
                }
            }
        } catch (e: Exception) {
            return ""
        }
    }

    data class UiState(
        val isLoading: Boolean = false,
        val chats: List<ChatPreview> = emptyList(),
        val error: Throwable? = null
    )

    data class ChatPreview(
        val chatId: String,
        val name: String,
        val avatar: MediaItem?,
        val lastMessage: String?,
        val lastMessageTime: String?,
        val unreadCount: Int = 0
    )
}