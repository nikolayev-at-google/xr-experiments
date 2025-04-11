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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

class SoundPoolManager (maxStreams: Int) {

    private val _soundsLoaded = MutableStateFlow<Boolean>(false)
    val soundsLoaded: StateFlow<Boolean> = _soundsLoaded.asStateFlow()

    private var soundsLoading = AtomicInteger(0)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_SONIFICATION)
                .setUsage(USAGE_ASSISTANCE_SONIFICATION)
                .build()
        )
        .setMaxStreams(maxStreams)
        .build()

    init {
        soundPool.setOnLoadCompleteListener{ soundPool, sampleId, status ->
            if (status == 0) {
                if (this.soundsLoading.decrementAndGet() == 0) {
                    this._soundsLoaded.value = true
                }
            } else {
                throw RuntimeException("Failed to load sound with status ${status}")
            }
        }
    }

    fun loadSound(applicationContext: Context, soundSampleAssetPath: String): Int? {
        if (this.soundsLoading.incrementAndGet() == 1) {
            this._soundsLoaded.value = false
        }

        return soundPool.load(applicationContext.assets.openFd(soundSampleAssetPath), 1)
    }

    fun playSound(session: Session, entity: Entity, soundSampleId: Int,
                  volume: Float = 1.0f, loop: Boolean = false): Int? {
        if (!this.soundsLoaded.value) {
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