package com.shadowlaginc.personalai

import java.util.UUID

// Participant enum to represent the sender of a message
enum class Participant {
    USER, MODEL, ERROR
}

// ChatMessage data class to represent a message in the chat
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "",
    val participant: Participant = Participant.USER,
    var isPending: Boolean = false
)