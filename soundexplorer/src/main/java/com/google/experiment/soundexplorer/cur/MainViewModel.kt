package com.google.experiment.soundexplorer.cur

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    // Action to show dialog
    fun showDialog() {
        _isDialogVisible.value = !_isDialogVisible.value
        _isModelsVisible.value = false
    }

    fun showModels() {
        _isModelsVisible.value = !_isModelsVisible.value
        _isDialogVisible.value = false
    }
}