@file:Suppress("SpellCheckingInspection", "unused")

package com.google.experiment.soundexplorer.sample

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.xr.scenecore.Model
import com.google.experiment.soundexplorer.core.GlbModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.sample.AllModelsLoadingState


class SoundExplorerViewModel(
    private val modelRepository: GlbModelRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ModelCacheViewModel"
    }

    // StateFlow remains the same
    private val _loadingState = MutableStateFlow<AllModelsLoadingState>(AllModelsLoadingState.NotStarted)
    val loadingState: StateFlow<AllModelsLoadingState> = _loadingState.asStateFlow()

    // List of models to load remains the same
    private val requiredModelIdentifiers = GlbModel.allGlbModels

    // Flag to prevent multiple load triggers
    private var loadingTriggered = false

    /**
     * Triggers the loading process for all required models using the repository.
     * Should be called once the repository has been initialized with the session.
     */
    fun triggerModelLoading() {
        if (loadingTriggered || _loadingState.value !is AllModelsLoadingState.NotStarted) {
            Log.w(TAG, "Loading already triggered or not in NotStarted state.")
            return
        }
        loadingTriggered = true
        _loadingState.value = AllModelsLoadingState.LoadingInitiated
        Log.i(TAG, "Triggering loading of ${requiredModelIdentifiers.size} models via Repository.")

        viewModelScope.launch {
            loadAllModelsFromRepository()
        }
    }

    private suspend fun loadAllModelsFromRepository() {
        val totalModels = requiredModelIdentifiers.size
        _loadingState.value = AllModelsLoadingState.InProgress(0, totalModels)

        val results = mutableMapOf<GlbModel, Result<Model>>()
        val jobs = mutableListOf<Job>()

        Log.d(TAG, "Launching parallel load jobs via repository...")
        // Use coroutineScope for structured concurrency
        try {
            coroutineScope {
                for (identifier in requiredModelIdentifiers) {
                    jobs += launch { // Launch each retrieval job
                        Log.d(TAG, "Requesting model '$identifier' from repository.")
                        // Call the repository's suspend function
                        val result = modelRepository.getOrLoadModel(identifier)
                        results[identifier] = result // Store the Result<Model>

                        // Update progress based on successful loads only
                        val currentSuccessCount = results.count { it.value.isSuccess }
                        _loadingState.value = AllModelsLoadingState.InProgress(currentSuccessCount, totalModels)
                        Log.d(TAG, "Result for '$identifier': ${if(result.isSuccess) "Success" else "Failure"}")
                    }
                }
            } // coroutineScope waits for all launched jobs

            Log.d(TAG, "All repository load jobs completed. Analyzing results.")
            // Analyze results after all jobs complete
            val successfulModels = results.filterValues { it.isSuccess }.mapValues { it.value.getOrThrow() }
            val failedLoads = results.filterValues { it.isFailure }.mapValues { it.value.exceptionOrNull()!! }

            if (failedLoads.isNotEmpty()) {
                Log.e(TAG, "Errors occurred during model loading via repository: ${failedLoads.keys}")
                _loadingState.value = AllModelsLoadingState.Error(failedLoads)
            } else if (successfulModels.size == totalModels) {
                Log.i(TAG, "All $totalModels models loaded successfully via repository.")
                _loadingState.value = AllModelsLoadingState.Ready(successfulModels)
            } else {
                Log.e(TAG, "Inconsistent state after loading from repository. Success: ${successfulModels.size}, Failures: ${failedLoads.size}")
                _loadingState.value = AllModelsLoadingState.Error(
                    mapOf(GlbModel.Fake to IllegalStateException("Mismatch in loaded model count"))
                )
            }

        } catch (e: CancellationException) {
            Log.w(TAG, "Model loading cancelled.", e)
            _loadingState.value = AllModelsLoadingState.Error(mapOf(GlbModel.Fake to e))
        } catch (e: Exception) {
            // Catch unexpected errors during orchestration
            Log.e(TAG, "Critical error during model loading orchestration", e)
            _loadingState.value = AllModelsLoadingState.Error(mapOf(GlbModel.Fake to e))
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "ViewModel cleared. Instructing repository to clear.")
        // Delegate cleanup to the repository
        // This assumes the Repository might be shared (Singleton) and shouldn't be fully cleared
        // just because one ViewModel is destroyed. If the repo is scoped to the ViewModel,
        // then clearing makes more sense. Adjust based on your DI scoping.
        // A more robust approach might involve reference counting in the repo if it's a Singleton.
        // For simplicity here, we'll call clear.
        modelRepository.clear()
    }

    fun onEraserClick() {
        TODO("Not yet implemented")
    }

    fun onClearAllClick() {
        TODO("Not yet implemented")
    }

    fun onPlayClick() {
        TODO("Not yet implemented")
    }
}