package com.google.experiment.soundexplorer.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SoundExplorerViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val TAG = "SoundExplorerVM"
    }


    fun onEraserClick() {
        // TODO
        Log.d(TAG, "onEraserClick: ")
    }

    fun onClearAllClick() {
        // TODO
        Log.d(TAG, "onClearAllClick: ")
    }

    fun onPlayClick() {
        // TODO
        Log.d(TAG, "onPlayClick: ")

    }
}