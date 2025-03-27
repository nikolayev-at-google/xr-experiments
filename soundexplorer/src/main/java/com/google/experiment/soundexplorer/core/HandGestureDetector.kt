package com.google.experiment.soundexplorer.core
import android.annotation.SuppressLint
import androidx.xr.arcore.Hand
import androidx.xr.arcore.HandJointType
import androidx.xr.runtime.Session as ARCoreSession
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import android.util.Log

/**
 * Detects hand gestures with accurate palm orientation and fist detection.
 */
@SuppressLint("RestrictedApi")
class HandGestureDetector(
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val debug: Boolean = true,
    private val listener: (GestureEvent) -> Unit
) {
    enum class HandGesture { OPEN_PALM, CLOSED_FIST, OTHER }

    data class GestureEvent(
        val isLeftHand: Boolean,
        val gesture: HandGesture,
        val fingertipPoses: List<Pose> = emptyList(),
        val palmPose: Pose? = null,
        val fingertipPositionsCm: List<Vector3> = emptyList(),
        val palmPositionCm: Vector3? = null
    )

    private var prevLeftGesture = HandGesture.OTHER
    private var prevRightGesture = HandGesture.OTHER
    private lateinit var job : Job



    fun startDetection(session: ARCoreSession) {
        job = coroutineScope.launch {
            Hand.left(session)?.state?.collect { handState ->
                processHandState(true, handState)
            }
        }
    }

    private fun processHandState(isLeftHand: Boolean, handState: Hand.State) {
        val currentGesture = detectGesture(handState, isLeftHand)
        val prevGesture = if (isLeftHand) prevLeftGesture else prevRightGesture

        // Only send event if gesture has changed
        if (currentGesture != prevGesture) {
            // Update previous gesture
            if (isLeftHand) {
                prevLeftGesture = currentGesture
            } else {
                prevRightGesture = currentGesture
            }

            // Prepare event data
            val eventData = when (currentGesture) {
                HandGesture.OPEN_PALM -> {
                    val joints = handState.handJoints
                    val palmPose = joints[HandJointType.PALM]

                    // Get fingertip poses
                    val thumbTip = joints[HandJointType.THUMB_TIP]
                    val indexTip = joints[HandJointType.INDEX_TIP]
                    val middleTip = joints[HandJointType.MIDDLE_TIP]
                    val ringTip = joints[HandJointType.RING_TIP]
                    val pinkyTip = joints[HandJointType.LITTLE_TIP]

                    val fingertipPoses = listOfNotNull(thumbTip, indexTip, middleTip, ringTip, pinkyTip)

                    // Convert positions to centimeters
                    val fingertipPositionsCm = fingertipPoses.map { pose ->
                        toCentimeters(pose.translation)
                    }

                    val palmPositionCm = palmPose?.let { toCentimeters(it.translation) }

                    GestureEvent(
                        isLeftHand = isLeftHand,
                        gesture = HandGesture.OPEN_PALM,
                        fingertipPoses = fingertipPoses,
                        palmPose = palmPose,
                        fingertipPositionsCm = fingertipPositionsCm,
                        palmPositionCm = palmPositionCm
                    )
                }
                else -> {
                    GestureEvent(
                        isLeftHand = isLeftHand,
                        gesture = currentGesture
                    )
                }
            }

            // Notify listeners
            listener.invoke(eventData)
        }
    }

    private fun detectGesture(handState: Hand.State, isLeftHand: Boolean): HandGesture {
        if (!handState.isActive) {
            return HandGesture.OTHER
        }

        val joints = handState.handJoints
        val palmPose = joints[HandJointType.PALM] ?: return HandGesture.OTHER

        // Check palm orientation - correctly using the direction vector
        if (isPalmFacingUser(palmPose)) {
            // Palm is facing user, determine if open or closed
            return if (isHandOpen(joints)) {
                HandGesture.OPEN_PALM
            } else if (isHandClosed(joints)) {
                HandGesture.CLOSED_FIST
            } else {
                HandGesture.OTHER
            }
        }

        return HandGesture.OTHER
    }

    /**
     * Check if palm is facing the user.
     * In ARCore, the palm normal vector points out from the palm surface.
     */
    private fun isPalmFacingUser(palmPose: Pose): Boolean {
        // CORRECTED: Get direction vector that points out from palm surface
        // In ARCore's coordinate system, this is the forward vector!
        val palmNormal = palmPose.forward

        // Vector pointing to the camera/user
        val toCameraVector = Vector3.Forward.copy(z = -1f)  // Inverted Z since Forward is -Z

        // Calculate dot product - positive when vectors point in similar directions
        val dotProduct = palmNormal.dot(toCameraVector)

        // Log for debugging
        if (debug) {
            Log.d("HandGesture", "Palm normal: $palmNormal, Dot product: $dotProduct")
        }

        // If dot product is positive, palm is facing user
        return dotProduct > 0.5f  // Threshold allows for some deviation
    }

    /**
     * Check if hand is open with fingers extended
     */
    private fun isHandOpen(joints: Map<HandJointType, Pose>): Boolean {
        // Get fingertips and metacarpals
        val indexTip = joints[HandJointType.INDEX_TIP]?.translation ?: return false
        val middleTip = joints[HandJointType.MIDDLE_TIP]?.translation ?: return false
        val ringTip = joints[HandJointType.RING_TIP]?.translation ?: return false
        val pinkyTip = joints[HandJointType.LITTLE_TIP]?.translation ?: return false

        val indexMcp = joints[HandJointType.INDEX_METACARPAL]?.translation ?: return false
        val middleMcp = joints[HandJointType.MIDDLE_METACARPAL]?.translation ?: return false
        val ringMcp = joints[HandJointType.RING_METACARPAL]?.translation ?: return false
        val pinkyMcp = joints[HandJointType.LITTLE_METACARPAL]?.translation ?: return false

        val wrist = joints[HandJointType.WRIST]?.translation ?: return false

        // Calculate distances from wrist to fingertips
        val indexLength = Vector3.distance(wrist, indexTip)
        val middleLength = Vector3.distance(wrist, middleTip)
        val ringLength = Vector3.distance(wrist, ringTip)
        val pinkyLength = Vector3.distance(wrist, pinkyTip)

        // Calculate distances from wrist to MCPs (finger bases)
        val palmLength = (Vector3.distance(wrist, indexMcp) +
                Vector3.distance(wrist, middleMcp) +
                Vector3.distance(wrist, ringMcp) +
                Vector3.distance(wrist, pinkyMcp)) / 4f

        // Calculate finger extension ratios
        val indexRatio = indexLength / palmLength
        val middleRatio = middleLength / palmLength
        val ringRatio = ringLength / palmLength
        val pinkyRatio = pinkyLength / palmLength

        if (debug) {
            Log.d("HandGesture", "Finger ratios: I:$indexRatio, M:$middleRatio, R:$ringRatio, P:$pinkyRatio")
        }

        // For an open hand, fingers should be significantly longer than palm
        val extendedFingerCount = listOf(
            indexRatio > 1.7f,  // Thresholds adjusted based on typical hand proportions
            middleRatio > 1.8f,
            ringRatio > 1.7f,
            pinkyRatio > 1.5f  // Pinky is naturally shorter
        ).count { it }

        return extendedFingerCount >= 3  // At least 3 fingers need to be extended
    }

    /**
     * Check if hand is closed into a fist
     */
    private fun isHandClosed(joints: Map<HandJointType, Pose>): Boolean {
        // Get fingertips
        val indexTip = joints[HandJointType.INDEX_TIP]?.translation ?: return false
        val middleTip = joints[HandJointType.MIDDLE_TIP]?.translation ?: return false
        val ringTip = joints[HandJointType.RING_TIP]?.translation ?: return false
        val pinkyTip = joints[HandJointType.LITTLE_TIP]?.translation ?: return false

        // Get palm center
        val palm = joints[HandJointType.PALM]?.translation ?: return false

        // Get wrist position
        val wrist = joints[HandJointType.WRIST]?.translation ?: return false

        // Calculate palm length as reference
        val palmLength = Vector3.distance(wrist, palm)

        // For a fist, fingertips should be close to palm
        val distToIndex = Vector3.distance(indexTip, palm) / palmLength
        val distToMiddle = Vector3.distance(middleTip, palm) / palmLength
        val distToRing = Vector3.distance(ringTip, palm) / palmLength
        val distToPinky = Vector3.distance(pinkyTip, palm) / palmLength

        if (debug) {
            Log.d("HandGesture", "Fist distances: I:$distToIndex, M:$distToMiddle, R:$distToRing, P:$distToPinky")
        }

        // Count fingers close to palm
        val closedFingerCount = listOf(
            distToIndex < 1.0f,
            distToMiddle < 1.0f,
            distToRing < 1.0f,
            distToPinky < 1.0f
        ).count { it }

        return closedFingerCount >= 3  // At least 3 fingers need to be close to palm
    }

    /**
     * Convert Vector3 from meters to centimeters
     */
    private fun toCentimeters(position: Vector3): Vector3 {
        return Vector3(position.x * 100f, position.y * 100f, position.z * 100f)
    }

    fun stopDetection() {
        // Any cleanup code
        job.cancel()
    }
}