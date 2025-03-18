package com.example.xrexp.audio.positional

import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf

data class PositionalAudioControlUiState(
    val angle: Float = 0f,
    val loop: Boolean = true,
    val isPlaying: Boolean = false,
    val slidersEnabled: Boolean = false,
    val showDialog: Boolean = false
)