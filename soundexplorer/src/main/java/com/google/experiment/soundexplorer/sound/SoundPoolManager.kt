package com.google.experiment.soundexplorer.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
import android.media.SoundPool
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.PointSourceAttributes
import androidx.xr.scenecore.Session
import androidx.xr.scenecore.SpatialSoundPool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicBoolean

class SoundPoolManager () {
    val MAX_STREAMS = 27

    val ready: Boolean get() { return this.starting.get() && this.soundsLoading.value == 0 }

    private var starting = AtomicBoolean(false)
    private var soundsLoading = MutableStateFlow<Int>(0)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_SONIFICATION)
                .setUsage(USAGE_ASSISTANCE_SONIFICATION)
                .build()
        )
        .setMaxStreams(MAX_STREAMS)
        .build()

    init {
        soundPool.setOnLoadCompleteListener{ soundPool, sampleId, status ->
            if (status == 0) {
                this.soundsLoading.update { x -> x - 1 }
            } else {
                throw RuntimeException("Failed to load sound with status ${status}")
            }
        }
    }

    fun loadSound(applicationContext: Context, soundSampleAssetPath: String): Int? {
        this.soundsLoading.update { x -> x + 1 }

        if (this.starting.get()) { // not quite thread safe
            throw IllegalStateException("Tried to load a sound after calling start(). " +
                    "All sounds must be preloaded.")
        }

        val fd = applicationContext.assets.openFd(soundSampleAssetPath)

        val sfxId = soundPool.load(fd, 1)

        return sfxId
    }

    suspend fun start() {
        if (!this.starting.compareAndSet(false, true)) {
            return
        }

        this.soundsLoading.first { x -> x == 0 }
    }

    fun playSound(session: Session, entity: Entity, soundSampleId: Int,
                  volume: Float = 1.0f, loop: Boolean = false): Int? {
        if (!this.ready) {
            return null
        }

        val pointSource = PointSourceAttributes(entity)

        val streamId = SpatialSoundPool.play(
            session = session,
            soundPool = soundPool,
            soundID = soundSampleId,
            attributes = pointSource,
            volume = volume,
            priority = 0,
            loop = if (loop) -1 else 0,
            rate = 1.0f
        )

        return if (streamId != 0) streamId else null
    }

    fun setVolume(streamId: Int, volume: Float) {
        this.soundPool.setVolume(streamId, volume, volume)
    }
}