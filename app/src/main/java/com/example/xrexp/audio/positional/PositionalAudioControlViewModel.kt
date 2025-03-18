package com.example.xrexp.audio.positional

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.concurrent.futures.await
import androidx.lifecycle.ViewModel
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class PositionalAudioControlViewModel : ViewModel() {

    companion object {
        private const val TAG = "PositionalAudioVM"
        private const val GLB_MODEL = "models/xyzArrows.glb"
    }

    private val _uiState = mutableStateOf(PositionalAudioControlUiState())
    val uiState: State<PositionalAudioControlUiState> = _uiState

    private val _distance: MutableStateFlow<Float> = MutableStateFlow(0f)
    val distance : StateFlow<Float> = _distance.asStateFlow()

    private var _gltfModel : MutableStateFlow<GltfModel?> = MutableStateFlow(null)
    val gltfModel : StateFlow<GltfModel?> = _gltfModel.asStateFlow()


    suspend fun loadModel(scenecoreSession : Session) {
        Log.i(TAG, "loadModel($scenecoreSession)")

        _gltfModel.value = GltfModel.create(scenecoreSession, GLB_MODEL).await()



    }

    fun onAngleChanged(value: Float) {
        _uiState.value = _uiState.value.copy(angle = value)
    }

    fun onDistanceChanged(value: Float) {
        _distance.value = value
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
            showDialog = false
        )
        _distance.value  = 0f
    }

    fun onDismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false)
    }
}