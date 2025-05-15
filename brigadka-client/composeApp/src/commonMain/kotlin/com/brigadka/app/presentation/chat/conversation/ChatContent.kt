package com.brigadka.app.presentation.chat.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ChatContent(component: ChatComponent) {
    val uiState by component.uiState.collectAsState()
    ChatContent(uiState = uiState, onBackClick = component::onBack, onSendMessage = component::sendMessage)
}

@Composable
fun ChatContentPreview() {
    val uiState = ChatComponent.ChatUiState(
        chatName = "Jack Sparrow",
        isOnline = true,
        messages = listOf(
            ChatComponent.Message(
                message_id = "1",
                sender_id = 1,
                content = "Hello!",
                sent_at = "2023-10-01T12:00:00Z"
            ),
            ChatComponent.Message(
                message_id = "2",
                sender_id = 2,
                content = "Hi there!",
                sent_at = "2023-10-01T12:01:00Z"
            )
        ),
        currentUserId = 1,
        isConnected = true
    )

    ChatContent(uiState, onBackClick = {}, onSendMessage = {})
}
@Composable
fun ChatContent(uiState: ChatComponent.ChatUiState, onBackClick: () -> Unit, onSendMessage: suspend (String) -> Unit) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = lazyListState,
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(
                        message = message,
                        isFromCurrentUser = message.sender_id == uiState.currentUserId,
                        onReactionClick = {}
                    )
                }
            }

            // Typing indicator
            if (uiState.typingUsers.isNotEmpty()) {
                Text(
                    text = "${uiState.typingUsers.joinToString(", ")} typing...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Message input
            ChatInput(
                value = messageText,
                onValueChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotEmpty()) {
                        coroutineScope.launch {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    }
                }
            )
        }

        // Connection status
        if (!uiState.isConnected) {
            ConnectionStatusBanner()
        }
    }
}

data class ChatTopBarState(
    val chatName: String,
    val isOnline: Boolean,
    val onBackClick: () -> Unit
)

@Composable
private fun MessageBubble(
    message: ChatComponent.Message,
    isFromCurrentUser: Boolean,
    onReactionClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromCurrentUser) 16.dp else 0.dp,
                bottomEnd = if (isFromCurrentUser) 0.dp else 16.dp
            ),
            color = if (isFromCurrentUser)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromCurrentUser)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = message.sent_at?.let {
                // You might want to format the timestamp more nicely
                it.substringBefore("T") + " " + it.substringAfter("T").substringBefore(".")
            } ?: "Sending...",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("Введите сообщение...") },
                singleLine = false,
                maxLines = 5,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
            )

            IconButton(
                onClick = onSendClick,
                enabled = value.isNotEmpty(),
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = "Reconnecting...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Add to ChatContent.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(state: ChatTopBarState) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.chatName,
                    style = MaterialTheme.typography.titleMedium
                )
                if (state.isOnline) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (state.isOnline)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = state.onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}