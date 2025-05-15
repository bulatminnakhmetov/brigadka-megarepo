package com.brigadka.app.previews

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.brigadka.app.presentation.AppTheme
import com.brigadka.app.presentation.chat.conversation.ChatContentPreview
import com.brigadka.app.presentation.chat.list.ChatListContentPreview

@Preview
@Composable
fun ChatListContentPreviewPreview() {
    AppTheme {
        Surface {
            ChatListContentPreview()
        }
    }
}

@Preview
@Composable
fun ChatContentPreviewPreview() {
    AppTheme {
        Surface {
            ChatContentPreview()
        }
    }

}

