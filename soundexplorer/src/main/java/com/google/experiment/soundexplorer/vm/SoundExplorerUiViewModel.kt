@file:Suppress("SpellCheckingInspection", "unused")

package com.google.experiment.soundexplorer.vm

import android.util.Log
import androidx.concurrent.futures.await
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.xr.compose.subspace.layout.PoseChangeEvent
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.Model
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.sound.SoundCompositionSimple
import com.google.experiment.soundexplorer.sound.SoundExplorerSoundPool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SoundExplorerUiViewModel @Inject constructor() : ViewModel() {

    private val _addShapeMenuOpen = MutableStateFlow<Boolean>(false)
    val addShapeMenuOpen: StateFlow<Boolean> = _addShapeMenuOpen.asStateFlow()

    private val _modelsMap = MutableStateFlow<Map<GlbModel, GltfModel?>>(mutableMapOf())
    val modelsMap: StateFlow<Map<GlbModel, GltfModel?>> = _modelsMap.asStateFlow()

    private val _soundComponentsInitialized = MutableStateFlow<Boolean>(false)
    private var _soundPool: SoundExplorerSoundPool? = null
    private var _soundComposition: SoundCompositionSimple? = null

    val soundComponentsInitialized: StateFlow<Boolean> = _soundComponentsInitialized.asStateFlow()

    val soundPool: SoundExplorerSoundPool
        get() { return checkNotNull(_soundPool) }

    val soundComposition: SoundCompositionSimple
        get() { return checkNotNull(_soundComposition) }

    companion object {
        private const val TAG = "SoundExplorerViewModel"
    }

    fun initializeSoundComponents(session: Session) {
        if (this._soundComponentsInitialized.value) {
            return
        }

        this._soundPool = SoundExplorerSoundPool(session.activity)
        this._soundComposition = SoundCompositionSimple(this.soundPool.manager, session)

        this._soundComponentsInitialized.value = true
    }

    // val models:

    fun initialize(session : Session, glbModel: GlbModel) {
        viewModelScope.launch {
            val shape = GltfModel.create(session, glbModel.assetName).await()
            _modelsMap.value = _modelsMap.value.plus(Pair(glbModel, shape))
        }
    }


    fun onEditClick() {
    }

    fun onDeleteAllClick() {
    }

    fun onCloseAppClick() {
    }

    fun onAddShapeClick() {
        _addShapeMenuOpen.value = !_addShapeMenuOpen.value
    }

    fun onPlayClick() {
    }

    fun onModelPoseChange(glbModel : GlbModel, poseEvent: PoseChangeEvent) {
        Log.d(TAG, "${glbModel} - poseEvent: ${poseEvent.pose}")
    }

}