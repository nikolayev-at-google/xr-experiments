package com.google.experiment.soundexplorer.cur

import androidx.lifecycle.ViewModel
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
    private val _isDialogVisible = MutableStateFlow(false)
    val isDialogVisible = _isDialogVisible.asStateFlow()

    private val _isModelsVisible = MutableStateFlow(false)
    val isModelsVisible = _isModelsVisible.asStateFlow()

    private val _soundComponentsInitialized = MutableStateFlow<Boolean>(false)
    val soundComponentsInitialized: StateFlow<Boolean> = _soundComponentsInitialized.asStateFlow()

    private var _soundPool: SoundExplorerSoundPool? = null
    val soundPool: SoundExplorerSoundPool
        get() { return checkNotNull(_soundPool) }

    private var _soundComposition: SoundComposition? = null
    val soundComposition: SoundComposition
        get() { return checkNotNull(_soundComposition) }

    // Action to show dialog
    fun showDialog() {
        _isDialogVisible.value = !_isDialogVisible.value
        _isModelsVisible.value = false
    }

    fun showModels() {
        _isModelsVisible.value = !_isModelsVisible.value
        _isDialogVisible.value = false
    }

    fun initializeSoundComposition(session: Session) {
        if (this._soundComponentsInitialized.value) {
            return
        }

        this._soundPool = SoundExplorerSoundPool(session.activity)
        this._soundComposition = SoundComposition(this.soundPool.manager, session)

        this._soundComponentsInitialized.value = true
    }
}