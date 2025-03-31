package com.google.experiment.soundexplorer.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.concurrent.futures.await
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.xr.arcore.Hand
import androidx.xr.arcore.HandJointType
import androidx.xr.runtime.SessionCreatePermissionsNotGranted
import androidx.xr.runtime.SessionCreateSuccess
import androidx.xr.runtime.SessionResumePermissionsNotGranted
import androidx.xr.runtime.SessionResumeSuccess
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.scenecore.Session as SceneCoreSession
import androidx.xr.runtime.Session as ARCoreSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class HandMenuActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    // TODO: refactor
    private val scenecoreSession : SceneCoreSession by lazy { SceneCoreSession.create(this) }
    private lateinit var arCoreSession: ARCoreSession

    private lateinit var job : Job


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerRequestPermissionLauncher(this)

        Log.d(TAG, "onCreate")
        setContent {
            SoundExplorerMainScreen2()
        }

        when (val result = ARCoreSession.create(this)) {
            is SessionCreateSuccess -> {
                arCoreSession = result.session
            }
            is SessionCreatePermissionsNotGranted -> {
                requestPermissionLauncher.launch(result.permissions.toTypedArray())
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (!this::arCoreSession.isInitialized) {
            return
        }
        when (val result = arCoreSession.resume()) {
            is SessionResumeSuccess -> {
                startHandTracking(
                    lifecycleOwner = this, session = arCoreSession
                )
            }
            is SessionResumePermissionsNotGranted -> {
                requestPermissionLauncher.launch(result.permissions.toTypedArray())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!this::arCoreSession.isInitialized) {
            return
        }
        stopHandTracking()
        arCoreSession.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!this::arCoreSession.isInitialized) {
            return
        }
        arCoreSession.destroy()
    }

    @SuppressLint("RestrictedApi")
    private fun startHandTracking(
        lifecycleOwner: LifecycleOwner,
        session: ARCoreSession
    ) {
        job = lifecycleOwner.lifecycleScope.launch {

            val almModel = GltfModel.create(scenecoreSession, "glb/02static.glb").await()
            val palmEntity = GltfModelEntity.create(scenecoreSession, almModel).apply {
                setScale(0.005f)
                addComponent(InteractableComponent.create(scenecoreSession, mainExecutor) { event ->
                    when (event.action) {
                        InputEvent.ACTION_DOWN -> {
                            Log.d("TAG", "InputEvent.ACTION_DOWN")
                        }
                        InputEvent.ACTION_UP -> {
                            Log.d("TAG", "InputEvent.ACTION_UP")
                        }
                        InputEvent.ACTION_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_MOVE")
                        }
                        InputEvent.ACTION_CANCEL -> {
                            Log.d("TAG", "InputEvent.ACTION_CANCEL")
                        }
                        InputEvent.ACTION_HOVER_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_MOVE")
                        }
                        InputEvent.ACTION_HOVER_ENTER -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_ENTER")
                            setScale(0.009f)
                        }
                        InputEvent.ACTION_HOVER_EXIT -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_EXIT")
                            setScale(0.005f)
                        }
                        else -> {
                            Log.d("TAG", "InputEvent.OTHER: ${event.action} event:[$event]")
                        }
                    }
                })
            }

            handTracking(session ,scenecoreSession, palmEntity)
        }
    }

    private fun stopHandTracking() {
        job.cancel()
    }

    @SuppressLint("RestrictedApi")
    private suspend fun handTracking(
        session: ARCoreSession,
        scenecoreSession : SceneCoreSession,
        palmEntity : Entity
    ) {
        Hand.left(session)?.state?.collect { leftHandState ->
            val palmPose = leftHandState.handJoints[HandJointType.PALM] ?: return@collect

            // the down direction points in the same direction as the palm
            val angle = Vector3.angleBetween(palmPose.rotation * Vector3.Down, Vector3.Up)
            palmEntity.setHidden(angle > Math.toRadians(40.0))

            val transformedPose =
                scenecoreSession.perceptionSpace.transformPoseTo(
                    palmPose,
                    scenecoreSession.activitySpace,
                )
            val newPosition = transformedPose.translation + transformedPose.down*0.05f
            palmEntity.setPose(Pose(newPosition, transformedPose.rotation))
        }
    }

    private fun registerRequestPermissionLauncher(activity: ComponentActivity) {
        requestPermissionLauncher =
            activity.registerForActivityResult(
                RequestMultiplePermissions()
            ) { permissions ->
                val allPermissionsGranted = permissions.all { it.value }
                if (!allPermissionsGranted) {
                    Toast.makeText(
                        activity,
                        "Required permissions were not granted, closing activity. ",
                        Toast.LENGTH_LONG,
                    )
                        .show()
                    activity.finish()
                } else {
                    activity.recreate()
                }
            }
    }

}