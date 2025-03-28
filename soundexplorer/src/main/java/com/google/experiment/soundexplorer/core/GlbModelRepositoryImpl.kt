package com.google.experiment.soundexplorer.core

import android.util.Log
import androidx.xr.scenecore.Model
import androidx.xr.scenecore.Session as SceneCoreSession // Alias for clarity
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject // For DI frameworks like Hilt/Dagger
import kotlin.Result // Use kotlin.Result for success/failure
import com.google.experiment.soundexplorer.ext.loadGltfModel


class GlbModelRepositoryImpl @Inject constructor(
    // Inject the IO dispatcher for background loading tasks
    private val ioDispatcher: CoroutineDispatcher
) : GlbModelRepository {

    companion object {
        private const val TAG = "ModelRepImpl"
    }

    private lateinit var scenecoreSession: SceneCoreSession

    // Cache for ongoing loading jobs (Deferred ensures only one load per identifier)
    private val loadingJobs = ConcurrentHashMap<GlbModel, Deferred<Result<Model>>>()
    // Cache for successfully loaded models
    private val loadedModels = ConcurrentHashMap<GlbModel, Model>()

    override fun initializeSession(session: SceneCoreSession) {
        if (this.scenecoreSession !== session) {
            Log.w(TAG, "Repository already initialized with a different session. Ignoring new session.")
            // Or decide on a different strategy, like resetting caches if the session changes.
        } else {
            this.scenecoreSession = session
            Log.i(TAG, "SceneCoreSession initialized in Repository.")
        }
    }

    override suspend fun getOrLoadModel(modelIdentifier: GlbModel): Result<Model> {
        // 1. Check memory cache for already loaded models
        loadedModels[modelIdentifier]?.let {
            Log.d(TAG, "Model '$modelIdentifier' found in memory cache.")
            return Result.success(it)
        }

        // 2. Check cache for ongoing loading jobs
        // computeIfAbsent ensures atomic creation and retrieval of the Deferred job
        val loadingJob = loadingJobs.computeIfAbsent(modelIdentifier) { identifier ->

            Log.d(TAG, "No existing job or cache for '$identifier'. Creating new loading job.")
            // Launch the loading coroutine within the repository's scope (using viewModelScope indirectly via caller)
            // Or, if Repository is a Singleton, it might need its own CoroutineScope.
            // Here, we assume the caller (ViewModel) provides the scope.
            // We use SupervisorJob to prevent one failure from cancelling others if called within a larger scope.
            CoroutineScope(ioDispatcher + SupervisorJob()).async {
                try {
                    Log.d(TAG, "Calling session.loadGltfModel for URI: $modelIdentifier")

                    // Call the actual suspend loading function (extension function assumed)
                    val model = scenecoreSession.loadGltfModel(modelIdentifier.assetName)
                        ?: throw RuntimeException("SceneCoreSession loadGltfModel returned null for '$identifier'")

                    // Cache the successfully loaded model
                    loadedModels[identifier] = model
                    Log.i(TAG, "Model '$identifier' loaded and cached successfully.")
                    Result.success(model)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load model '$identifier'", e)
                    Result.failure(e) // Propagate the error
                } finally {
                    // Remove the job from the map once it's completed (success or failure)
                    loadingJobs.remove(identifier)
                    Log.d(TAG, "Loading job removed for '$identifier'.")
                }
            }

        }

        // 3. Await the result of the (potentially new) loading job
        Log.d(TAG, "Awaiting loading job result for '$modelIdentifier'.")
        return loadingJob.await()
    }

    override fun clear() {
        Log.i(TAG, "Clearing repository. Cancelling ${loadingJobs.size} jobs, clearing ${loadedModels.size} cached models.")
        // Cancel any ongoing loading jobs
        loadingJobs.values.forEach { it.cancel("Repository cleared") }
        loadingJobs.clear()

        // Release resources associated with loaded models (if required by SceneCore API)
        loadedModels.values.forEach { model ->
            try {
                // // TODO: Check SceneCore API - Is explicit release needed for Model objects?
                // model.release()
                Log.d(TAG,"Resources for cached model $model potentially released (if needed).")
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing model resources for $model", e)
            }
        }
        loadedModels.clear()
        Log.i(TAG, "Repository cleared.")
    }
}