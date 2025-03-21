package com.example.xrexp.arcore.asl

import android.annotation.SuppressLint
import android.util.Log

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt

import androidx.xr.runtime.math.Vector3
import androidx.xr.arcore.Hand
import androidx.xr.runtime.math.Pose
import androidx.xr.arcore.HandJointType

/**
 * A detector for American Sign Language (ASL) hand signs with improved detection for A and E.
 */
@SuppressLint("RestrictedApi")
object ASLDetector {
    private const val TAG = "ASLDetector"
    private const val DEBUG = true

    /**
     * Represents supported ASL signs
     */
    enum class Sign {
        A, B, C, D, E, NONE
    }

    /**
     * Debug information for ASL detection
     */
    data class DebugInfo(
        val isActive: Boolean = false,
        val hasAllRequiredJoints: Boolean = false,
        val detectedSign: Sign = Sign.NONE,
        val confidenceScores: Map<Sign, Float> = emptyMap(),
        val fingerExtensions: Map<String, Float> = emptyMap(),
        val fingerCurls: Map<String, Float> = emptyMap(),
        val fingerPositions: Map<String, Vector3> = emptyMap(),
        val palmOrientation: Vector3 = Vector3(),
        // Additional debug data for sign A and E
        val thumbPositionData: Map<String, Float> = emptyMap()
    )

    /**
     * Result of the ASL sign detection with optional debug information
     */
    data class Result(
        val sign: Sign,
        val confidence: Float,
        val debugInfo: DebugInfo? = null
    )

    /**
     * Detects the current ASL sign from hand tracking data.
     */
    fun detectSign(handState: Hand.State, includeDebugInfo: Boolean = DEBUG): Result {
        // Initialize debug info
        var debugInfo = DebugInfo(isActive = handState.isActive)

        // Check if hand is being tracked
        if (!handState.isActive) {
            if (DEBUG) Log.d(TAG, "Hand is not active")
            return Result(Sign.NONE, 0f, if (includeDebugInfo) debugInfo else null)
        }

        val joints = handState.handJoints

        // Verify all required joints are present
        if (!hasRequiredJoints(joints)) {
            debugInfo = debugInfo.copy(hasAllRequiredJoints = false)
            if (DEBUG) Log.d(TAG, "Missing required joints for ASL detection")
            return Result(Sign.NONE, 0f, if (includeDebugInfo) debugInfo else null)
        }

        debugInfo = debugInfo.copy(hasAllRequiredJoints = true)

        // Calculate finger positions, extensions, and curls
        val fingerData = calculateFingerData(joints)
        val fingerExtensions = fingerData.first
        val fingerCurls = fingerData.second
        val fingerPositions = fingerData.third

        // Calculate palm orientation
        val palmOrientation = calculatePalmOrientation(joints)

        // Additional thumb position data for A and E debugging
        val thumbPositionData = calculateThumbPositionData(joints)

        // Calculate confidence scores for each sign
        val confidenceScores = mapOf(
            Sign.A to calculateSignAConfidence(joints, fingerExtensions, fingerCurls, thumbPositionData),
            Sign.B to calculateSignBConfidence(joints, fingerExtensions, fingerCurls, palmOrientation),
            Sign.C to calculateSignCConfidence(joints, fingerExtensions, fingerCurls, palmOrientation),
            Sign.D to calculateSignDConfidence(joints, fingerExtensions, fingerCurls, palmOrientation),
            Sign.E to calculateSignEConfidence(joints, fingerExtensions, fingerCurls, palmOrientation, thumbPositionData)
        )

        // Determine the most confident sign
        var detectedSign : Sign = Sign.NONE
        var highestConfidence : Float = 0f
        val firstElement = confidenceScores.entries.maxByOrNull { it.value }
        if (firstElement != null) {
            detectedSign = firstElement.key
            highestConfidence = firstElement.value
        }

        // Update debug info
        debugInfo = debugInfo.copy(
            detectedSign = if (highestConfidence > 0.6f) detectedSign else Sign.NONE,
            confidenceScores = confidenceScores,
            fingerExtensions = fingerExtensions,
            fingerCurls = fingerCurls,
            fingerPositions = fingerPositions.mapValues { it.value.translation },
            palmOrientation = palmOrientation,
            thumbPositionData = thumbPositionData
        )

        if (DEBUG) {
            Log.d(TAG, "ASL Detection: $detectedSign (confidence: ${highestConfidence.format(2)})")
            for ((sign, confidence) in confidenceScores) {
                Log.d(TAG, "  $sign: ${confidence.format(2)}")
            }

            // Special debug for A and E
            if (detectedSign == Sign.A || detectedSign == Sign.E ||
                confidenceScores[Sign.A]!! > 0.4f || confidenceScores[Sign.E]!! > 0.4f) {
                Log.d(TAG, "Finger curl values:")
                for ((finger, curl) in fingerCurls) {
                    Log.d(TAG, "  $finger curl: ${curl.format(2)}")
                }
                Log.d(TAG, "Thumb position data:")
                for ((key, value) in thumbPositionData) {
                    Log.d(TAG, "  $key: ${value.format(2)}")
                }
            }
        }

        return Result(
            sign = if (highestConfidence > 0.6f) detectedSign else Sign.NONE,
            confidence = highestConfidence,
            debugInfo = if (includeDebugInfo) debugInfo else null
        )
    }

    /**
     * Calculate detailed thumb position data for better A and E detection
     */
    private fun calculateThumbPositionData(joints: Map<HandJointType, Pose>): Map<String, Float> {
        val thumbPositionData = mutableMapOf<String, Float>()

        val thumbTip = joints[HandJointType.THUMB_TIP]?.translation ?: return thumbPositionData

        // Distance from thumb to each finger's proximal joint
        for ((finger, jointType) in listOf(
            "index" to HandJointType.INDEX_PROXIMAL,
            "middle" to HandJointType.MIDDLE_PROXIMAL,
            "ring" to HandJointType.RING_PROXIMAL,
            "little" to HandJointType.LITTLE_PROXIMAL
        )) {
            val proximalPos = joints[jointType]?.translation ?: continue
            thumbPositionData["thumb_to_${finger}_proximal"] = distance(thumbTip, proximalPos)
        }

        // Relative height of thumb compared to other finger tips
        val wristPos = joints[HandJointType.WRIST]?.translation ?: return thumbPositionData
        val palmPos = joints[HandJointType.PALM]?.translation ?: return thumbPositionData

        // Create a local coordinate system for the hand
        val wristToPalm = normalize(createVector(wristPos, palmPos))

        // Calculate relative heights in hand's local space
        for ((finger, jointType) in listOf(
            "thumb" to HandJointType.THUMB_TIP,
            "index" to HandJointType.INDEX_TIP,
            "middle" to HandJointType.MIDDLE_TIP,
            "ring" to HandJointType.RING_TIP,
            "little" to HandJointType.LITTLE_TIP
        )) {
            val tipPos = joints[jointType]?.translation ?: continue
            val wristToTip = createVector(wristPos, tipPos)

            // Project onto wrist-to-palm direction to get "height" in hand's frame
            thumbPositionData["${finger}_height"] = dotProduct(wristToTip, wristToPalm)
        }

        // Thumb position relative to palm
        val thumbBase = joints[HandJointType.THUMB_METACARPAL]?.translation ?: return thumbPositionData
        val indexBase = joints[HandJointType.INDEX_METACARPAL]?.translation ?: return thumbPositionData
        val littleBase = joints[HandJointType.LITTLE_METACARPAL]?.translation ?: return thumbPositionData

        // Vector across palm from index to little finger metacarpal
        val acrossPalm = normalize(createVector(indexBase, littleBase))

        // Vector from palm center to thumb tip
        val palmToThumb = createVector(palmPos, thumbTip)

        // How much the thumb crosses the palm (dot product with across-palm vector)
        thumbPositionData["thumb_crossing_palm"] = dotProduct(normalize(palmToThumb), acrossPalm)

        return thumbPositionData
    }

    /**
     * Check if all required joints for ASL detection are present
     */
    private fun hasRequiredJoints(joints: Map<HandJointType, Pose>): Boolean {
        val requiredJoints = listOf(
            HandJointType.WRIST,
            HandJointType.PALM,
            HandJointType.THUMB_METACARPAL,
            HandJointType.THUMB_PROXIMAL,
            HandJointType.THUMB_DISTAL,
            HandJointType.THUMB_TIP,
            HandJointType.INDEX_METACARPAL,
            HandJointType.INDEX_PROXIMAL,
            HandJointType.INDEX_INTERMEDIATE,
            HandJointType.INDEX_DISTAL,
            HandJointType.INDEX_TIP,
            HandJointType.MIDDLE_METACARPAL,
            HandJointType.MIDDLE_PROXIMAL,
            HandJointType.MIDDLE_INTERMEDIATE,
            HandJointType.MIDDLE_DISTAL,
            HandJointType.MIDDLE_TIP,
            HandJointType.RING_METACARPAL,
            HandJointType.RING_PROXIMAL,
            HandJointType.RING_INTERMEDIATE,
            HandJointType.RING_DISTAL,
            HandJointType.RING_TIP,
            HandJointType.LITTLE_METACARPAL,
            HandJointType.LITTLE_PROXIMAL,
            HandJointType.LITTLE_INTERMEDIATE,
            HandJointType.LITTLE_DISTAL,
            HandJointType.LITTLE_TIP
        )

        return requiredJoints.all { joints.containsKey(it) }
    }

    /**
     * Calculates finger extensions, curls, and positions
     * @return Triple of (fingerExtensions, fingerCurls, fingerPositions)
     */
    private fun calculateFingerData(joints: Map<HandJointType, Pose>): Triple<Map<String, Float>, Map<String, Float>, Map<String, Pose>> {
        val fingerExtensions = mutableMapOf<String, Float>()
        val fingerCurls = mutableMapOf<String, Float>()
        val fingerPositions = mutableMapOf<String, Pose>()

        // Calculate thumb extension
        val thumbBaseToWristDist = distance(
            joints[HandJointType.THUMB_METACARPAL]!!.translation,
            joints[HandJointType.WRIST]!!.translation
        )
        val thumbTipToWristDist = distance(
            joints[HandJointType.THUMB_TIP]!!.translation,
            joints[HandJointType.WRIST]!!.translation
        )
        fingerExtensions["thumb"] = thumbTipToWristDist / thumbBaseToWristDist

        // Calculate thumb curl
        fingerCurls["thumb"] = getFingerCurlValue(
            joints,
            HandJointType.THUMB_METACARPAL,
            HandJointType.THUMB_PROXIMAL,
            HandJointType.THUMB_TIP
        )

        // Store thumb position
        fingerPositions["thumb"] = joints[HandJointType.THUMB_TIP]!!

        // Calculate index finger
        calculateFingerData(
            "index",
            joints,
            HandJointType.INDEX_METACARPAL,
            HandJointType.INDEX_PROXIMAL,
            HandJointType.INDEX_INTERMEDIATE,
            HandJointType.INDEX_DISTAL,
            HandJointType.INDEX_TIP,
            fingerExtensions,
            fingerCurls,
            fingerPositions
        )

        // Calculate middle finger
        calculateFingerData(
            "middle",
            joints,
            HandJointType.MIDDLE_METACARPAL,
            HandJointType.MIDDLE_PROXIMAL,
            HandJointType.MIDDLE_INTERMEDIATE,
            HandJointType.MIDDLE_DISTAL,
            HandJointType.MIDDLE_TIP,
            fingerExtensions,
            fingerCurls,
            fingerPositions
        )

        // Calculate ring finger
        calculateFingerData(
            "ring",
            joints,
            HandJointType.RING_METACARPAL,
            HandJointType.RING_PROXIMAL,
            HandJointType.RING_INTERMEDIATE,
            HandJointType.RING_DISTAL,
            HandJointType.RING_TIP,
            fingerExtensions,
            fingerCurls,
            fingerPositions
        )

        // Calculate little finger
        calculateFingerData(
            "little",
            joints,
            HandJointType.LITTLE_METACARPAL,
            HandJointType.LITTLE_PROXIMAL,
            HandJointType.LITTLE_INTERMEDIATE,
            HandJointType.LITTLE_DISTAL,
            HandJointType.LITTLE_TIP,
            fingerExtensions,
            fingerCurls,
            fingerPositions
        )

        return Triple(fingerExtensions, fingerCurls, fingerPositions)
    }

    /**
     * Helper to calculate finger extension, curl and position for a single finger
     */
    private fun calculateFingerData(
        fingerName: String,
        joints: Map<HandJointType, Pose>,
        metacarpalType: HandJointType,
        proximalType: HandJointType,
        intermediateType: HandJointType,
        distalType: HandJointType,
        tipType: HandJointType,
        fingerExtensions: MutableMap<String, Float>,
        fingerCurls: MutableMap<String, Float>,
        fingerPositions: MutableMap<String, Pose>
    ) {
        // Calculate finger extension
        val fingerBaseToWristDist = distance(
            joints[metacarpalType]!!.translation,
            joints[HandJointType.WRIST]!!.translation
        )
        val fingerTipToWristDist = distance(
            joints[tipType]!!.translation,
            joints[HandJointType.WRIST]!!.translation
        )
        fingerExtensions[fingerName] = fingerTipToWristDist / fingerBaseToWristDist

        // Calculate finger curl
        fingerCurls[fingerName] = getFingerCurlValue(
            joints,
            proximalType,
            intermediateType,
            tipType
        )

        // Store finger tip position
        fingerPositions[fingerName] = joints[tipType]!!
    }

    /**
     * Calculates the palm orientation vector
     */
    private fun calculatePalmOrientation(joints: Map<HandJointType, Pose>): Vector3 {
        val wristPose = joints[HandJointType.WRIST]!!
        val palmPose = joints[HandJointType.PALM]!!
        val indexMeta = joints[HandJointType.INDEX_METACARPAL]!!
        val pinkyMeta = joints[HandJointType.LITTLE_METACARPAL]!!

        // Calculate palm normal vector using cross product
        val wristToPalm = createVector(wristPose.translation, palmPose.translation)
        val indexToPinky = createVector(indexMeta.translation, pinkyMeta.translation)

        return normalize(crossProduct(wristToPalm, indexToPinky))
    }

    /**
     * IMPROVED Confidence calculation for ASL Sign A
     * A fist with the thumb positioned at the side of the fist
     */
    private fun calculateSignAConfidence(
        joints: Map<HandJointType, Pose>,
        fingerExtensions: Map<String, Float>,
        fingerCurls: Map<String, Float>,
        thumbPositionData: Map<String, Float>
    ): Float {
        var confidence = 0f
        val debugDetails = mutableMapOf<String, Float>()

        // REVISED: For Sign A: All fingers should be curled (low extension, high curl)
        // Increased extension threshold to 1.3 (was 1.2)
        // Increased curl threshold to 0.6 (was 0.5)
        val indexCurled = fingerCurls["index"]!! < 0.6f && fingerExtensions["index"]!! < 1.3f
        val middleCurled = fingerCurls["middle"]!! < 0.6f && fingerExtensions["middle"]!! < 1.3f
        val ringCurled = fingerCurls["ring"]!! < 0.6f && fingerExtensions["ring"]!! < 1.3f
        val littleCurled = fingerCurls["little"]!! < 0.6f && fingerExtensions["little"]!! < 1.3f

        // All fingers should be curled similarly for a fist
        val fingerCurlConsistency = areValuesSimilar(
            listOf(
                fingerCurls["index"]!!,
                fingerCurls["middle"]!!,
                fingerCurls["ring"]!!,
                fingerCurls["little"]!!
            ),
            maxDifference = 0.2f
        )

        // REVISED: Thumb position for Sign A
        // Thumb should be somewhat tucked, but not fully curled like other fingers
        val thumbTucked = fingerCurls["thumb"]!! < 0.9f && fingerExtensions["thumb"]!! < 1.7f

        // IMPROVED: Check thumb position relative to index finger
        // Increased distance threshold to 0.12 (was 0.05)
        // Also check alternative references for thumb position
        val thumbToIndexProximalDist = thumbPositionData["thumb_to_index_proximal"] ?: 1.0f

        // Thumb should be close to the side of the index finger
        val thumbCorrectPosition = thumbToIndexProximalDist < 0.12f

        // ADDED: Additional checks for A sign

        // Thumb should be lower than other fingers
        val isThumbLower = (thumbPositionData["thumb_height"] ?: 0f) <
                (thumbPositionData["index_height"] ?: 0f)

        // Thumb shouldn't cross over the palm too much
        val thumbNotCrossingPalm = (thumbPositionData["thumb_crossing_palm"] ?: 1.0f) < 0.3f

        // Calculate confidence based on all criteria
        confidence += if (indexCurled) 0.15f else 0f
        confidence += if (middleCurled) 0.15f else 0f
        confidence += if (ringCurled) 0.15f else 0f
        confidence += if (littleCurled) 0.15f else 0f
        confidence += if (fingerCurlConsistency) 0.05f else 0f
        confidence += if (thumbTucked) 0.1f else 0f
        confidence += if (thumbCorrectPosition) 0.1f else 0f
        confidence += if (isThumbLower) 0.1f else 0f
        confidence += if (thumbNotCrossingPalm) 0.05f else 0f

        // Debug logging for sign A
        if (DEBUG && confidence > 0.5f) {
            Log.d(TAG, "Sign A details:")
            Log.d(TAG, "  Index curled: $indexCurled")
            Log.d(TAG, "  Middle curled: $middleCurled")
            Log.d(TAG, "  Ring curled: $ringCurled")
            Log.d(TAG, "  Little curled: $littleCurled")
            Log.d(TAG, "  Finger curl consistency: $fingerCurlConsistency")
            Log.d(TAG, "  Thumb tucked: $thumbTucked")
            Log.d(TAG, "  Thumb correct position: $thumbCorrectPosition ($thumbToIndexProximalDist)")
            Log.d(TAG, "  Thumb lower than fingers: $isThumbLower")
            Log.d(TAG, "  Thumb not crossing palm: $thumbNotCrossingPalm")
        }

        return confidence
    }

    /**
     * Confidence calculation for ASL Sign B
     * Hand held up, palm facing away, all fingers extended upward and together, with thumb tucked inward
     */
    private fun calculateSignBConfidence(
        joints: Map<HandJointType, Pose>,
        fingerExtensions: Map<String, Float>,
        fingerCurls: Map<String, Float>,
        palmOrientation: Vector3
    ): Float {
        var confidence = 0f

        // For Sign B: All fingers except thumb should be extended and straight
        val indexExtended = fingerExtensions["index"]!! > 1.6f && fingerCurls["index"]!! > 0.7f
        val middleExtended = fingerExtensions["middle"]!! > 1.6f && fingerCurls["middle"]!! > 0.7f
        val ringExtended = fingerExtensions["ring"]!! > 1.6f && fingerCurls["ring"]!! > 0.7f
        val littleExtended = fingerExtensions["little"]!! > 1.6f && fingerCurls["little"]!! > 0.7f

        // Thumb should be tucked across palm
        val thumbTucked = fingerCurls["thumb"]!! < 0.5f

        // Fingers should be close together
        val fingersClose = areFingersTogether(joints)

        // Palm should be facing forward/away
        val palmFacingAway = palmOrientation.z < -0.7f

        // Calculate confidence based on all criteria
        confidence += if (indexExtended) 0.15f else 0f
        confidence += if (middleExtended) 0.15f else 0f
        confidence += if (ringExtended) 0.15f else 0f
        confidence += if (littleExtended) 0.15f else 0f
        confidence += if (thumbTucked) 0.2f else 0f
        confidence += if (fingersClose) 0.1f else 0f
        confidence += if (palmFacingAway) 0.1f else 0f

        return confidence
    }

    /**
     * Confidence calculation for ASL Sign C
     * Hand with fingers together and curved in a C shape
     */
    private fun calculateSignCConfidence(
        joints: Map<HandJointType, Pose>,
        fingerExtensions: Map<String, Float>,
        fingerCurls: Map<String, Float>,
        palmOrientation: Vector3
    ): Float {
        var confidence = 0f

        // For Sign C: Fingers should be extended but curved
        val indexCurved = fingerExtensions["index"]!! > 1.3f && fingerCurls["index"]!! < 0.7f && fingerCurls["index"]!! > 0.3f
        val middleCurved = fingerExtensions["middle"]!! > 1.3f && fingerCurls["middle"]!! < 0.7f && fingerCurls["middle"]!! > 0.3f
        val ringCurved = fingerExtensions["ring"]!! > 1.3f && fingerCurls["ring"]!! < 0.7f && fingerCurls["ring"]!! > 0.3f
        val littleCurved = fingerExtensions["little"]!! > 1.3f && fingerCurls["little"]!! < 0.7f && fingerCurls["little"]!! > 0.3f

        // Thumb should also be curved and aligned with fingers
        val thumbCurved = fingerCurls["thumb"]!! < 0.7f && fingerCurls["thumb"]!! > 0.3f

        // Fingers should be close together
        val fingersClose = areFingersTogether(joints)

        // Palm should be facing to the side
        val palmFacingSide = abs(palmOrientation.x) > 0.7f

        // Thumb and pinky should be relatively close (forming the C)
        val thumbPos = joints[HandJointType.THUMB_TIP]!!.translation
        val pinkyPos = joints[HandJointType.LITTLE_TIP]!!.translation
        val thumbToPinkyDist = distance(thumbPos, pinkyPos)
        val cShapeFormed = thumbToPinkyDist < 0.15f

        // Calculate confidence based on all criteria
        confidence += if (indexCurved) 0.1f else 0f
        confidence += if (middleCurved) 0.1f else 0f
        confidence += if (ringCurved) 0.1f else 0f
        confidence += if (littleCurved) 0.1f else 0f
        confidence += if (thumbCurved) 0.1f else 0f
        confidence += if (fingersClose) 0.1f else 0f
        confidence += if (palmFacingSide) 0.2f else 0f
        confidence += if (cShapeFormed) 0.2f else 0f

        return confidence
    }

    /**
     * Confidence calculation for ASL Sign D
     * Index finger pointing up with the thumb and other fingers making a small O shape
     */
    private fun calculateSignDConfidence(
        joints: Map<HandJointType, Pose>,
        fingerExtensions: Map<String, Float>,
        fingerCurls: Map<String, Float>,
        palmOrientation: Vector3
    ): Float {
        var confidence = 0f

        // For Sign D: Index finger should be extended, others curled
        val indexExtended = fingerExtensions["index"]!! > 1.5f && fingerCurls["index"]!! > 0.7f
        val middleCurled = fingerCurls["middle"]!! < 0.5f
        val ringCurled = fingerCurls["ring"]!! < 0.5f
        val littleCurled = fingerCurls["little"]!! < 0.5f

        // Thumb should be extended but curved towards index
        val thumbPosition = fingerCurls["thumb"]!! < 0.7f && fingerCurls["thumb"]!! > 0.3f

        // Thumb and middle finger should be close (forming the small O)
        val thumbPos = joints[HandJointType.THUMB_TIP]!!.translation
        val middlePos = joints[HandJointType.MIDDLE_TIP]!!.translation
        val thumbToMiddleDist = distance(thumbPos, middlePos)
        val oShapeFormed = thumbToMiddleDist < 0.05f

        // Palm should be facing sideways
        val palmFacingSide = abs(palmOrientation.x) > 0.7f

        // Calculate confidence based on all criteria
        confidence += if (indexExtended) 0.25f else 0f
        confidence += if (middleCurled) 0.15f else 0f
        confidence += if (ringCurled) 0.15f else 0f
        confidence += if (littleCurled) 0.15f else 0f
        confidence += if (thumbPosition) 0.1f else 0f
        confidence += if (oShapeFormed) 0.1f else 0f
        confidence += if (palmFacingSide) 0.1f else 0f

        return confidence
    }

    /**
     * IMPROVED Confidence calculation for ASL Sign E
     * Fingers curled in toward palm, thumb tucked across fingers
     */
    private fun calculateSignEConfidence(
        joints: Map<HandJointType, Pose>,
        fingerExtensions: Map<String, Float>,
        fingerCurls: Map<String, Float>,
        palmOrientation: Vector3,
        thumbPositionData: Map<String, Float>
    ): Float {
        var confidence = 0f

        // REVISED: For Sign E: All fingers should be curled, but not as tight as a fist
        // Widened the range for partly curled fingers
        val indexPartlyCurled = fingerCurls["index"]!! < 0.65f && fingerCurls["index"]!! > 0.15f
        val middlePartlyCurled = fingerCurls["middle"]!! < 0.65f && fingerCurls["middle"]!! > 0.15f
        val ringPartlyCurled = fingerCurls["ring"]!! < 0.65f && fingerCurls["ring"]!! > 0.15f
        val littlePartlyCurled = fingerCurls["little"]!! < 0.65f && fingerCurls["little"]!! > 0.15f

        // IMPROVED: Thumb position checks for E sign
        // Thumb should be tucked across fingers
        val thumbPosition = fingerCurls["thumb"]!! < 0.7f

        // REVISED: Thumb should be positioned near middle finger metacarpal or proximal
        // Increased threshold and checked multiple points
        val thumbToMiddleProximalDist = thumbPositionData["thumb_to_middle_proximal"] ?: 1.0f

        // Added more alternatives for thumb placement
        val thumbToIndexProximalDist = thumbPositionData["thumb_to_index_proximal"] ?: 1.0f
        val thumbPlacement = (thumbToMiddleProximalDist < 0.12f || thumbToIndexProximalDist < 0.12f)

        // ADDED: For E, the thumb should cross the palm significantly
        val thumbCrossingPalm = (thumbPositionData["thumb_crossing_palm"] ?: 0f) > 0.3f

        // REVISED: Palm orientation check
        // Palm should be facing to the side or slightly forward
        val palmOriented = (palmOrientation.z < -0.3f) || (abs(palmOrientation.x) > 0.5f)

        // Calculate confidence based on all criteria
        confidence += if (indexPartlyCurled) 0.12f else 0f
        confidence += if (middlePartlyCurled) 0.12f else 0f
        confidence += if (ringPartlyCurled) 0.12f else 0f
        confidence += if (littlePartlyCurled) 0.12f else 0f
        confidence += if (thumbPosition) 0.12f else 0f
        confidence += if (thumbPlacement) 0.15f else 0f
        confidence += if (thumbCrossingPalm) 0.15f else 0f
        confidence += if (palmOriented) 0.1f else 0f

        // Debug logging for sign E
        if (DEBUG && confidence > 0.5f) {
            Log.d(TAG, "Sign E details:")
            Log.d(TAG, "  Index partly curled: $indexPartlyCurled")
            Log.d(TAG, "  Middle partly curled: $middlePartlyCurled")
            Log.d(TAG, "  Ring partly curled: $ringPartlyCurled")
            Log.d(TAG, "  Little partly curled: $littlePartlyCurled")
            Log.d(TAG, "  Thumb position: $thumbPosition")
            Log.d(TAG, "  Thumb placement: $thumbPlacement")
            Log.d(TAG, "  Thumb to middle proximal dist: $thumbToMiddleProximalDist")
            Log.d(TAG, "  Thumb to index proximal dist: $thumbToIndexProximalDist")
            Log.d(TAG, "  Thumb crossing palm: $thumbCrossingPalm")
            Log.d(TAG, "  Palm oriented correctly: $palmOriented")
        }

        return confidence
    }

    /**
     * Check if fingers are positioned close together
     */
    private fun areFingersTogether(joints: Map<HandJointType, Pose>): Boolean {
        val indexTip = joints[HandJointType.INDEX_TIP]!!.translation
        val middleTip = joints[HandJointType.MIDDLE_TIP]!!.translation
        val ringTip = joints[HandJointType.RING_TIP]!!.translation
        val littleTip = joints[HandJointType.LITTLE_TIP]!!.translation

        val indexToMiddle = distance(indexTip, middleTip)
        val middleToRing = distance(middleTip, ringTip)
        val ringToLittle = distance(ringTip, littleTip)

        // Increased thresholds
        return indexToMiddle < 0.06f && middleToRing < 0.06f && ringToLittle < 0.06f
    }

    /**
     * Check if a set of values are similar to each other (within maxDifference)
     */
    private fun areValuesSimilar(values: List<Float>, maxDifference: Float): Boolean {
        if (values.isEmpty()) return true
        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        return (max - min) <= maxDifference
    }

    /**
     * Gets the curl value for a finger (lower values indicate more curled)
     */
    private fun getFingerCurlValue(
        joints: Map<HandJointType, Pose>,
        proximalType: HandJointType,
        intermediateType: HandJointType,
        tipType: HandJointType
    ): Float {
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
     * Calculate angle between two vectors in degrees
     */
    private fun angleBetweenVectors(v1: Vector3, v2: Vector3): Float {
        val dot = dotProduct(normalize(v1), normalize(v2))
        return acos(dot.coerceIn(-1f, 1f)) * (180f / Math.PI.toFloat())
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
    private fun normalize(v: Vector3): Vector3 {
        val length = sqrt((v.x * v.x + v.y * v.y + v.z * v.z).toDouble()).toFloat()
        return if (length > 0.000001f) {
            Vector3(v.x / length, v.y / length, v.z / length)
        } else {
            Vector3(0f, 0f, 0f)
        }
    }

    /**
     * Calculates the dot product of two vectors.
     */
    private fun dotProduct(v1: Vector3, v2: Vector3): Float {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z
    }

    /**
     * Calculates the cross product of two vectors.
     */
    private fun crossProduct(v1: Vector3, v2: Vector3): Vector3 {
        return Vector3(
            v1.y * v2.z - v1.z * v2.y,
            v1.z * v2.x - v1.x * v2.z,
            v1.x * v2.y - v1.y * v2.x
        )
    }

    /**
     * Calculates the Euclidean distance between two points in 3D space.
     */
    private fun distance(point1: Vector3, point2: Vector3): Float {
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        val dz = point2.z - point1.z
        return sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    /**
     * Format a float to specified decimal places
     */
    private fun Float.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}