package com.example.xrexp.audio.positional


data class PositionalAudioControlUiState(
    val angle: Float = 0f,
    val distance: Float = 0f,
    val loop: Boolean = true,
    val isPlaying: Boolean = false,
    val slidersEnabled: Boolean = false,
    val showDialog: Boolean = false
)