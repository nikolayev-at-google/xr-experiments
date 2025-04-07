@file:Suppress("SpellCheckingInspection", "unused")

package com.google.experiment.soundexplorer.vm

import androidx.concurrent.futures.await
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.Model
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.core.GlbModel
import dagger.hilt.android.lifecycle.HiltViewModel
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

    companion object {
        private const val TAG = "SoundExplorerViewModel"
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

}