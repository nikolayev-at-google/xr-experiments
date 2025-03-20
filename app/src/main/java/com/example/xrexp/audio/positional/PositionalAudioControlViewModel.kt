package com.example.xrexp.audio.positional

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
import android.media.SoundPool
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.concurrent.futures.await
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.PointSourceAttributes
import androidx.xr.scenecore.Session
import androidx.xr.scenecore.SpatialSoundPool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class PositionalAudioControlViewModel : ViewModel() {

    companion object {
        private const val TAG = "PositionalAudioVM"
        private const val GLB_MODEL = "models/xyzArrows.glb"

        val soundResources = listOf(
            0 to "audio/mechanical_clock_ring.ogg",
            1 to "audio/media_sound_drums.ogg",
            2 to "audio/media_sound_guitar.ogg",
            3 to "audio/media_sound_perc.ogg"
        )

        val soundNames = listOf(
            "mechanical_clock_ring.ogg",
            "media_sound_drums.ogg",
            "media_sound_guitar.ogg",
            "media_sound_perc.ogg"
        )
    }

    private var soundPool: SoundPool? = null

    // Map of loaded sound IDs
    private val soundIds = mutableMapOf<Int, Int>()

    // Map to track if a sound is currently playing and its stream ID
    private val playingStreams = mutableMapOf<Int, Int>()

    // Map to track paused streams
    private val pausedStreams = mutableMapOf<Int, Int>()

    // State for UI updates
    private val _soundsState = MutableStateFlow<Map<Int, SoundState>>(emptyMap())
    val soundsState: StateFlow<Map<Int, SoundState>> = _soundsState.asStateFlow()

    // List of items for the dropdown
    private val _items = MutableStateFlow<List<String>>(emptyList())
    val items: StateFlow<List<String>> = _items.asStateFlow()

    // Currently selected item
    private val _selectedItem = MutableStateFlow("mechanical_clock_ring.ogg")
    val selectedItem: StateFlow<String> = _selectedItem.asStateFlow()

    // Expanded state of the dropdown
    private val _isDropdownExpanded = MutableStateFlow(false)
    val isDropdownExpanded: StateFlow<Boolean> = _isDropdownExpanded.asStateFlow()

    private val _uiState = mutableStateOf(PositionalAudioControlUiState())
    val uiState: State<PositionalAudioControlUiState> = _uiState

    private var _gltfModel : MutableStateFlow<GltfModel?> = MutableStateFlow(null)
    val gltfModel : StateFlow<GltfModel?> = _gltfModel.asStateFlow()

    private val _modelPose: MutableStateFlow<Pose> = MutableStateFlow(Pose())
    val modelPose : StateFlow<Pose> = _modelPose.asStateFlow()


    init {
        // Initialize with some data
        // In a real app, this might come from a repository or network call
        _items.value = soundNames

        initializeSoundPool()
    }

    override fun onCleared() {
        soundPool?.release()
        soundPool = null
        super.onCleared()
    }

    suspend fun loadModel(scenecoreSession : Session) {
        Log.i(TAG, "loadModel($scenecoreSession)")

        _gltfModel.value = GltfModel.create(scenecoreSession, GLB_MODEL).await()
    }

    fun onAngleChanged(value: Float) {
        _uiState.value = _uiState.value.copy(angle = value)
        rotateAndTranslate()
    }

    fun onDistanceChanged(value: Float) {
        _uiState.value = _uiState.value.copy(distance = value)
        rotateAndTranslate()
    }

    fun onLoopChanged(checked: Boolean) {
        _uiState.value = _uiState.value.copy(loop = checked)
    }

    fun onPlayClicked() {
        _uiState.value = _uiState.value.copy(
            isPlaying = true,
            slidersEnabled = true,
            showDialog = true
        )
    }

    fun onStopClicked() {
        _uiState.value = _uiState.value.copy(
            isPlaying = false,
            slidersEnabled = false,
            angle = 0f,
            distance = 0f,
            showDialog = false
        )
        _modelPose.value = Pose()
    }

    fun onItemSelected(item: String) {
        _selectedItem.value = item
        _isDropdownExpanded.value = false
    }

    fun onDropdownExpandedChange(expanded: Boolean) {
        _isDropdownExpanded.value = expanded
    }

    fun loadSounds(context: Context) {
        viewModelScope.launch {
            soundResources.forEach { (index, assetName) ->
                val soundEffect = context.assets.openFd(assetName)
                _soundsState.value += (index to SoundState(name = assetName))
                soundPool?.let { pool ->
                    val pointSoundId = pool.load(soundEffect, 1)
                    soundIds[index] = pointSoundId
                }
            }
        }
    }

    fun playSound(xrSession : Session, entity: Entity, soundIndex: Int) {

        soundIds[soundIndex] ?: return

        /**
         *
         */
        val maxVolume = 1F
        val lowPriority = 0
        val infiniteLoop = -1
        val normalSpeed = 1F
        val pointSource = PointSourceAttributes(entity)

        soundPool?.let { pool ->
            // Stop if already playing
            if (playingStreams.containsKey(soundIndex)) {
                pool.stop(playingStreams[soundIndex] ?: 0)
            }

            // Play sound
            val streamId = SpatialSoundPool.play(
                session = xrSession,
                soundPool = pool,
                soundID = soundIndex,
                attributes = pointSource,
                volume = maxVolume,
                priority = lowPriority,
                loop = infiniteLoop,
                rate = normalSpeed
            )

            if (streamId > 0) {
                playingStreams[soundIndex] = streamId
                pausedStreams.remove(soundIndex)
                updateSoundState(soundIndex, isPlaying = true, isPaused = false)
            }
        }
    }

    fun pauseSound(soundIndex: Int) {
        val streamId = playingStreams[soundIndex] ?: return

        soundPool?.let { pool ->
            pool.pause(streamId)
            pausedStreams[soundIndex] = streamId
            playingStreams.remove(soundIndex)
            updateSoundState(soundIndex, isPlaying = false, isPaused = true)
        }
    }

    fun resumeSound(soundIndex: Int) {
        val streamId = pausedStreams[soundIndex] ?: return

        soundPool?.let { pool ->
            pool.resume(streamId)
            playingStreams[soundIndex] = streamId
            pausedStreams.remove(soundIndex)
            updateSoundState(soundIndex, isPlaying = true, isPaused = false)
        }
    }

    fun stopSound(soundIndex: Int) {
        val streamId = playingStreams[soundIndex] ?: pausedStreams[soundIndex] ?: return

        soundPool?.let { pool ->
            pool.stop(streamId)
            playingStreams.remove(soundIndex)
            pausedStreams.remove(soundIndex)
            updateSoundState(soundIndex, isPlaying = false, isPaused = false)
        }
    }

    private fun initializeSoundPool() {

        // Configure audio attributes
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(CONTENT_TYPE_SONIFICATION)
            .setUsage(USAGE_ASSISTANCE_SONIFICATION)
            .build()

        // Create SoundPool
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        // Set load listener
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            val soundIndex = soundIds.entries.find { it.value == sampleId }?.key ?: return@setOnLoadCompleteListener
            if (status == 0) { // 0 means success
                updateSoundState(soundIndex, isLoaded = true)
            }
        }
    }

    private fun updateSoundState(
        soundIndex: Int,
        isLoaded: Boolean? = null,
        isPlaying: Boolean? = null,
        isPaused: Boolean? = null
    ) {
        Log.d(TAG, "updateSoundState($soundIndex, $isLoaded, $isPlaying, $isPaused)")

        val currentState = _soundsState.value[soundIndex] ?: return
        val newState = currentState.copy(
            isLoaded = isLoaded ?: currentState.isLoaded,
            isPlaying = isPlaying ?: currentState.isPlaying,
            isPaused = isPaused ?: currentState.isPaused
        )
        _soundsState.value += (soundIndex to newState)
    }

    private fun rotateAndTranslate() {
        _modelPose.value = Pose.Identity
            .rotate(Quaternion.fromEulerAngles(0f, _uiState.value.angle, 0f))
            .translate(Vector3(0f, 0f, -_uiState.value.distance))
    }

}