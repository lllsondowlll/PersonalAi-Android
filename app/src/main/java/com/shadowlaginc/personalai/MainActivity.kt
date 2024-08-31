package com.shadowlaginc.personalai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shadowlaginc.personalai.ui.theme.PersonalAiTheme

// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalAiTheme {
                ChatScreen()  // Initialize the ChatScreen composable
            }
        }
    }
}