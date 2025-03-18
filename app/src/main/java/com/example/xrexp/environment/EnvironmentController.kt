package com.example.xrexp.environment

import android.util.Log
import androidx.concurrent.futures.await
import androidx.xr.scenecore.ExrImage
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.JxrPlatformAdapter
import androidx.xr.scenecore.Session
import androidx.xr.scenecore.SpatialEnvironment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class EnvironmentController(
    private val xrSession: Session,
    private val coroutineScope: CoroutineScope
) {

    companion object {
        private const val TAG = "EnvironmentController"
    }

    private val assetCache: HashMap<String, Any> = HashMap()
    private var activeEnvironmentModelName: String? = null

    fun requestHomeSpaceMode() = xrSession.spatialEnvironment.requestHomeSpaceMode()

    fun requestFullSpaceMode() = xrSession.spatialEnvironment.requestFullSpaceMode()

    fun requestPassthrough(
        passthroughOpacityPreference: Float?
    ) = xrSession.spatialEnvironment.setPassthroughOpacityPreference(passthroughOpacityPreference)

    /**
     * Request the system load a custom Environment
     */
    fun requestCustomEnvironment(
        environmentModelName: String,
        environmentImageName: String? = null
    ) {
        coroutineScope.launch {
            try {

                if (activeEnvironmentModelName == null ||
                    activeEnvironmentModelName != environmentModelName
                    ) {

                    val environmentModel = assetCache[environmentModelName] as GltfModel

                    var environmentImage : ExrImage? = null
                    if (environmentImageName != null) {
                        environmentImage = assetCache[environmentImageName] as ExrImage?
                    }

                    SpatialEnvironment.SpatialEnvironmentPreference(
                        skybox = environmentImage,
                        geometry = environmentModel
                    ).let {
                        xrSession.spatialEnvironment.setSpatialEnvironmentPreference(
                            it
                        )
                    }

                    activeEnvironmentModelName = environmentModelName
                }
                xrSession.spatialEnvironment.setPassthroughOpacityPreference(0f)

            } catch (e: Exception){
                Log.e(TAG, "Failed to update Environment Preference for $environmentModelName: $e")
            }

        }
    }

    fun loadModelAsset(modelName: String) {
        coroutineScope.launch {
            // load the asset if it hasn't been loaded previously
            if (!assetCache.containsKey(modelName)) {
                try {
                    val gltfModel = GltfModel.create(xrSession, modelName).await()

                    assetCache[modelName] = gltfModel

                }catch (e: Exception) {
                    Log.e(TAG, "Failed to load model for $modelName: $e")
                }
            }
        }
    }

    fun loadImageAsset(imageName: String) {
        coroutineScope.launch {
            // load the asset if it hasn't been loaded previously
            if (!assetCache.containsKey(imageName)) {
                try {
                    val exrImage = ExrImage.create(xrSession, imageName)
                    assetCache[imageName] = exrImage
                 }catch (e: Exception) {
                    Log.e(TAG, "Failed to load ExrImage for $imageName: $e")
                }
            }
        }
    }
}
