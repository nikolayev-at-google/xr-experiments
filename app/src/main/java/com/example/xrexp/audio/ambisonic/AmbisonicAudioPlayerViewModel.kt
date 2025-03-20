package com.example.xrexp.audio.ambisonic

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AmbisonicAudioPlayerViewModel : ViewModel() {
    private var mediaPlayer: MediaPlayer? = null

    // UI State
    var isPlaying by mutableStateOf(false)
        private set
    var currentPosition by mutableStateOf(0)
        private set
    var audioDuration by mutableStateOf(0)
        private set
    var isLooping by mutableStateOf(false)
        private set
    var isPauseEnabled by mutableStateOf(false)
        private set
    var isStopEnabled by mutableStateOf(false)
        private set

    private val mediaUrl = "https://actions.google.com/sounds/v1/weather/desert_howling_wind.ogg"

    fun initialize(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setOnPreparedListener { mp ->
                    audioDuration = mp.duration
                }

                setOnCompletionListener {
                    if (!isLooping) {
                        stop() // Reset to initial state when playback completes
                    }
                }

                try {
                    setDataSource(mediaUrl)
                    prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Start position tracking
            startPositionTracking()
        }
    }

    private fun startPositionTracking() {
        viewModelScope.launch {
            while (isActive) {
                mediaPlayer?.let {
                    if (isPlaying) {
                        currentPosition = it.currentPosition
                    }
                }
                delay(100) // Update every 100 ms
            }
        }
    }

    fun play() {
        mediaPlayer?.let {
            if (!isPlaying) {
                it.start()
                isPlaying = true
                isPauseEnabled = true
                isStopEnabled = true
            }
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
                isPauseEnabled = false
                isStopEnabled = true
            }
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                // We need to prepare again after stop
                try {
                    it.prepare()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            it.seekTo(0)
            isPlaying = false
            currentPosition = 0
            isPauseEnabled = false
            isStopEnabled = false
        }
    }

    fun toggleLoop() {
        isLooping = !isLooping
        mediaPlayer?.isLooping = isLooping
    }

    override fun onCleared() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onCleared()
    }

    // Helper function to format time in mm:ss format
    fun formatTime(millis: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis.toLong()) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}