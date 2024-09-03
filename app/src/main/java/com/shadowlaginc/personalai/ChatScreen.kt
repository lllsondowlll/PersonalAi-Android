package com.shadowlaginc.personalai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = viewModel(factory = GenerativeViewModelFactory),
    navController: NavController = rememberNavController()
) {
    val chatUiState by chatViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            MessageInput(
                chatUiState = chatUiState,
                onSendMessage = { inputText ->
                    chatViewModel.sendMessage(inputText)
                    chatUiState.updateTextFieldEmptyState(true)
                },
                resetScroll = {
                    coroutineScope.launch {
                        listState.scrollToItem(0)
                    }
                },
                onVoiceButtonClicked = {
                    navController.navigate("voice")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            ChatLogWindow(chatUiState.messages, listState)
        }
    }
}

@Composable
fun ChatLogWindow(
    chatMessages: List<ChatMessage>,
    listState: LazyListState
) {
    LazyColumn(
        reverseLayout = true,
        state = listState
    ) {
        items(chatMessages.reversed()) { message ->
            ChatMessageTheme(message)
        }
    }
}

@Composable
fun ChatMessageTheme(chatMessage: ChatMessage) {
    val isModelMessage = chatMessage.participant == Participant.MODEL ||
            chatMessage.participant == Participant.ERROR

    val backgroundColor = when (chatMessage.participant) {
        Participant.USER -> MaterialTheme.colorScheme.primary
        Participant.MODEL -> MaterialTheme.colorScheme.secondary
        Participant.ERROR -> MaterialTheme.colorScheme.error
    }

    val textColor = when (chatMessage.participant) {
        Participant.USER -> MaterialTheme.colorScheme.onPrimary
        Participant.MODEL -> MaterialTheme.colorScheme.onSecondary
        Participant.ERROR -> MaterialTheme.colorScheme.onError
    }

    val bubbleShape = if (isModelMessage) {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    }

    val horizontalAlignment = if (isModelMessage) {
        Alignment.Start
    } else {
        Alignment.End
    }

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = chatMessage.participant.name,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row {
            if (chatMessage.isPending) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(all = 8.dp)
                )
            }
            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = bubbleShape,
                    modifier = Modifier.widthIn(0.dp, maxWidth * 0.9f)
                ) {
                    Text(
                        text = chatMessage.text,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun MessageInput(
    chatUiState: ChatUiState,
    onSendMessage: (String) -> Unit,
    resetScroll: () -> Unit = {},
    onVoiceButtonClicked: () -> Unit
) {
    var userMessage by rememberSaveable { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        shape = RoundedCornerShape(50.dp),
    ) {
        Row(modifier = Modifier) {
            TextField(
                value = userMessage,
                placeholder = { Text(stringResource(R.string.chat_label)) },
                onValueChange = {
                    userMessage = it
                    chatUiState.updateTextFieldEmptyState(it.isBlank())
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                modifier = Modifier
                    .weight(0.85f)
                    .padding(0.dp),
                shape = RoundedCornerShape(50.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Determine the button state based on isTextFieldEmpty
            val buttonState = if (chatUiState.isTextFieldEmpty) {
                ChatUiState.ButtonState.VOICE
            } else {
                ChatUiState.ButtonState.SEND
            }

            // Render the appropriate button based on buttonState
            when (buttonState) {
                ChatUiState.ButtonState.VOICE -> {
                    IconButton(
                        onClick = {
                            onVoiceButtonClicked()
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically)
                            .fillMaxWidth()
                            .weight(0.15f)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.gemini_voice),
                            contentDescription = stringResource(R.string.action_activate_chat),
                            modifier = Modifier
                        )
                    }
                }

                ChatUiState.ButtonState.SEND -> {
                    IconButton(
                        onClick = {
                            onSendMessage(userMessage)
                            userMessage = ""
                            resetScroll()
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically)
                            .fillMaxWidth()
                            .weight(0.15f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.action_send),
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}