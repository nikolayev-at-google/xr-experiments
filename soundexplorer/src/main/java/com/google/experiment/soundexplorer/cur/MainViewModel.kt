package com.google.experiment.soundexplorer.cur

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.xr.runtime.math.Pose
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.sound.SoundComposition
import com.google.experiment.soundexplorer.sound.SoundExplorerSoundPool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel() {
    // UI state to track if the dialog is showing
    private val _isDialogHidden = MutableStateFlow(true)
    val isDialogHidden = _isDialogHidden.asStateFlow()

    private val _isModelsVisible = MutableStateFlow(false)
    val isModelsVisible = _isModelsVisible.asStateFlow()

    private val _soundComponentsInitialized = MutableStateFlow<Boolean>(false)
    val soundComponentsInitialized: StateFlow<Boolean> = _soundComponentsInitialized.asStateFlow()

    private val _toolbarPose = MutableStateFlow(Pose())
    val toolbarPose = _toolbarPose.asStateFlow()

    private var _soundPool: SoundExplorerSoundPool? = null
    val soundPool: SoundExplorerSoundPool
        get() { return checkNotNull(_soundPool) }

    private var _soundComposition: SoundComposition? = null
    val soundComposition: SoundComposition
        get() { return checkNotNull(_soundComposition) }

    private val _isSoundObjectsHidden = MutableStateFlow(true)
    val isSoundObjectsHidden = _isSoundObjectsHidden.asStateFlow()

    class DeleteAll(val value: Boolean = false)
    private val _deleteAll = MutableStateFlow(DeleteAll())
    val deleteAll = _deleteAll.asStateFlow()

    fun updateSoundObjectsVisibility(isHidden: Boolean) {
        _isSoundObjectsHidden.value = isHidden
    }

    // Action to show dialog
    fun showDialog() {
        _isDialogHidden.value = !_isDialogHidden.value
    }

    fun showModels() {
        _isModelsVisible.value = !_isModelsVisible.value
    }

    fun initializeSoundComposition(session: Session) {
        if (this._soundComponentsInitialized.value) {
            return
        }

        this._soundPool = SoundExplorerSoundPool(session.activity)
        this._soundComposition = SoundComposition(this.soundPool.manager, session)

        this._soundComponentsInitialized.value = true
    }

    fun setToolbarPose(pose: Pose) {
        _toolbarPose.value = pose
    }

    fun deleteAll() {
        _deleteAll.value = DeleteAll(true)
    }
}