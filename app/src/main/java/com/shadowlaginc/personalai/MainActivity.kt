package com.shadowlaginc.personalai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shadowlaginc.personalai.ui.theme.PersonalAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PersonalAiTheme {
                val navController = rememberNavController() // Create NavController
                val chatViewModel: ChatViewModel = viewModel(factory = GenerativeViewModelFactory)

                NavHost(navController = navController, startDestination = "chat") {
                    composable("chat") { ChatScreen(chatViewModel, navController = navController) }
                    composable("voice") {
                        VoiceScreen(
                            navController = navController,
                            chatViewModel = chatViewModel
                        )
                    }
                }
            }
        }
    }
}