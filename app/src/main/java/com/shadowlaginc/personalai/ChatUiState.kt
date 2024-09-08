package com.shadowlaginc.personalai

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList

class ChatUiState(
    // Add the initial state for the text field
    messages: List<ChatMessage> = emptyList(),
    initialTextFieldEmpty: Boolean = true
) {
    private val _messages: MutableList<ChatMessage> = messages.toMutableStateList()
    val messages: List<ChatMessage> = _messages

    private val _isTextFieldEmpty = mutableStateOf(initialTextFieldEmpty)
    var isTextFieldEmpty: Boolean by _isTextFieldEmpty // Expose the state as a mutable property

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

    fun updateTextFieldEmptyState(isEmpty: Boolean) {
        _isTextFieldEmpty.value = isEmpty
    }

    // Define the ButtonState enum within ChatUiState
    enum class ButtonState {
        VOICE, SEND
    }
}