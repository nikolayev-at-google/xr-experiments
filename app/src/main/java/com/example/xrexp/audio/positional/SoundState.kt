package com.example.xrexp.audio.positional

data class SoundState(
    val isLoaded: Boolean = false,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val name: String = ""
)