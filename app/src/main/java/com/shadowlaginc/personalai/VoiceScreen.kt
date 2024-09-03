package com.shadowlaginc.personalai

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(navController: NavController) {
    var isRecording by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }

    // Request microphone permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, start recording
                isRecording = true
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                navController.popBackStack() // Go back to the previous screen
            }
        }
    )

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
            if (isRecording) {
                // TODO: Implement speech recognition UI and logic here
                Text("Recording... (Placeholder)")
                Text("Recognized text: $recognizedText")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { isRecording = false }) {
                    Text("Stop Recording")
                }
            } else {
                // Check for permission and request if needed
                if (ContextCompat.checkSelfPermission(
                        LocalContext.current,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Button(onClick = { isRecording = true }) {
                        Text("Start Recording")
                    }
                } else {
                    Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }) {
                        Text("Request Microphone Permission")
                    }
                }
            }
        }
    }
}