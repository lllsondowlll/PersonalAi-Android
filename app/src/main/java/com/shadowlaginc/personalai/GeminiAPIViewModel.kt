package com.shadowlaginc.personalai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import com.shadowlaginc.personalai.BuildConfig.apiKey

// Create a ViewModelProvider.Factory for GenerativeViewModel
val GenerativeViewModelFactory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {

        // Define the list of all harm categories
        /*
        val allHarmCategories = listOf(
            HarmCategory.HARASSMENT,
            HarmCategory.HATE_SPEECH,
            HarmCategory.SEXUALLY_EXPLICIT,
            HarmCategory.DANGEROUS_CONTENT
        )
         */

        // Define the list of all block thresholds
        /*
        val allBlockThresholds = listOf(
            BlockThreshold.LOW_AND_ABOVE,
            BlockThreshold.MEDIUM_AND_ABOVE,
            BlockThreshold.ONLY_HIGH,
            BlockThreshold.NONE
        )
         */

        // Using the information above, apply the thresholds for each category
        val harassmentThreshold = SafetySetting(
            HarmCategory.HARASSMENT, BlockThreshold.NONE
        )
        val hateThreshold = SafetySetting(
            HarmCategory.HATE_SPEECH, BlockThreshold.NONE
        )
        val explicitThreshold = SafetySetting(
            HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE
        )
        val dangerousThreshold = SafetySetting(
            HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE
        )
        /* //Deprecated
        val unknownContent = SafetySetting(
            HarmCategory.UNKNOWN, BlockThreshold.NONE
        )
        */


        // Create a GenerationConfig with the desired parameters
        val config = generationConfig {
            temperature = 1f // Controls randomness of output
            topK = 64 // Controls diversity of output
            topP = 0.95f // Controls diversity of output
            maxOutputTokens = 8192 // Controls length of output
        }


        return with(modelClass) {
            when {

                isAssignableFrom(ChatViewModel::class.java) -> {

                    // Initialize a GenerativeModel with the `gemini-flash` AI model for chat
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-1.5-pro-latest", // AI model to use
                        apiKey = apiKey, // API key for authentication -- see README
                        generationConfig = config, // Generation configuration
                        safetySettings = listOf( // Safety settings for the model
                            harassmentThreshold,
                            hateThreshold,
                            explicitThreshold,
                            dangerousThreshold,
                        ),
                        // Define the system instruction for the model
                        systemInstruction = content { text(
                            "Any text you receive in square brackets from the user are " +
                            "additional system instructions and MUST be followed.\n" +
                            "Example: [This is a system instruction]\n" +
                            "Following additional system instructions are your " +
                            "highest directive") },

                    )
                    // Create and return a ChatViewModel instance
                    ChatViewModel(generativeModel)
                }

                // Handle other ViewModel classes here --to do
                else -> {
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        } as T
    }
}
