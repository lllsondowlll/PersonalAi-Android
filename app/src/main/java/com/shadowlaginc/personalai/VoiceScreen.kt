package com.shadowlaginc.personalai

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.Context
import android.media.AudioManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(navController: NavController, chatViewModel: ChatViewModel) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(true) } // Controls whether listening is active
    var recognizedText by remember { mutableStateOf("") }
    var isPaused by remember { mutableStateOf(false) } // Controls if the app is paused by the user
    val uiState by chatViewModel.uiState.collectAsState()
    val latestMessage = uiState.messages.lastOrNull { it.participant == Participant.MODEL }?.text ?: ""

    // Check if speech recognition is available
    val isSpeechRecognitionAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }

    // Request microphone permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                isRecording = true
            } else {
                navController.popBackStack() // Go back to the previous screen if permission is denied
            }
        }
    )

    fun muteSystemSounds(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
    }

    fun unmuteSystemSounds(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
    }

    // Function to manage speech recognition
    @Composable
    fun manageSpeechRecognition(isRecording: Boolean, isPaused: Boolean, recognitionListener: RecognitionListener) {
        if (isRecording && !isPaused) {
            val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Prefer offline recognition
            }

            speechRecognizer.setRecognitionListener(recognitionListener)
            speechRecognizer.startListening(intent)

            // Cleanup on disposal
            DisposableEffect(Unit) {
                onDispose {
                    speechRecognizer.stopListening()
                    speechRecognizer.destroy()
                    unmuteSystemSounds(context)
                }
            }
        }
    }

    // Speech recognition listener
    val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            recognizedText = "Listening..."
            muteSystemSounds(context)
        }

        override fun onBeginningOfSpeech() {
            //TODO("Not yet implemented")
            recognizedText = "Listening..."
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            recognizedText = "Processing..."
            unmuteSystemSounds(context)
        }

        //Continuous Text to Speech handling
        override fun onError(error: Int) {
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    isRecording = false
                    isRecording = true
                }
                else -> {
                    recognizedText = "Error: $error"
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            recognizedText = matches?.getOrNull(0) ?: "No results found."

            // Automatically send the message after recognizing speech
            if (recognizedText.isNotBlank()) {
                chatViewModel.sendMessage(recognizedText)
                isRecording = false // Stop listening until the model has responded
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    // Manage speech recognition based on isRecording and isPaused
    manageSpeechRecognition(isRecording, isPaused, recognitionListener)

    // Restart listening when the model responds
    LaunchedEffect(latestMessage) {
        if (latestMessage.isNotBlank() && !isPaused) {
            isRecording = true // Restart speech recognition when the model responds
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.voice_input)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isSpeechRecognitionAvailable) {
                Text("Speech recognition is not available on this device.")
            } else {
                // Display the recognized text
                Text("User: $recognizedText")

                // Display the latest message from the model
                Text("Model: $latestMessage")

                Spacer(modifier = Modifier.height(16.dp))

                // Pause/Resume button to control the hands-free flow
                Button(onClick = {
                    isPaused = !isPaused
                    isRecording = !isPaused // If paused, stop recording; if resumed, start recording
                }) {
                    Text(if (isPaused) "Resume" else "Pause")
                }
            }
        }
    }
}