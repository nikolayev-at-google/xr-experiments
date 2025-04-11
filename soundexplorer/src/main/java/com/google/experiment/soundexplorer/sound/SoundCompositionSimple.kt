package com.google.experiment.soundexplorer.sound

import androidx.xr.scenecore.Component
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SoundCompositionSimple (
    val soundPoolManager: SoundPoolManager,
    val session: Session) {

    enum class State {
        LOADING,    // object has been instantiated, but sounds are still loading or there are unattached components
        READY,      // all known sounds have been loaded and components have been initialized
        PLAYING,
        STOPPED
    }

    private val _state = MutableStateFlow<State>(State.LOADING)
    val state: StateFlow<State> = _state.asStateFlow()

    private var compositionComponents = mutableListOf<SoundCompositionComponent>()
    private var unattachedComponents = mutableSetOf<SoundCompositionComponent>()

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
        val activeSoundStreamId: Int
            get() = when (this.soundType) {
                SoundSampleType.LOW -> checkNotNull(lowSoundStreamId)
                SoundSampleType.MEDIUM -> checkNotNull(mediumSoundStreamId)
                SoundSampleType.HIGH -> checkNotNull(highSoundStreamId)
            }

        var isPlaying: Boolean = false
            internal set

        var soundType: SoundSampleType = defaultSoundType
            get() { synchronized(this) { return field } }
            set(value) {
                synchronized(this) {
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
            this.composition.attachComponent(this)
            return true
        }

        // Note! The current implementation relies on all sounds being played at once.
        // Thus, sound components may never be reattached after detachment.
        override fun onDetach(entity: Entity) {
            stop()
            this.composition.detachComponent(this)
            this.entity = null
        }
    }

    private fun playSound(component: SoundCompositionComponent) {
        synchronized(this) {
            initializeSounds() // ensure sounds are initialized
            component.isPlaying = true
            if (this._state.value == State.PLAYING) {
                this.soundPoolManager.setVolume(component.activeSoundStreamId, 1.0f)
            }
        }
    }

    private fun stopSound(component: SoundCompositionComponent) {
        synchronized(this) {
            initializeSounds() // ensure sounds are initialized
            component.isPlaying = false
            this.soundPoolManager.setVolume(component.activeSoundStreamId, 0.0f)
        }
    }

    private fun replaceSound(component: SoundCompositionComponent, newSoundStreamId: Int?) {
        synchronized(this) {
            initializeSounds() // ensure sounds are initialized
            this.soundPoolManager.setVolume(component.activeSoundStreamId, 0.0f)
            if (newSoundStreamId != null && this._state.value == State.PLAYING) {
                this.soundPoolManager.setVolume(newSoundStreamId, if (component.isPlaying) 1.0f else 0.0f)
            }
        }
    }

    private fun attachComponent(component: SoundCompositionComponent) {
        synchronized(this) {
            this.unattachedComponents.remove(component)
            if (this.unattachedComponents.isEmpty() && this._state.value == State.LOADING) {
                this._state.value = State.READY
            }
        }
    }

    private fun detachComponent(component: SoundCompositionComponent) {
        synchronized(this) {
            // currently when a component is detached, we just forget about it.
            // components can not be reattached once detached
            this.unattachedComponents.remove(component)
            this.compositionComponents.remove(component)
        }
    }

    fun addComponent(lowSoundId: Int, mediumSoundId: Int, highSoundId: Int,
                     defaultSoundType: SoundSampleType = SoundSampleType.MEDIUM): SoundCompositionComponent {
        synchronized(this) {
            if (this.state.value >= State.PLAYING) {
                throw IllegalStateException("Tried to add an component after play() was called.")
            }

            this._state.value = State.LOADING

            val component = SoundCompositionComponent(
                this, lowSoundId, mediumSoundId, highSoundId, defaultSoundType)

            this.unattachedComponents.add(component)
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

            val lowVolume = if (this._state.value == State.PLAYING && compositionComponent.isPlaying &&
                compositionComponent.soundType == SoundSampleType.LOW) 1.0f else 0.0f
            compositionComponent.lowSoundStreamId = checkNotNull(soundPoolManager.playSound(
                session,
                entity,
                compositionComponent.lowSoundId,
                volume = 1.0f,
                loop = true))
            soundPoolManager.setVolume(compositionComponent.lowSoundStreamId!!, lowVolume)

            val mediumVolume = if (this._state.value == State.PLAYING && compositionComponent.isPlaying &&
                compositionComponent.soundType == SoundSampleType.MEDIUM) 1.0f else 0.0f
            compositionComponent.mediumSoundStreamId = checkNotNull(soundPoolManager.playSound(
                session,
                entity,
                compositionComponent.mediumSoundId,
                volume = 1.0f,
                loop = true))
            soundPoolManager.setVolume(compositionComponent.mediumSoundStreamId!!, mediumVolume)

            val highVolume = if (this._state.value == State.PLAYING && compositionComponent.isPlaying &&
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
            if (this._state.value != State.READY && this._state.value != State.STOPPED) {
                return
            }

            this._state.value = State.PLAYING

            if (!this.soundsInitialized) {
                initializeSounds()
                return // calling initialize sounds will set volumes appropriately, no need to continue.
            }

            for (compositionComponent in compositionComponents) {
                this.soundPoolManager.setVolume(
                    compositionComponent.activeSoundStreamId,
                    if (compositionComponent.isPlaying) 1.0f else 0.0f
                )
            }
        }
    }

    fun pause() {
        stop()
    }

    fun stop() {
        synchronized(this) {
            if (this._state.value != State.PLAYING) {
                return
            }

            this._state.value = State.STOPPED

            for (compositionComponent in this.compositionComponents) {
                this.soundPoolManager.setVolume(compositionComponent.activeSoundStreamId, 0.0f)
            }
        }
    }
}