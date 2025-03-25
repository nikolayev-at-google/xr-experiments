package com.google.experiment.soundexplorer.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.xr.scenecore.Session as SceneCoreSession


@HiltViewModel
class SoundExplorerViewModel @Inject constructor(
    private val sceneCoreSession: SceneCoreSession
) : ViewModel() {

    init {
        // Enable Full space for this app
        sceneCoreSession.spatialEnvironment.requestFullSpaceMode()
    }
}