package com.example.xrexp.arcore

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.concurrent.futures.await
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.xr.arcore.Hand
import androidx.xr.arcore.HandJointType

import androidx.xr.arcore.perceptionState
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SubspaceComposable
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.runtime.SessionCreateSuccess
import androidx.xr.runtime.math.Pose
import androidx.xr.scenecore.Session as SceneCoreSession
import androidx.xr.runtime.Session as ARCoreSession
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.compose.platform.SpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width
import com.example.xrexp.arcore.thumbsup.HandGestureDebugVisualization
import com.example.xrexp.arcore.thumbsup.ThumbsUpDetector
import com.example.xrexp.ui.theme.LocalSpacing
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator


class ExpHandsActivity : ComponentActivity() {

    companion object {

        private val TAG = "ExpArCoreActivity"

        const val GLB_FILE_NAME = "models/xyzArrows.glb"
    }

    private lateinit var arCoreSession: ARCoreSession
    private lateinit var sceneCoreSession: SceneCoreSession
    private lateinit var arrowsModel : GltfModel
    private val leftHandEntities = mutableMapOf<HandJointType, GltfModelEntity>()
    private val rightHandEntities = mutableMapOf<HandJointType, GltfModelEntity>()
    private lateinit var resourceAsync : Deferred<Unit>

    // State holders for Compose
    private val _leftHandDebugInfo = mutableStateOf<ThumbsUpDetector.DebugInfo>(ThumbsUpDetector.DebugInfo())
    val leftHandDebugInfo: State<ThumbsUpDetector.DebugInfo> = _leftHandDebugInfo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Subspace {
                MainPanel()
            }
        }

        arCoreSession = (ARCoreSession.create(this) as SessionCreateSuccess).session
        sceneCoreSession = SceneCoreSession.create(this)

        resourceAsync =
            lifecycleScope.async {
                arrowsModel = GltfModel.create(sceneCoreSession, "models/xyzArrows.glb").await()

                HandJointType.entries.forEach {
                    leftHandEntities[it] = GltfModelEntity
                        .create(sceneCoreSession, arrowsModel, Pose()).apply {
                            setScale(0.01f)
                            setParent(sceneCoreSession.activitySpace)
                        }
                    rightHandEntities[it] = GltfModelEntity
                        .create(sceneCoreSession, arrowsModel, Pose()).apply {
                            setScale(0.01f)
                            setParent(sceneCoreSession.activitySpace)
                        }
                }
            }

        lifecycleScope.launch {
            val gltfModel = GltfModel.create(sceneCoreSession, GLB_FILE_NAME).await()
            // check for spatial capabilities
            if (SpatialCapabilities.getOrCreate(sceneCoreSession).isContent3dEnabled){
                // create the gltf entity using the gltf file from the previous snippet
                val gltfEntity = GltfModelEntity.create(sceneCoreSession, gltfModel)
                gltfEntity.setParent(sceneCoreSession.activitySpace)
            } else {
                Toast.makeText(this@ExpHandsActivity, "3D content not enabled", Toast.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launch {
            resourceAsync.await()

            Hand.left(arCoreSession)?.state?.collect { handState ->
                handStateTracking(handState, true)
            }

//            Hand.right(arCoreSession)?.state?.collect {
//                handStateTracking(it, false)
//            }
        }
    }

    override fun onResume() {
        super.onResume()
        arCoreSession.resume()
    }

    override fun onPause() {
        super.onPause()
        arCoreSession.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arCoreSession.destroy()
    }

    @Composable
    @SubspaceComposable
    private fun MainPanel() {
        val state by arCoreSession.state.collectAsStateWithLifecycle()
        val perceptionState = state.perceptionState

        SpatialPanel(
            SubspaceModifier
                .width(1000.dp)
                .height(480.dp)
                .resizable(true)
                .movable(true)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(LocalSpacing.current.l)
            ) {
                Text(
                    modifier = Modifier.padding(LocalSpacing.current.m),
                    text = "Detected planes: ${perceptionState?.trackables?.size}"
                )
                HandGestureDebugVisualization(
                    leftHandDebugInfo.value
                )
//                Row {
//                    HandTrackingPanel(perceptionState?.leftHand, true)
//                    HandTrackingPanel(perceptionState?.rightHand, false)
//                }
            }
        }
    }

    @Composable
    private fun HandTrackingPanel(hand: Hand?, isLeftHand: Boolean) {

        val name = if (isLeftHand) "Left Hand" else "Right Hand"
        val bgColor = if (isLeftHand) Color.Yellow else Color.Cyan

        if (hand == null) {
            Text("$name is NULL")
            return
        }

        val handState by hand.state.collectAsStateWithLifecycle()

        Column(
            modifier =
                Modifier
                    .background(color = bgColor)
                    .fillMaxHeight()
                    .padding(horizontal = LocalSpacing.current.m)
        ) {
            Text("$name isActive: ${handState.isActive}")
            for ((jointType, pose) in handState.handJoints) {
                Text("$name joint ${jointType}: ${pose.translation}")
            }
        }
    }

    private fun handStateTracking(handState: Hand.State, isLeftHand: Boolean) {
        if (handState.isActive) {

            val result = ThumbsUpDetector.detectThumbsUp(handState)

            // Update the state for Compose
            _leftHandDebugInfo.value = result.debugInfo

            if (result.isThumbsUp) {
                // Handle hand thumbs up detected
                Log.d(TAG, "=================================================================")
                Log.d(TAG, ">>>>>>>>>>>                 ThumbsUp        <<<<<<<<<<<<<<<<<<<<<")
                Log.d(TAG, "=================================================================")
            }

            // Debug visualization
            result.debugInfo?.let { debug ->
                logDetailedDebugInfo(debug)
            }

            handState.handJoints.forEach { joint ->
                val transformedPose =
                    sceneCoreSession.perceptionSpace.transformPoseTo(
                        joint.value,
                        sceneCoreSession.activitySpace,
                    )
                val newPosition = transformedPose.translation + transformedPose.down * 0.01f

                if (isLeftHand)
                    leftHandEntities[joint.key]?.setPose(Pose(newPosition, transformedPose.rotation))
                else
                    rightHandEntities[joint.key]?.setPose(Pose(newPosition, transformedPose.rotation))
            }
        }
    }

    private fun logDetailedDebugInfo(debug: ThumbsUpDetector.DebugInfo) {
        Log.d(ThumbsUpDetector.TAG, "=== HAND DEBUG INFO ===")
        Log.d(ThumbsUpDetector.TAG, "Active: ${debug.isActive}")
        Log.d(ThumbsUpDetector.TAG, "Has all joints: ${debug.hasAllRequiredJoints}")
        Log.d(ThumbsUpDetector.TAG, "Thumb pointing up: ${debug.isThumbPointingUp} (alignment: ${debug.thumbUpAlignment})")
        Log.d(ThumbsUpDetector.TAG, "Thumb extended: ${debug.isThumbExtended} (ratio: ${debug.thumbExtensionRatio})")
        Log.d(ThumbsUpDetector.TAG, "Finger curl status:")
        Log.d(ThumbsUpDetector.TAG, "  Index: ${debug.isIndexCurled} (${debug.fingerCurlValues["index"]})")
        Log.d(ThumbsUpDetector.TAG, "  Middle: ${debug.isMiddleCurled} (${debug.fingerCurlValues["middle"]})")
        Log.d(ThumbsUpDetector.TAG, "  Ring: ${debug.isRingCurled} (${debug.fingerCurlValues["ring"]})")
        Log.d(ThumbsUpDetector.TAG, "  Little: ${debug.isLittleCurled} (${debug.fingerCurlValues["little"]})")
        Log.d(ThumbsUpDetector.TAG, "Up vector: ${debug.upVector}")
        Log.d(ThumbsUpDetector.TAG, "Thumb vector: ${debug.thumbVector}")
    }
}



