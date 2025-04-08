package com.google.experiment.soundexplorer.sound

import androidx.xr.scenecore.Component
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SoundCompositionSimple (
    val soundPoolManager: SoundPoolManager,
    val session: Session) {

    private val _unattachedComponents = MutableStateFlow<Int>(0)
    val unattachedComponents: StateFlow<Int> = _unattachedComponents.asStateFlow()

    private var compositionComponents = mutableListOf<SoundCompositionComponent>()
    private var isPlaying = false
    private var soundsInitialized = false

    enum class SoundSampleType {
        LOW,
        MEDIUM,
        HIGH
    }

    inner class SoundCompositionComponent (
        val composition: SoundCompositionSimple,
        val lowSoundId: Int,
        val mediumSoundId: Int,
        val highSoundId: Int,
        defaultSoundType: SoundSampleType = SoundSampleType.MEDIUM
    ) : Component {
        val activeSoundId: Int
            get() = when (this.soundType) {
                SoundSampleType.LOW -> lowSoundId
                SoundSampleType.MEDIUM -> mediumSoundId
                SoundSampleType.HIGH -> highSoundId
            }

        val activeSoundStreamId: Int
            get() = when (this.soundType) {
                SoundSampleType.LOW -> checkNotNull(lowSoundStreamId)
                SoundSampleType.MEDIUM -> checkNotNull(mediumSoundStreamId)
                SoundSampleType.HIGH -> checkNotNull(highSoundStreamId)
            }

        var isPlaying: Boolean = false
            internal set

        var soundType: SoundSampleType = defaultSoundType
            set(value) {
                if (field == value) {
                    return
                }

                this.composition.replaceSound(this, when (value) {
                    SoundSampleType.LOW -> lowSoundId
                    SoundSampleType.MEDIUM -> mediumSoundId
                    SoundSampleType.HIGH -> highSoundId
                })

                field = value
            }

        internal var lowSoundStreamId: Int? = null
        internal var mediumSoundStreamId: Int? = null
        internal var highSoundStreamId: Int? = null

        internal var entity: Entity? = null

        fun play() {
            this.composition.playSound(this)
        }

        fun stop() {
            this.composition.stopSound(this)
        }

        override fun onAttach(entity: Entity): Boolean {
            this.entity = entity
            this.composition._unattachedComponents.update { x -> x - 1 }
            return true
        }

        override fun onDetach(entity: Entity) {
            // todo - currently would not be robust to detachment
            this.composition._unattachedComponents.update { x -> x + 1 }
            stop()
            this.entity = null
        }
    }

    // private var callCount: Int = 0
    // private var streamId: Int? = 0

    private fun playSound(component: SoundCompositionComponent) {
        synchronized(this) {
            this.play() // hack

            component.isPlaying = true
            // if (component.activeSoundStreamId != null) {
                this.soundPoolManager.setVolume(component.activeSoundStreamId, 1.0f)
                /* if (callCount == 0) {
                    streamId = this.soundPoolManager.playSound(session, component.entity!!, component.activeSoundId, 1.0f, true)
                    this.soundPoolManager.setVolume(streamId!!, 0.0f)
                } else if (callCount % 2 == 1) {
                    this.soundPoolManager.setVolume(streamId!!, 1.0f)
                } else {
                    this.soundPoolManager.setVolume(streamId!!, 0.0f)
                }
                ++callCount */
            // }
        }
    }

    private fun stopSound(component: SoundCompositionComponent) {
        synchronized(this) {
            component.isPlaying = false
            this.soundPoolManager.setVolume(component.activeSoundStreamId, 0.0f)
        }
    }

    private fun replaceSound(component: SoundCompositionComponent, newSoundStreamId: Int?) {
        // HACK : Multiple streams can not be associated with a single entity
        //        For initial check in, just throw if someone tries to swap the sound by changing volume.
        // throw IllegalStateException("Multiple streams can not be associated with a single entity. Todo- fix")

        synchronized(this) {
            this.soundPoolManager.setVolume(component.activeSoundStreamId, 0.0f)
            if (newSoundStreamId != null) {
                this.soundPoolManager.setVolume(newSoundStreamId, if (component.isPlaying) 1.0f else 0.0f)
            }
        }
    }

    fun addComponent(lowSoundId: Int, mediumSoundId: Int, highSoundId: Int,
                     defaultSoundType: SoundSampleType = SoundSampleType.MEDIUM): SoundCompositionComponent {
        synchronized(this) {
            if (this.isPlaying || this.soundsInitialized) {
                throw IllegalStateException("Tried to add an component after play() was called.")
            }

            val component = SoundCompositionComponent(
                this, lowSoundId, mediumSoundId, highSoundId, defaultSoundType)

            this._unattachedComponents.update { x -> x + 1 }
            this.compositionComponents.add(component)

            return component
        }
    }

    private fun initializeSounds() {
        if (this.soundsInitialized) {
            return
        }

        // Start playing all sounds at the same time.
        for (compositionComponent in compositionComponents) {
            val entity = compositionComponent.entity

            if (entity == null) {
                throw IllegalStateException("Tried to initialize sound on a component that was not attached to an entity.")
            }

            // TODO - Multiple streams can not be associated with a single entity. Need to fix.
            val lowVolume = if (isPlaying && compositionComponent.isPlaying &&
                compositionComponent.soundType == SoundSampleType.LOW) 1.0f else 0.0f
            compositionComponent.lowSoundStreamId = checkNotNull(soundPoolManager.playSound(
                session,
                entity,
                compositionComponent.lowSoundId,
                volume = 1.0f,
                loop = true))
            soundPoolManager.setVolume(compositionComponent.lowSoundStreamId!!, lowVolume)

            val mediumVolume = if (isPlaying && compositionComponent.isPlaying &&
                compositionComponent.soundType == SoundSampleType.MEDIUM) 1.0f else 0.0f
            compositionComponent.mediumSoundStreamId = checkNotNull(soundPoolManager.playSound(
                session,
                entity,
                compositionComponent.mediumSoundId,
                volume = 1.0f,
                loop = true))
            soundPoolManager.setVolume(compositionComponent.mediumSoundStreamId!!, mediumVolume)

            // TODO - Multiple streams can not be associated with a single entity. Need to fix.
            val highVolume = if (isPlaying && compositionComponent.isPlaying &&
                compositionComponent.soundType == SoundSampleType.HIGH) 1.0f else 0.0f
            compositionComponent.highSoundStreamId = checkNotNull(soundPoolManager.playSound(
                session,
                entity,
                compositionComponent.highSoundId,
                volume = 1.0f,
                loop = true))
            soundPoolManager.setVolume(compositionComponent.highSoundStreamId!!, highVolume)
        }

        this.soundsInitialized = true
    }

    fun play() {
        synchronized(this) {
            if (this.isPlaying) {
                return
            }

            this.isPlaying = true

            if (!this.soundsInitialized) {
                initializeSounds()
                return // calling initialize sounds will set volumes appropriately, no need to continue.
            }

            for (compositionComponent in compositionComponents) {
                this.soundPoolManager.setVolume(
                    compositionComponent.activeSoundStreamId,
                    if (this.isPlaying && compositionComponent.isPlaying) 1.0f else 0.0f
                )
            }
        }
    }

    fun pause() {
        stop()
    }

    fun stop() {
        synchronized(this) {
            if (!this.isPlaying) {
                return
            }

            this.isPlaying = false

            for (compositionComponent in this.compositionComponents) {
                this.soundPoolManager.setVolume(compositionComponent.activeSoundStreamId, 0.0f)
            }
        }
    }
}