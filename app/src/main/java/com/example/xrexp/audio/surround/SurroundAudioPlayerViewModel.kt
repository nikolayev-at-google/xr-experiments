package com.example.xrexp.audio.surround

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SurroundAudioPlayerViewModel : ViewModel() {
    private var player: ExoPlayer? = null

    // UI State
    var isAudioPlaying by  mutableStateOf(false)
        private set
    var currentPosition by mutableLongStateOf(0L)
        private set
    var audioDuration by mutableLongStateOf(0L)
        private set
    var isLooping by mutableStateOf(true)
        private set
    var isPauseEnabled by mutableStateOf(false)
        private set
    var isStopEnabled by mutableStateOf(false)
        private set

    private val mediaUrl = "https://actions.google.com/sounds/v1/weather/thunderstorm.ogg"

    fun initialize(context: Context) {
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            audioDuration = this@apply.duration
                        }
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        isAudioPlaying = playing
                        isPauseEnabled = playing
                        isStopEnabled = playing || currentPosition > 0
                    }
                })

                val mediaItem = MediaItem.fromUri(mediaUrl)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = false
                repeatMode = if (isLooping) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            }

            // Start position tracking
            startPositionTracking()
        }
    }

    private fun startPositionTracking() {
        viewModelScope.launch {
            while (isActive) {
                player?.let {
                    currentPosition = it.currentPosition
                }
                delay(100) // Update every 100 ms
            }
        }
    }

    fun play() {
        player?.let {
            it.play()
            isPauseEnabled = true
            isStopEnabled = true
        }
    }

    fun pause() {
        player?.pause()
    }

    fun stop() {
        player?.let {
            it.pause()
            it.seekTo(0)
            currentPosition = 0
            isPauseEnabled = false
            isStopEnabled = false
        }
    }

    fun toggleLoop() {
        isLooping = !isLooping
        player?.let {
            it.repeatMode = if (isLooping) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        }
    }

    override fun onCleared() {
        player?.release()
        player = null
        super.onCleared()
    }

    // Helper function to format time in mm:ss format
    fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }
}