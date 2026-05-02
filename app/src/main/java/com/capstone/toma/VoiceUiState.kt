package com.capstone.toma

/**
 * CHANGED: openWakeWord migration - Expanded states for Voice & Enrollment
 */
sealed interface VoiceUiState {
    // Basic Voice States
    data object Idle : VoiceUiState
    data object Listening : VoiceUiState
    data object Processing : VoiceUiState
    data object Speaking : VoiceUiState
    data object Recovering : VoiceUiState
    data class Result(val text: String) : VoiceUiState
    data class Error(val message: String) : VoiceUiState

    // Enrollment (Speaker Registration) States
    data class Enrolling(val currentCount: Int, val totalCount: Int = 30) : VoiceUiState
    data object Uploading : VoiceUiState
    data object Training : VoiceUiState
}
