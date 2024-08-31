package com.shadowlaginc.personalai

import androidx.compose.runtime.toMutableStateList

class ChatUiState(
    messages: List<ChatMessage> = emptyList()
) {
    private val _messages: MutableList<ChatMessage> = messages.toMutableStateList()
    val messages: List<ChatMessage> = _messages

    // appends a new message to the chat UI state
    fun addMessage(msg: ChatMessage) {
        _messages.add(msg)
    }

    // replaces the last pending message with a new message
    fun replaceLastPendingMessage() {
        val lastMessage = _messages.lastOrNull()
        lastMessage?.let {
            val newMessage = lastMessage.apply { isPending = false }
            _messages.removeLast()
            _messages.add(newMessage)
        }
    }
}

