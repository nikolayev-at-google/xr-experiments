package com.google.experiment.soundexplorer.core

import androidx.xr.scenecore.Session as SceneCoreSession // Alias for clarity
import androidx.xr.scenecore.Model


/**
 * Interface for accessing 3D model data.
 * Abstracts the data source (SceneCoreSession, network, etc.) and caching.
 */
interface GlbModelRepository {
    /**
     * Initializes the repository with the necessary SceneCoreSession.
     * Must be called before attempting to load models.
     * Idempotent: subsequent calls with the same session should be safe.
     * @param session The active SceneCoreSession.
     */
//    fun initializeSession(session: SceneCoreSession)

    /**
     * Gets a model by its identifier, loading it if necessary.
     * Handles caching and concurrent requests.
     *
     * @param modelIdentifier Unique identifier (e.g., asset path "models/object_1.glb").
     * @return Result<Model> containing the loaded Model on success or an exception on failure.
     * @throws IllegalStateException if initializeSession() has not been called yet.
     */
    suspend fun getOrLoadModel(modelIdentifier: GlbModel): Result<Model>

    /**
     * Clears caches and releases resources held by the repository.
     * Should be called when the repository is no longer needed (e.g., ViewModel onCleared).
     */
    fun clear()
}