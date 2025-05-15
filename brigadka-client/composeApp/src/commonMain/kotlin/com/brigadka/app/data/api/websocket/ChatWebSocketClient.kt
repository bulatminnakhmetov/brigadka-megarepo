package com.brigadka.app.data.api.websocket

import com.brigadka.app.data.repository.AuthTokenRepository
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import io.ktor.client.request.header
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatWebSocketClient(
    private val httpClient: HttpClient,
    private val authTokenRepository: AuthTokenRepository,
    private val baseUrl: String,
    private val coroutineScope: CoroutineScope,
) {

    private val json: Json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(WebSocketMessage::class) {
                subclass(ChatMessage::class, ChatMessage.serializer())
                subclass(JoinChatMessage::class, JoinChatMessage.serializer())
                subclass(LeaveChatMessage::class, LeaveChatMessage.serializer())
                subclass(ReactionMessage::class, ReactionMessage.serializer())
                subclass(ReactionRemovedMessage::class, ReactionRemovedMessage.serializer())
                subclass(TypingMessage::class, TypingMessage.serializer())
                subclass(ReadReceiptMessage::class, ReadReceiptMessage.serializer())
            }
        }
        classDiscriminator = "type"
    }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<WebSocketMessage>()
    val incomingMessages: SharedFlow<WebSocketMessage> = _incomingMessages.asSharedFlow()

    // Separate flows for each message type
    val chatMessages: SharedFlow<ChatMessage> = _incomingMessages
        .filterIsInstance<ChatMessage>()
        .shareIn(coroutineScope, SharingStarted.Eagerly, 0)
//
//    val typingMessages: SharedFlow<TypingMessage> = _incomingMessages
//        .filterIsInstance<TypingMessage>()
//        .shareIn(CoroutineScope(Dispatchers.Default + SupervisorJob()), SharingStarted.Eagerly, 0)
//
//    val reactionMessages: SharedFlow<ReactionMessage> = _incomingMessages
//        .filterIsInstance<ReactionMessage>()
//        .shareIn(CoroutineScope(Dispatchers.Default + SupervisorJob()), SharingStarted.Eagerly, 0)
//
//    val reactionRemovedMessages: SharedFlow<ReactionRemovedMessage> = _incomingMessages
//        .filterIsInstance<ReactionRemovedMessage>()
//        .shareIn(CoroutineScope(Dispatchers.Default + SupervisorJob()), SharingStarted.Eagerly, 0)
//
//    val readReceiptMessages: SharedFlow<ReadReceiptMessage> = _incomingMessages
//        .filterIsInstance<ReadReceiptMessage>()
//        .shareIn(CoroutineScope(Dispatchers.Default + SupervisorJob()), SharingStarted.Eagerly, 0)
//
//    val joinChatMessages: SharedFlow<JoinChatMessage> = _incomingMessages
//        .filterIsInstance<JoinChatMessage>()
//        .shareIn(CoroutineScope(Dispatchers.Default + SupervisorJob()), SharingStarted.Eagerly, 0)
//
//    val leaveChatMessages: SharedFlow<LeaveChatMessage> = _incomingMessages
//        .filterIsInstance<LeaveChatMessage>()
//        .shareIn(CoroutineScope(Dispatchers.Default + SupervisorJob()), SharingStarted.Eagerly, 0)

    private val _sendChannel = Channel<WebSocketMessage>(Channel.BUFFERED)

    private var session: WebSocketSession? = null
    private var connectionJob: Job? = null
    private var reconnectionJob: Job? = null

    // Reconnection settings
    private var isReconnectionEnabled = true
    private var baseRetryDelaySeconds = 1L
    private var maxRetryDelaySeconds = 30L
    private var maxRetryAttempts = 10
    private var currentRetryAttempt = 0

    suspend fun connect(enableReconnection: Boolean = true) {
        if (_connectionState.value == ConnectionState.CONNECTED ||
            _connectionState.value == ConnectionState.CONNECTING) {
            return
        }

        isReconnectionEnabled = enableReconnection
        currentRetryAttempt = 0
        connectInternal()
    }

    private suspend fun connectInternal() {
        _connectionState.value = ConnectionState.CONNECTING

        try {
            val token = authTokenRepository.token.first().accessToken
            if (token == null) {
                _connectionState.value = ConnectionState.ERROR
                scheduleReconnectionIfNeeded()
                return
            }

            connectionJob = coroutineScope.launch(Dispatchers.IO) {
                try {
                    httpClient.webSocket(
                        method = HttpMethod.Get,
                        host = Url(baseUrl).host,
                        port = Url(baseUrl).port,
                        path = "/api/ws/chat",
                        request = {
                            header("Authorization", "Bearer $token")
                        }
                    ) {
                        session = this
                        _connectionState.value = ConnectionState.CONNECTED
                        // Reset retry counter on successful connection
                        currentRetryAttempt = 0

                        val sendJob = launch { sendMessages() }
                        val receiveJob = launch { receiveMessages() }

                        try {
                            sendJob.join()
                            receiveJob.join()
                        } finally {
                            sendJob.cancel()
                            receiveJob.cancel()
                            _connectionState.value = ConnectionState.DISCONNECTED
                            scheduleReconnectionIfNeeded()
                        }
                    }
                } catch (e: Exception) {
                    _connectionState.value = ConnectionState.ERROR
                    scheduleReconnectionIfNeeded()
                }
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.ERROR
            scheduleReconnectionIfNeeded()
        }
    }

    private fun scheduleReconnectionIfNeeded() {
        if (!isReconnectionEnabled) return
        if (currentRetryAttempt >= maxRetryAttempts) return
        if (_connectionState.value == ConnectionState.CONNECTED) return
        if (reconnectionJob?.isActive == true) return

        currentRetryAttempt++
        // Calculate backoff delay with exponential increase
        val delaySeconds = min(
            baseRetryDelaySeconds * (1 shl (currentRetryAttempt - 1)),
            maxRetryDelaySeconds
        )

        reconnectionJob = coroutineScope.launch {
            delay(delaySeconds.seconds)
            connectInternal()
        }
    }

    suspend fun disconnect(disableReconnection: Boolean = true) {
        if (disableReconnection) {
            isReconnectionEnabled = false
        }

        reconnectionJob?.cancel()
        reconnectionJob = null
        connectionJob?.cancel()
        connectionJob = null
        session?.close()
        session = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun sendChatMessage(userID: Int, chatId: String, content: String): String {
        val messageId = Uuid.random().toString()
        val message = ChatMessage(
            chat_id = chatId,
            message_id = messageId,
            content = content,
            sender_id = userID,
            sent_at = null
        )
        _sendChannel.send(message)
        return messageId
    }

    suspend fun sendTypingIndicator(chatId: String, isTyping: Boolean) {
        val message = TypingMessage(
            chat_id = chatId,
            user_id = null,
            is_typing = isTyping,
            timestamp = null
        )
        _sendChannel.send(message)
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun sendReaction(chatId: String, messageId: String, reactionCode: String): String {
        val reactionId = Uuid.random().toString()
        val message = ReactionMessage(
            chat_id = chatId,
            reaction_id = reactionId,
            message_id = messageId,
            user_id = null,
            reaction_code = reactionCode,
            reacted_at = null
        )
        _sendChannel.send(message)
        return reactionId
    }

    suspend fun removeReaction(chatId: String, messageId: String, reactionId: String, reactionCode: String) {
        val message = ReactionRemovedMessage(
            chat_id = chatId,
            reaction_id = reactionId,
            message_id = messageId,
            user_id = null,
            reaction_code = reactionCode,
            removed_at = null
        )
        _sendChannel.send(message)
    }

    suspend fun sendReadReceipt(chatId: String, messageId: String) {
        val message = ReadReceiptMessage(
            chat_id = chatId,
            user_id = null,
            message_id = messageId,
            read_at = null
        )
        _sendChannel.send(message)
    }

    private suspend fun sendMessages() {
        for (message in _sendChannel) {
            try {
                val messageJson = json.encodeToString(message)
                session?.send(Frame.Text(messageJson))
            } catch (e: Exception) {
                // Log error or handle it appropriately
            }
        }
    }

    private suspend fun receiveMessages() {
        try {
            session?.let { websocketSession ->
                for (frame in websocketSession.incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            try {
                                // Parse message type first to determine which class to deserialize to
                                val message = json.decodeFromString<WebSocketMessage>(text)
                                _incomingMessages.emit(message)
                            } catch (e: Exception) {
                                // Log error or handle invalid messages
                            }
                        }
                        else -> {} // Ignore other frame types
                    }
                }
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.ERROR
            scheduleReconnectionIfNeeded()
        }
    }

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }
}