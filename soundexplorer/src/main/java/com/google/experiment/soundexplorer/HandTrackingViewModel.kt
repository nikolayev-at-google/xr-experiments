package com.google.experiment.soundexplorer


import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.xr.arcore.Hand
import androidx.xr.arcore.HandJointType
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to handle hand tracking data and analysis
 */
@SuppressLint("RestrictedApi")
class HandTrackingViewModel() : ViewModel() {

    // Hand tracking data
    private val _leftHandData = MutableStateFlow<HandData?>(null)
    val leftHandData: StateFlow<HandData?> = _leftHandData.asStateFlow()

    private val _rightHandData = MutableStateFlow<HandData?>(null)
    val rightHandData: StateFlow<HandData?> = _rightHandData.asStateFlow()

    // Current gesture
    private val _leftHandGesture = MutableStateFlow<String>("None")
    val leftHandGesture: StateFlow<String> = _leftHandGesture.asStateFlow()

    private val _rightHandGesture = MutableStateFlow<String>("None")
    val rightHandGesture: StateFlow<String> = _rightHandGesture.asStateFlow()

    private lateinit var leftJob : Job
//    private lateinit var rightJob : Job

    // Data class to store hand joint information
    data class HandData(
        val palmPose: Pose?,
        val wristPose: Pose?,
        val fingerTips: Map<String, Pose>,
        val mcpJoints: Map<String, Pose>,
        val pipJoints: Map<String, Pose>,
        val dipJoints: Map<String, Pose>,
        val fingerExtensionRatios: Map<String, Float>,
        val fingerDistancesToPalm: Map<String, Float>,
        val palmToFingerRatios: Map<String, Float>,
        val palmNormalVector: Vector3?,
        val isPalmFacingUser: Boolean
    )

    // Start tracking hands
    fun startTracking(session: Session) {
        leftJob = viewModelScope.launch {
            Hand.left(session)?.state?.collect { handState ->
                if (handState.isActive) {
                    processHandState(handState, true)
                } else {
                    _leftHandData.value = null
                    _leftHandGesture.value = "Not Tracking"
                }
            }
        }
    }

    fun stopTracking() {
        leftJob.cancel()
    }

    private fun processHandState(handState: Hand.State, isLeftHand: Boolean) {
        val joints = handState.handJoints

        // Get palm and wrist poses
        val palmPose = joints[HandJointType.PALM]
        val wristPose = joints[HandJointType.WRIST]

        // Get finger tip poses
        val fingerTips = mapOf(
            "Thumb" to joints[HandJointType.THUMB_TIP],
            "Index" to joints[HandJointType.INDEX_TIP],
            "Middle" to joints[HandJointType.MIDDLE_TIP],
            "Ring" to joints[HandJointType.RING_TIP],
            "Pinky" to joints[HandJointType.LITTLE_TIP]
        ).filterValues { it != null }.mapValues { it.value!! }

        // Get MCP joints (finger base)
        val mcpJoints = mapOf(
            "Thumb" to joints[HandJointType.THUMB_METACARPAL],
            "Index" to joints[HandJointType.INDEX_METACARPAL],
            "Middle" to joints[HandJointType.MIDDLE_METACARPAL],
            "Ring" to joints[HandJointType.RING_METACARPAL],
            "Pinky" to joints[HandJointType.LITTLE_METACARPAL]
        ).filterValues { it != null }.mapValues { it.value!! }

        // Get PIP joints (middle joints)
        val pipJoints = mapOf(
            "Thumb" to joints[HandJointType.THUMB_PROXIMAL],
            "Index" to joints[HandJointType.INDEX_PROXIMAL],
            "Middle" to joints[HandJointType.MIDDLE_PROXIMAL],
            "Ring" to joints[HandJointType.RING_PROXIMAL],
            "Pinky" to joints[HandJointType.LITTLE_PROXIMAL]
        ).filterValues { it != null }.mapValues { it.value!! }

        // Get DIP joints (end joints)
        val dipJoints = mapOf(
            "Thumb" to joints[HandJointType.THUMB_PROXIMAL],
            "Index" to joints[HandJointType.INDEX_INTERMEDIATE],
            "Middle" to joints[HandJointType.MIDDLE_INTERMEDIATE],
            "Ring" to joints[HandJointType.RING_INTERMEDIATE],
            "Pinky" to joints[HandJointType.LITTLE_INTERMEDIATE]
        ).filterValues { it != null }.mapValues { it.value!! }

        // Calculate finger extension ratios
        val fingerExtensionRatios = mutableMapOf<String, Float>()
        val fingerDistancesToPalm = mutableMapOf<String, Float>()
        val palmToFingerRatios = mutableMapOf<String, Float>()

        if (wristPose != null && palmPose != null) {
            val palmLength = Vector3.distance(wristPose.translation, palmPose.translation)

            fingerTips.forEach { (finger, tip) ->
                val mcpJoint = mcpJoints[finger]
                if (mcpJoint != null) {
                    // Calculate finger length (wrist to tip)
                    val fingerLength = Vector3.distance(wristPose.translation, tip.translation)
                    // Calculate palm to finger base length
                    val palmToFingerBase = Vector3.distance(wristPose.translation, mcpJoint.translation)

                    // Store ratios
                    fingerExtensionRatios[finger] = fingerLength / palmLength
                    fingerDistancesToPalm[finger] = Vector3.distance(tip.translation, palmPose.translation) / palmLength
                    palmToFingerRatios[finger] = palmToFingerBase / palmLength
                }
            }
        }

        // Calculate palm normal vector and determine if palm is facing user
        val palmNormalVector = palmPose?.forward
        val isPalmFacingUser = if (palmNormalVector != null) {
            // Vector pointing to the camera/user (Forward is -Z in ARCore)
            val toCameraVector = Vector3.Forward.copy(z = -1f)
            // Calculate dot product - positive when vectors point in similar directions
            val dotProduct = palmNormalVector.dot(toCameraVector)
            dotProduct > 0.5f  // Threshold allows for some deviation
        } else {
            false
        }

        // Determine gesture
        val gesture = when {
            !isPalmFacingUser -> "Palm Not Facing User"
            isHandOpen(fingerExtensionRatios) -> "Open Palm"
            isHandClosed(fingerDistancesToPalm) -> "Closed Fist"
            else -> "Other"
        }

        // Store hand data
        val handData = HandData(
            palmPose = palmPose,
            wristPose = wristPose,
            fingerTips = fingerTips,
            mcpJoints = mcpJoints,
            pipJoints = pipJoints,
            dipJoints = dipJoints,
            fingerExtensionRatios = fingerExtensionRatios,
            fingerDistancesToPalm = fingerDistancesToPalm,
            palmToFingerRatios = palmToFingerRatios,
            palmNormalVector = palmNormalVector,
            isPalmFacingUser = isPalmFacingUser
        )

        if (isLeftHand) {
            _leftHandData.value = handData
            _leftHandGesture.value = gesture
        } else {
            _rightHandData.value = handData
            _rightHandGesture.value = gesture
        }
    }

    private fun isHandOpen(fingerExtensionRatios: Map<String, Float>): Boolean {
        // Check if fingers are extended
        val indexExtended = fingerExtensionRatios["Index"]?.let { it > 1.7f } ?: false
        val middleExtended = fingerExtensionRatios["Middle"]?.let { it > 1.8f } ?: false
        val ringExtended = fingerExtensionRatios["Ring"]?.let { it > 1.7f } ?: false
        val pinkyExtended = fingerExtensionRatios["Pinky"]?.let { it > 1.5f } ?: false

        val extendedFingerCount = listOf(indexExtended, middleExtended, ringExtended, pinkyExtended)
            .count { it }

        return extendedFingerCount >= 3
    }

    private fun isHandClosed(fingerDistancesToPalm: Map<String, Float>): Boolean {
        // Check if fingertips are close to palm
        val indexClose = fingerDistancesToPalm["Index"]?.let { it < 1.0f } ?: false
        val middleClose = fingerDistancesToPalm["Middle"]?.let { it < 1.0f } ?: false
        val ringClose = fingerDistancesToPalm["Ring"]?.let { it < 1.0f } ?: false
        val pinkyClose = fingerDistancesToPalm["Pinky"]?.let { it < 1.0f } ?: false

        val closedFingerCount = listOf(indexClose, middleClose, ringClose, pinkyClose)
            .count { it }

        return closedFingerCount >= 3
    }
}
