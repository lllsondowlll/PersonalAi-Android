package com.shadowlaginc.personalai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    generativeModel: GenerativeModel
) : ViewModel() {
    private val chat = generativeModel.startChat(

        // Define the chat history -- to do
        history = listOf(
            content(role = "user") { text("") },
            content(role = "model") { text("") }
        )
    )

    // Define the chat UI state
    private val _uiState: MutableStateFlow<ChatUiState> =
        MutableStateFlow(ChatUiState(chat.history.mapNotNull { content ->
            val messageText = content.parts.first().asTextOrNull() ?: ""
            if (messageText.isNotBlank()) {
                ChatMessage(
                    text = messageText,
                    participant = if (content.role == "user") Participant.USER else Participant.MODEL,
                    isPending = false
                )
            } else {
                null // Skip empty messages
            }
        }))

    // Expose the chat UI state as a StateFlow
    val uiState: StateFlow<ChatUiState> =
        _uiState.asStateFlow()


    // Send a message to the chat
    fun sendMessage(userMessage: String) {
        _uiState.value.addMessage(
            ChatMessage(
                text = userMessage,
                participant = Participant.USER,
                // Indicate that the message is pending until the response is received
                isPending = true
            )
        )

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMessage)

                _uiState.value.replaceLastPendingMessage()

                // Add the model response
                response.text?.let { modelResponse ->
                    _uiState.value.addMessage(
                        ChatMessage(
                            text = modelResponse,
                            participant = Participant.MODEL,
                            isPending = false // The response has been received
                        )
                    )
                }
            } catch (e: Exception) { // Handle exceptions
                _uiState.value.replaceLastPendingMessage()
                _uiState.value.addMessage(
                    ChatMessage(
                        text = e.localizedMessage ?: "Unknown error",
                        participant = Participant.ERROR // Indicate the error in the UI
                    )
                )
            }
        }
    }
}
