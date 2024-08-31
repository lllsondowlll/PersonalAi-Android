package com.shadowlaginc.personalai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.shadowlaginc.personalai.ui.theme.PersonalAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalAiTheme {
                ChatScreen()  // Just call the ChatScreen composable
            }
        }
    }
}