package com.shadowlaginc.personalai

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface UiState {

    // Empty state when the screen is first shown
    data object Initial : UiState

    // Still loading
    data object Loading : UiState

    // Text has been generated
    data class Success(val outputText: String) : UiState

    // There was an error generating text
    data class Error(val errorMessage: String) : UiState
}