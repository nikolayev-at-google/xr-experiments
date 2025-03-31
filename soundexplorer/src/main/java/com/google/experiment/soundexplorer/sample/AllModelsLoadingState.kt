package com.google.experiment.soundexplorer.sample

import androidx.xr.scenecore.Model
import com.google.experiment.soundexplorer.core.GlbModel

sealed class AllModelsLoadingState {
    object NotStarted : AllModelsLoadingState() // Initial state before triggering load
    object LoadingInitiated : AllModelsLoadingState()
    data class InProgress(val loadedCount: Int, val totalCount: Int) : AllModelsLoadingState()
    data class Ready(val models: Map<GlbModel, Model>) : AllModelsLoadingState()
    data class Error(val errors: Map<GlbModel, Throwable>) : AllModelsLoadingState()
}