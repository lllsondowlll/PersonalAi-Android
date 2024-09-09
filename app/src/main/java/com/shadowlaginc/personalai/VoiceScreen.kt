package com.shadowlaginc.personalai

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import io.github.whitemagic2014.tts.TTS
import io.github.whitemagic2014.tts.TTSVoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun VoiceWaveAnimation(isSpeaking: Boolean) {
    // Animation logic
    val waveHeight by animateFloatAsState(
        targetValue = if (isSpeaking) 100f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "Speech Animation Height"
    )

    val infiniteTransition = rememberInfiniteTransition(
        label = "Speech Animation Transition")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "Speech Animation"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        val wavePath = Path()
        val amplitude = waveHeight
        val wavelength = size.width / 2f

        wavePath.moveTo(0f, size.height / 2)
        for (x in 0..size.width.toInt()) {
            val y =
                amplitude * sin((x.toFloat() / wavelength) + waveOffset) + (size.height / 2)
            wavePath.lineTo(x.toFloat(), y)
        }

        drawPath(
            path = wavePath,
            color = Color.Cyan,
            style = Stroke(width = 4f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(navController: NavController, chatViewModel: ChatViewModel) {
    val context = LocalContext.current
    var isSpeaking by remember { mutableStateOf(false) }  // Controls whether user is speaking
    var isRecording by remember { mutableStateOf(false) } // Controls whether listening is active
    val scrollState = rememberScrollState()
    var recognizedText by remember { mutableStateOf("") }
    var isPaused by remember { mutableStateOf(false) } // Controls if the app is paused by the user
    val uiState by chatViewModel.uiState.collectAsState()
    val latestMessage =
        uiState.messages.lastOrNull { it.participant == Participant.MODEL }?.text ?: ""
    // Check if speech recognition is available
    val isSpeechRecognitionAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    var isAppExiting by remember { mutableStateOf(false) }
    // Request microphone permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                isRecording = true
            } else {
                isRecording = false
                navController.popBackStack() // Go back to the previous screen if permission is denied
            }
        }
    )
    LaunchedEffect(Unit) {
        launcher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    fun muteSystemSounds(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.ADJUST_MUTE,
            0
        )
        audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
    }

    fun unmuteSystemSounds(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.ADJUST_UNMUTE,
            0
        )
        audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
    }

    // Function to manage speech recognition
    @Composable
    fun manageSpeechRecognition(
        isRecording: Boolean,
        isPaused: Boolean,
        recognitionListener: RecognitionListener
    ) {
        muteSystemSounds(context)
        if (isRecording && !isPaused) {
            val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
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
                    if (isAppExiting) {
                        unmuteSystemSounds(context)
                    }
                }
            }
        }
    }

    // Speech recognition listener
    val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            recognizedText = "Listening..."
        }

        override fun onBeginningOfSpeech() {
            recognizedText = "Listening..."
            isSpeaking = true
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            recognizedText = "Processing..."
            isSpeaking = false
        }

        override fun onError(error: Int) {
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    isRecording = false
                    isRecording = true
                }

                else -> {
                    recognizedText = "Error: $error"
                    isSpeaking = false
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
            unmuteSystemSounds(context)
            playTTSResponse(latestMessage, context) {
                isRecording = true
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                isAppExiting = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                // Scrollable section for recognized and model output
                Column(
                    modifier = Modifier
                        .weight(1f) // Allows scrolling to take available vertical space
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp)
                ) {
                    // Display the recognized text
                    Text("User: $recognizedText")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display the latest message from the model
                    Text("Model: $latestMessage")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display your new wave animation
                VoiceWaveAnimation(isSpeaking = isSpeaking)

                Spacer(modifier = Modifier.height(16.dp))

                // Pause/Resume button to control the hands-free flow
                Button(onClick = {
                    isPaused = !isPaused
                    isRecording = !isPaused
                }) {
                    Text(if (isPaused) "Resume" else "Pause")
                }
            }
        }
    }
}

// Helper function to remove emojis from a string
fun removeEmojis(text: String): String {
    // Regex to match emojis and remove them
    val emojiPattern = "[\\p{So}\\p{Cn}]+".toRegex()
    return text.replace(emojiPattern, "")
}

suspend fun playTTSResponse(latestMessage: String, context: Context, onCompletion: () -> Unit) {
    // Ignore list for model-generated audio playback
    val audioSafeMessage = withContext(Dispatchers.IO) {
        removeEmojis(latestMessage) // Strip emojis in a background thread for performance
    }

    val voice =
        TTSVoice.provides().stream().filter { v -> v.shortName == "en-US-AvaNeural" }.findFirst()
            .orElse(null)

    suspend fun deleteIfExists(fileName: String) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    // Perform file deletion on IO thread
    withContext(Dispatchers.IO) {
        deleteIfExists("response.mp3")
        deleteIfExists("response.vtt")
    }

    // Perform TTS and media playback on IO thread
    withContext(Dispatchers.IO) {
        val fileName = TTS(voice, audioSafeMessage)  // Use cleanedMessage without emojis for TTS
            .findHeadHook()
            .fileName("response")
            .storage(context.filesDir.absolutePath)
            .trans()

        // Play media file asynchronously
        playMediaAsync(context.filesDir.absolutePath + "/$fileName")

        // Ensure onCompletion is called on the main thread
        withContext(Dispatchers.Main) {
            onCompletion()
        }
    }
}

// A helper function to play media in a coroutine-friendly way
suspend fun playMediaAsync(filePath: String) = suspendCancellableCoroutine<Unit> { continuation ->
    val mediaPlayer = MediaPlayer().apply {
        setDataSource(filePath)
        prepare()
        start()
    }

    mediaPlayer.setOnCompletionListener {
        mediaPlayer.release()
        continuation.resume(Unit) // Resume coroutine when playback is complete
    }

    // Ensure mediaPlayer is released if coroutine is cancelled
    continuation.invokeOnCancellation {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}