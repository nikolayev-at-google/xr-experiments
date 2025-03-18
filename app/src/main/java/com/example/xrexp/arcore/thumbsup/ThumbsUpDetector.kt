package com.example.xrexp.arcore.thumbsup

import android.annotation.SuppressLint
import androidx.xr.arcore.HandJointType
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import android.util.Log
import androidx.xr.arcore.Hand

/**
 * Detects whether a hand is showing a "thumbs up" gesture with extensive debugging.
 */
@SuppressLint("RestrictedApi")
object ThumbsUpDetector {
    const val TAG = "ThumbsUpDetector"
    private const val DEBUG = true // Set to true to enable debug logging

    /**
     * Debug data class that contains all the intermediate calculations and thresholds
     */
    data class DebugInfo(
        val isActive: Boolean = false,
        val hasAllRequiredJoints: Boolean = false,
        val thumbUpAlignment: Float = 0f,
        val isThumbPointingUp: Boolean = false,
        val thumbExtensionRatio: Float = 0f,
        val isThumbExtended: Boolean = false,
        val isIndexCurled: Boolean = false,
        val isMiddleCurled: Boolean = false,
        val isRingCurled: Boolean = false,
        val isLittleCurled: Boolean = false,
        val fingerCurlValues: Map<String, Float> = emptyMap(),
        val upVector: Vector3 = Vector3(),
        val thumbVector: Vector3 = Vector3()
    )

    /**
     * Result of the thumbs up detection with optional debug information
     */
    data class Result(
        val isThumbsUp: Boolean,
        val debugInfo: DebugInfo = DebugInfo()
    )

    /**
     * Determines if a hand is showing a "thumbs up" gesture with optional debug info.
     */
    fun detectThumbsUp(handState: Hand.State, includeDebugInfo: Boolean = DEBUG): Result {
        // Initialize debug info
        var debugInfo = DebugInfo(isActive = handState.isActive)

        // Check if hand is being tracked
        if (!handState.isActive) {
            if (DEBUG) Log.d(TAG, "Hand is not active")
            return Result(false, debugInfo)
        }

        val joints = handState.handJoints

        // Check for required joints
        val wristPose = joints[HandJointType.WRIST]
        val palmPose = joints[HandJointType.PALM]
        val thumbMetacarpalPose = joints[HandJointType.THUMB_METACARPAL]
        val thumbTipPose = joints[HandJointType.THUMB_TIP]
        val indexTipPose = joints[HandJointType.INDEX_TIP]
        val middleTipPose = joints[HandJointType.MIDDLE_TIP]
        val ringTipPose = joints[HandJointType.RING_TIP]
        val littleTipPose = joints[HandJointType.LITTLE_TIP]
        val indexIntermediatePose = joints[HandJointType.INDEX_INTERMEDIATE]
        val middleIntermediatePose = joints[HandJointType.MIDDLE_INTERMEDIATE]
        val ringIntermediatePose = joints[HandJointType.RING_INTERMEDIATE]
        val littleIntermediatePose = joints[HandJointType.LITTLE_INTERMEDIATE]
        val indexMetacarpalPose = joints[HandJointType.INDEX_METACARPAL]
        val indexProximalPose = joints[HandJointType.INDEX_PROXIMAL]
        val middleProximalPose = joints[HandJointType.MIDDLE_PROXIMAL]
        val ringProximalPose = joints[HandJointType.RING_PROXIMAL]
        val littleProximalPose = joints[HandJointType.LITTLE_PROXIMAL]

        // Check if all required joints are available
        val hasAllJoints = wristPose != null && palmPose != null &&
                thumbMetacarpalPose != null && thumbTipPose != null &&
                indexTipPose != null && middleTipPose != null &&
                ringTipPose != null && littleTipPose != null &&
                indexIntermediatePose != null && middleIntermediatePose != null &&
                ringIntermediatePose != null && littleIntermediatePose != null &&
                indexMetacarpalPose != null &&
                indexProximalPose != null && middleProximalPose != null &&
                ringProximalPose != null && littleProximalPose != null

        debugInfo = debugInfo.copy(hasAllRequiredJoints = hasAllJoints)

        if (!hasAllJoints) {
            if (DEBUG) Log.d(TAG, "Missing required joints")
            return Result(false, debugInfo)
        }

        // 1. Establish hand coordinate system

        // Forward vector: from wrist to palm
        val forwardVec = createVector(wristPose!!.translation, palmPose!!.translation)
        val forwardVecNorm = normalize(forwardVec)

        // Side vector (from index metacarpal to thumb metacarpal)
        // For right hand: index to thumb direction gives proper side vector
        // For left hand: need to go from thumb to index to maintain consistent coordinate system
        val isLeftHand = isLeftHand(joints)
        val sideVec = if (isLeftHand) {
            createVector(thumbMetacarpalPose!!.translation, indexMetacarpalPose!!.translation)
        } else {
            createVector(indexMetacarpalPose!!.translation, thumbMetacarpalPose!!.translation)
        }
        val sideVecNorm = normalize(sideVec)

        // Calculate the "up" direction using cross product
        val upVec = normalize(crossProduct(forwardVecNorm, sideVecNorm))

        // 2. Calculate thumb direction (from metacarpal to tip)
        val thumbVec = createVector(thumbMetacarpalPose.translation, thumbTipPose!!.translation)
        val thumbVecNorm = normalize(thumbVec)

        // 3. Check if thumb is pointing up relative to hand
        val thumbUpAlignment = dotProduct(thumbVecNorm, upVec)
        val thumbUpThreshold = 0.45f
        val isThumbPointingUp = thumbUpAlignment > thumbUpThreshold

        if (DEBUG) Log.d(TAG, "Thumb up alignment: $thumbUpAlignment (threshold: $thumbUpThreshold)")

        // 4. Check thumb extension
        val thumbMetaToWristDist = distance(thumbMetacarpalPose.translation, wristPose.translation)
        val thumbTipToWristDist = distance(thumbTipPose.translation, wristPose.translation)
        val thumbExtensionRatio = thumbTipToWristDist / thumbMetaToWristDist
        val thumbExtensionThreshold = 1.5f
        val isThumbExtended = thumbExtensionRatio > thumbExtensionThreshold

        if (DEBUG) Log.d(TAG, "Thumb extension ratio: $thumbExtensionRatio (threshold: $thumbExtensionThreshold)")

        // 5. Check if other fingers are curled
        val fingerCurlThreshold = 0.7f
        val fingerCurlValues = mutableMapOf<String, Float>()

        // Calculate curl values for each finger
        val indexCurlValue = getFingerCurlValue(joints, HandJointType.INDEX_PROXIMAL,
            HandJointType.INDEX_INTERMEDIATE, HandJointType.INDEX_TIP)
        val isIndexCurled = indexCurlValue < fingerCurlThreshold
        fingerCurlValues["index"] = indexCurlValue

        val middleCurlValue = getFingerCurlValue(joints, HandJointType.MIDDLE_PROXIMAL,
            HandJointType.MIDDLE_INTERMEDIATE, HandJointType.MIDDLE_TIP)
        val isMiddleCurled = middleCurlValue < fingerCurlThreshold
        fingerCurlValues["middle"] = middleCurlValue

        val ringCurlValue = getFingerCurlValue(joints, HandJointType.RING_PROXIMAL,
            HandJointType.RING_INTERMEDIATE, HandJointType.RING_TIP)
        val isRingCurled = ringCurlValue < fingerCurlThreshold
        fingerCurlValues["ring"] = ringCurlValue

        val littleCurlValue = getFingerCurlValue(joints, HandJointType.LITTLE_PROXIMAL,
            HandJointType.LITTLE_INTERMEDIATE, HandJointType.LITTLE_TIP)
        val isLittleCurled = littleCurlValue < fingerCurlThreshold
        fingerCurlValues["little"] = littleCurlValue

        if (DEBUG) {
            Log.d(TAG, "Finger curl values (threshold: $fingerCurlThreshold):")
            Log.d(TAG, "  Index: $indexCurlValue, curled: $isIndexCurled")
            Log.d(TAG, "  Middle: $middleCurlValue, curled: $isMiddleCurled")
            Log.d(TAG, "  Ring: $ringCurlValue, curled: $isRingCurled")
            Log.d(TAG, "  Little: $littleCurlValue, curled: $isLittleCurled")
        }

        // Update debug info
        debugInfo = debugInfo.copy(
            thumbUpAlignment = thumbUpAlignment,
            isThumbPointingUp = isThumbPointingUp,
            thumbExtensionRatio = thumbExtensionRatio,
            isThumbExtended = isThumbExtended,
            isIndexCurled = isIndexCurled,
            isMiddleCurled = isMiddleCurled,
            isRingCurled = isRingCurled,
            isLittleCurled = isLittleCurled,
            fingerCurlValues = fingerCurlValues,
            upVector = upVec,
            thumbVector = thumbVecNorm
        )

        // For thumbs up, thumb should be extended and pointing up, and other fingers should be curled
        val isThumbsUp = isThumbExtended &&
                isThumbPointingUp &&
                isIndexCurled &&
                isMiddleCurled &&
                isRingCurled &&
                isLittleCurled

        if (DEBUG) {
            Log.d(TAG, "THUMBS UP DETECTION RESULT: $isThumbsUp")
            Log.d(TAG, "  Thumb extended: $isThumbExtended")
            Log.d(TAG, "  Thumb pointing up: $isThumbPointingUp")
            Log.d(TAG, "  All fingers curled: ${isIndexCurled && isMiddleCurled && isRingCurled && isLittleCurled}")
        }

        return Result(isThumbsUp, debugInfo)
    }

    /**
     * Determines if this is a left hand based on the relative position of joints
     */
    private fun isLeftHand(joints: Map<HandJointType, Pose>): Boolean {
        val indexMeta = joints[HandJointType.INDEX_METACARPAL]?.translation
        val littleMeta = joints[HandJointType.LITTLE_METACARPAL]?.translation

        if (indexMeta != null && littleMeta != null) {
            // In a typical hand pose, if index is to the left of little finger,
            // it's a right hand, otherwise it's a left hand
            return indexMeta.x > littleMeta.x
        }

        // Default to right hand if we can't determine
        return false
    }

    /**
     * Gets the curl value for a finger (lower values indicate more curled)
     */
    private fun getFingerCurlValue(joints: Map<HandJointType, Pose>,
                                   proximalType: HandJointType,
                                   intermediateType: HandJointType,
                                   tipType: HandJointType): Float {

        val proximalPose = joints[proximalType] ?: return 0f
        val intermediatePose = joints[intermediateType] ?: return 0f
        val tipPose = joints[tipType] ?: return 0f

        // Vector from proximal to intermediate
        val segmentA = createVector(proximalPose.translation, intermediatePose.translation)

        // Vector from intermediate to tip
        val segmentB = createVector(intermediatePose.translation, tipPose.translation)

        // Calculate the dot product between the two segments
        return dotProduct(normalize(segmentA), normalize(segmentB))
    }

    /**
     * Creates a vector from point1 to point2.
     */
    private fun createVector(point1: Vector3, point2: Vector3): Vector3 {
        return Vector3(
            point2.x - point1.x,
            point2.y - point1.y,
            point2.z - point1.z
        )
    }

    /**
     * Normalizes a vector to unit length.
     */
    private fun normalize(v: Vector3): Vector3 = v.toNormalized()

    /**
     * Calculates the dot product of two vectors.
     */
    private fun dotProduct(v1: Vector3, v2: Vector3): Float = v1.dot(v2)

    /**
     * Calculates the cross product of two vectors.
     */
    private fun crossProduct(v1: Vector3, v2: Vector3): Vector3 = v1.cross(v2)

    /**
     * Calculates the Euclidean distance between two points in 3D space.
     */
    private fun distance(point1: Vector3, point2: Vector3): Float =
        Vector3.distance(point1, point2)
}