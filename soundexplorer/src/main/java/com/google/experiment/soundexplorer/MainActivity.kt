@file:Suppress("SpellCheckingInspection")

package com.google.experiment.soundexplorer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.viewModels
import androidx.xr.runtime.SessionCreatePermissionsNotGranted
import androidx.xr.runtime.SessionCreateSuccess
import androidx.xr.runtime.SessionResumePermissionsNotGranted
import androidx.xr.runtime.SessionResumeSuccess
import androidx.xr.scenecore.Session as SceneCoreSession
import androidx.xr.runtime.Session as ARCoreSession
import com.google.experiment.soundexplorer.ui.SoundExplorerMainScreen
import com.google.experiment.soundexplorer.vm.SoundExplorerViewModel
import kotlin.getValue
import com.google.experiment.soundexplorer.core.GlbModelRepository


class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.all { it.value }
            if (!allPermissionsGranted) {
                Log.e(TAG, "Required permissions were not granted.")
                Toast.makeText(this, "Required permissions were not granted.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Log.i(TAG, "Permissions granted, recreating activity.")
                recreate() // Recreate to retry session creation
            }
        }

    // Lazy init for SceneCoreSession remains
    private val scenecoreSession: SceneCoreSession by lazy {
        Log.d(TAG, "Creating SceneCoreSession (lazy init).")
        SceneCoreSession.create(this)
    }
    private lateinit var arCoreSession: ARCoreSession

    // Get ViewModel via delegate (Hilt handles injection if @AndroidEntryPoint and @HiltViewModel used)
    private val viewModel: SoundExplorerViewModel by viewModels()

    // // TODO: Inject the ModelRepository using Hilt/Dagger
    // @Inject
    lateinit var modelRepository: GlbModelRepository // Needs DI setup

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started.")

        // // TODO: If using manual DI, initialize modelRepository here or in Application class
        // // Example manual Singleton (simple version, not thread-safe for init)
        // if (!::modelRepository.isInitialized) {
        //     modelRepository = ModelRepositoryImpl(Dispatchers.IO) // Provide dispatcher
        // }


        // Attempt to create ARCore session
        when (val result = ARCoreSession.create(this)) {
            is SessionCreateSuccess -> {
                arCoreSession = result.session
                Log.i(TAG, "ARCoreSession created successfully.")

                // Initialize Repository with the session
                try {
                    // Ensure repository is initialized before using it
                    if (!::modelRepository.isInitialized) {
                        throw IllegalStateException("ModelRepository has not been initialized/injected.")
                    }
                    modelRepository.initializeSession(scenecoreSession)
                    Log.d(TAG, "ModelRepository initialized with SceneCoreSession.")

                    // Trigger loading in ViewModel *after* repository is initialized
                    viewModel.triggerModelLoading()
                    Log.d(TAG, "Triggered model loading in ViewModel.")

                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing repository or triggering viewmodel load", e)
                    // Handle critical error - maybe finish activity or show error state
                    Toast.makeText(this, "Initialization Error: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                    return // Don't set content if init failed
                }

                // Set content only after successful session creation and repo init attempt
                setContent {
                    Log.d(TAG, "Setting Composable content.")
                    SoundExplorerMainScreen(viewModel = viewModel)
                }
            }
            is SessionCreatePermissionsNotGranted -> {
                Log.w(TAG, "ARCore permissions not granted. Requesting...")
                requestPermissionLauncher.launch(result.permissions.toTypedArray())
                // Do not set content here; Activity will likely be recreated
            }
            else -> {
                Log.e(TAG, "Error creating ARCore session: ${result}")
                Toast.makeText(this, "Failed to create AR session: ${result}", Toast.LENGTH_LONG).show()
                finish() // Close on critical error
            }
        }
    }

    // onResume, onPause, onDestroy remain largely the same, ensuring arCoreSession is managed.
    override fun onResume() {
        super.onResume()
        if (!this::arCoreSession.isInitialized) {
            Log.d(TAG, "onResume: ARCoreSession not initialized yet.")
            return
        }
        Log.d(TAG, "onResume: Resuming ARCoreSession.")
        when (val result = arCoreSession.resume()) {
            is SessionResumeSuccess -> Log.i(TAG, "ARCoreSession resumed successfully.")
            is SessionResumePermissionsNotGranted -> {
                Log.w(TAG, "Permissions required for resume. Requesting...")
                requestPermissionLauncher.launch(result.permissions.toTypedArray())
            }
            else -> Log.e(TAG, "Error resuming ARCore session")
        }
    }

    override fun onPause() {
        super.onPause()
        if (!this::arCoreSession.isInitialized) {
            return
        }
        Log.d(TAG, "onPause: Pausing ARCoreSession.")
        arCoreSession.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::arCoreSession.isInitialized) {
            Log.d(TAG, "onDestroy: Destroying ARCoreSession.")
            arCoreSession.destroy()
        }
        // Note: ViewModel onCleared will call repository.clear()
        Log.d(TAG, "onDestroy: Activity destroyed.")
    }
}