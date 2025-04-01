@file:Suppress("SpellCheckingInspection", "unused")

package com.google.experiment.soundexplorer.vm

import androidx.lifecycle.ViewModel
import androidx.xr.runtime.math.Vector3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class SoundExplorerViewModel : ViewModel() {

    private val _slidersValues = MutableStateFlow<Vector3>(Vector3.Zero + Vector3(-0.2f, -0.4f, 0f))
    val slidersValues: StateFlow<Vector3> = _slidersValues.asStateFlow()

    private val _addShapeMenuOpen = MutableStateFlow<Boolean>(false)
    val addShapeMenuOpen: StateFlow<Boolean> = _addShapeMenuOpen.asStateFlow()

    companion object {
        private const val TAG = "SoundExplorerViewModel"
    }

    fun onEditClick() {
//        TODO("Not yet implemented")
    }

    fun onDeleteAllClick() {
//        TODO("Not yet implemented")
    }

    fun onCloseAppClick() {
//        TODO("Not yet implemented")
    }

    fun onAddShapeClick() {
        _addShapeMenuOpen.value = !_addShapeMenuOpen.value
    }

    fun onPlayClick() {
//        TODO("Not yet implemented")
    }


    fun onLeftValueChange(value: Float) {
        _slidersValues.value = Vector3(value, 0f, 0f)
    }

    fun onBottomValueChange(value: Float) {
        _slidersValues.value = Vector3(0f, value, 0f)
    }

    fun onRightValueChange(value: Float) {
        _slidersValues.value = Vector3(0f, 0f, value)

    }
}