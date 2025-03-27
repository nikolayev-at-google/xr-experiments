package com.example.xrexp.arcore.asl2

import android.annotation.SuppressLint
import androidx.xr.arcore.Hand
import androidx.xr.arcore.HandJointType
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import androidx.xr.runtime.math.toDegrees
import kotlin.math.abs


/**
 * Detects American Sign Language (ASL) signs from hand tracking data.
 *
 * This detector analyzes hand joint positions and orientations to identify
 * ASL alphabet letters (A-Z) and numbers (0-9). It compares measured hand
 * metrics against defined sign descriptors to calculate confidence values
 * for potential matches.
 *
 * @property parameters Configurable detection thresholds and sensitivity parameters
 */
@SuppressLint("RestrictedApi")
class ASLSignDetector(
    private var parameters: DetectionParameters = DetectionParameters()
) {

    /*
        Logic Flow
        The ASL sign detection process follows these steps:

        1. Hand State Input: The detector receives hand tracking data from ARCore/Android XR libraries
        2. Metrics Extraction: Joint positions are processed to calculate finger extensions, curls, orientations, etc.
        3. Sign Evaluation: Each potential sign is evaluated against the current hand metrics
        4. Confidence Calculation: Confidence scores are determined for each sign based on how well the metrics match known descriptors
        5. Result Filtering: Results are filtered by confidence threshold and sorted
        6. Result Generation: The detector outputs the highest confidence sign and alternative candidates for visualization/debugging
     */
    private val signLibrary = buildSignLibrary()

    /**
     * Processes hand tracking data to detect ASL signs.
     *
     * This function analyzes the current hand state, extracts hand metrics,
     * evaluates candidate signs, and returns a structured detection result.
     *
     * @param handState Current state of hand tracking from ARCore/Android XR
     * @return DetectionResult containing primary and alternative sign candidates
     */
    fun detectSign(handState: Hand.State): DetectionResult {
        /*
            Logic Flow:

            1. Extracts hand metrics from the hand state
            2. Evaluates each sign descriptor against the metrics
            3. Filters and sorts candidates by confidence
            4. Determines primary candidate if confidence exceeds threshold
            5. Returns detection result with primary and alternative candidates
         */

        // Extract hand metrics from hand state
        val handMetrics = extractHandMetrics(handState)

        // Evaluate each sign
        val candidates = signLibrary.map { descriptor ->
            val confidence = evaluateSign(handMetrics, descriptor)
            val debugInfo = generateDebugInfo(handMetrics, descriptor)
            SignCandidate(descriptor.sign, confidence, debugInfo)
        }.filter { it.confidence > 0.1f } // Filter low confidence

        // Sort by confidence
        val sortedCandidates = candidates.sortedByDescending { it.confidence }

        // Get primary candidate if it exceeds threshold
        val primaryCandidate = sortedCandidates.firstOrNull()?.let {
            if (it.confidence >= parameters.confidenceThreshold) it else null
        }

        return DetectionResult(
            primaryCandidate = primaryCandidate,
            alternativeCandidates = sortedCandidates.take(5),
            handMetrics = handMetrics
        )
    }

    /**
     * Updates the detection parameters.
     *
     * This function allows adjusting sensitivity settings during runtime.
     *
     * @param parameters New detection parameters
     */
    fun updateParameters(parameters: DetectionParameters) {
        /*
            Logic Flow:

            1. Replaces current parameters with new parameters
            2. Updates internal state if needed
         */
        this.parameters = parameters
    }

    /**
     * Extracts comprehensive hand metrics from raw hand joint data.
     *
     * This function processes the hand state to calculate finger extensions, curls,
     * palm orientation, and additional measurements used for sign detection.
     *
     * @param handState Current state of hand tracking
     * @return HandMetrics containing calculated metrics or default metrics if tracking fails
     */
    private fun extractHandMetrics(handState: Hand.State): HandMetrics {

        /*
            Logic Flow:

            1. Extracts joint positions for each finger
            2. Calculates metrics (extension, curl, direction) for each finger
            3. Determines palm and hand orientation
            4. Calculates distances between fingertips
            5. Checks which fingers are touching the palm
            6. Calculates relative heights of fingertips
            7. Returns comprehensive hand metrics
         */

        val handJoints = handState.handJoints

        // Extract wrist position
        val wristPose = handJoints[HandJointType.WRIST] ?: return createDefaultHandMetrics()

        // Extract all joint positions for each finger
        val thumbJoints = extractFingerJoints(handJoints, "THUMB")
        val indexJoints = extractFingerJoints(handJoints, "INDEX")
        val middleJoints = extractFingerJoints(handJoints, "MIDDLE")
        val ringJoints = extractFingerJoints(handJoints, "RING")
        val pinkyJoints = extractFingerJoints(handJoints, "LITTLE")

        // Calculate metrics for each finger
        val thumbMetrics = calculateFingerMetrics(thumbJoints)
        val indexMetrics = calculateFingerMetrics(indexJoints)
        val middleMetrics = calculateFingerMetrics(middleJoints)
        val ringMetrics = calculateFingerMetrics(ringJoints)
        val pinkyMetrics = calculateFingerMetrics(pinkyJoints)

        // Get fingertips
        val thumbTip = thumbJoints.lastOrNull() ?: wristPose
        val indexTip = indexJoints.lastOrNull() ?: wristPose
        val middleTip = middleJoints.lastOrNull() ?: wristPose
        val ringTip = ringJoints.lastOrNull() ?: wristPose
        val pinkyTip = pinkyJoints.lastOrNull() ?: wristPose

        // Get metacarpals
        val indexMetacarpal = handJoints[HandJointType.INDEX_METACARPAL] ?: wristPose
        val middleMetacarpal = handJoints[HandJointType.MIDDLE_METACARPAL] ?: wristPose
        val littleMetacarpal = handJoints[HandJointType.LITTLE_METACARPAL] ?: wristPose

        // Calculate palm orientation
        val palmOrientation = calculatePalmOrientation(
            wristPose,
            indexMetacarpal,
            littleMetacarpal
        )

        // Calculate hand orientation
        val handOrientation = calculateHandOrientation(
            wristPose,
            middleMetacarpal
        )

        // Calculate finger distances
        val fingerDistances = calculateFingerDistances(
            thumbTip,
            indexTip,
            middleTip,
            ringTip,
            pinkyTip
        )

        // Calculate additional metrics
        val fingerTouchingPalm = mapOf(
            FingerType.THUMB to isFingerTouchingPalm(thumbTip, wristPose, indexMetacarpal, littleMetacarpal),
            FingerType.INDEX to isFingerTouchingPalm(indexTip, wristPose, indexMetacarpal, littleMetacarpal),
            FingerType.MIDDLE to isFingerTouchingPalm(middleTip, wristPose, indexMetacarpal, littleMetacarpal),
            FingerType.RING to isFingerTouchingPalm(ringTip, wristPose, indexMetacarpal, littleMetacarpal),
            FingerType.PINKY to isFingerTouchingPalm(pinkyTip, wristPose, indexMetacarpal, littleMetacarpal)
        )

        val relativeFingerHeights = calculateRelativeFingerHeights(
            thumbTip, indexTip, middleTip, ringTip, pinkyTip, wristPose
        )

        // Create enhanced HandMetrics with additional properties
        return HandMetrics(
            thumb = thumbMetrics,
            index = indexMetrics,
            middle = middleMetrics,
            ring = ringMetrics,
            pinky = pinkyMetrics,
            palmOrientation = palmOrientation,
            handOrientation = handOrientation,
            fingerDistances = fingerDistances,
            fingerTouchingPalm = fingerTouchingPalm,          // Added
            relativeFingerHeights = relativeFingerHeights     // Added
        )
    }

    /**
     * Evaluates how well the current hand metrics match a sign descriptor.
     *
     * This function computes a confidence value (0-1) for how closely the
     * current hand configuration matches the expected configuration for a sign.
     *
     * @param metrics Current hand metrics
     * @param descriptor Sign descriptor to compare against
     * @return Confidence value between 0 (no match) and 1 (perfect match)
     */
    private fun evaluateSign(metrics: HandMetrics, descriptor: ASL2SignDescriptor): Float {

        /*
            Logic Flow:

            1. Uses custom evaluator if provided
            2. Otherwise, starts with maximum confidence (1.0)
            3. Reduces confidence for each finger extension that doesn't match expected range
            4. Reduces confidence for each finger curl that doesn't match expected range
            5. Reduces confidence if palm orientation doesn't match expected orientations
            6. Returns final confidence value
         */

        // Use custom evaluator if provided
        descriptor.customEvaluator?.let { return it(metrics) }

        var confidence = 1.0f

        // Evaluate finger extensions
        for ((fingerType, expectedRange) in descriptor.fingerExtensions) {
            val actualExtension = getFingerMetric(metrics, fingerType).extension

            if (!expectedRange.contains(actualExtension)) {
                // Reduce confidence based on distance from range
                val minDist = abs(actualExtension - expectedRange.min)
                val maxDist = abs(actualExtension - expectedRange.max)
                val distance = minDist.coerceAtMost(maxDist)
                confidence *= 0f.coerceAtLeast(1f - distance)
            }
        }

        // Evaluate finger curls
        for ((fingerType, expectedRange) in descriptor.fingerCurls) {
            val actualCurl = getFingerMetric(metrics, fingerType).curl

            if (!expectedRange.contains(actualCurl)) {
                val minDist = abs(actualCurl - expectedRange.min)
                val maxDist = abs(actualCurl - expectedRange.max)
                val distance = minDist.coerceAtMost(maxDist)
                confidence *= 0f.coerceAtLeast(1f - distance)
            }
        }

        // Evaluate palm orientation
        if (!descriptor.palmOrientations.contains(metrics.palmOrientation)) {
            confidence *= 0.5f // Significant penalty for wrong orientation
        }

        return confidence
    }

    // Helper functions...

    /**
     * Extracts joint positions for a specific finger.
     *
     * This function maps HandJointType enum values to create a list of poses
     * representing a finger's joints from wrist to fingertip.
     *
     * @param handJoints Map of all hand joint positions from tracking
     * @param fingerPrefix String identifier of finger ("THUMB", "INDEX", etc.)
     * @return List of Pose objects for the finger's joints in order (wrist to tip)
     */
    private fun extractFingerJoints(handJoints: Map<HandJointType, Pose>, fingerPrefix: String): List<Pose> {

        /*
            Logic Flow:

            1. Determines the joint types for the specified finger
            2. Maps joint types to their corresponding poses from the hand joint map
            3. Returns the list of joint poses in proper order (wrist to fingertip)
         */

        // Extract all joints for a specific finger
        // Return in order from base to tip
        // Implementation depends on HandJointType enum structure
        // Based on common hand tracking joint naming conventions
        val jointTypes = when (fingerPrefix) {
            "THUMB" -> listOf(
                HandJointType.WRIST,
                HandJointType.THUMB_METACARPAL,
                HandJointType.THUMB_PROXIMAL,
                HandJointType.THUMB_DISTAL,
                HandJointType.THUMB_TIP
            )
            "INDEX" -> listOf(
                HandJointType.WRIST,
                HandJointType.INDEX_METACARPAL,
                HandJointType.INDEX_PROXIMAL,
                HandJointType.INDEX_INTERMEDIATE,
                HandJointType.INDEX_DISTAL,
                HandJointType.INDEX_TIP
            )
            "MIDDLE" -> listOf(
                HandJointType.WRIST,
                HandJointType.MIDDLE_METACARPAL,
                HandJointType.MIDDLE_PROXIMAL,
                HandJointType.MIDDLE_INTERMEDIATE,
                HandJointType.MIDDLE_DISTAL,
                HandJointType.MIDDLE_TIP
            )
            "RING" -> listOf(
                HandJointType.WRIST,
                HandJointType.RING_METACARPAL,
                HandJointType.RING_PROXIMAL,
                HandJointType.RING_INTERMEDIATE,
                HandJointType.RING_DISTAL,
                HandJointType.RING_TIP
            )
            "LITTLE" -> listOf(  // Changed from "PINKY_FINGER" to "LITTLE"
                HandJointType.WRIST,
                HandJointType.LITTLE_METACARPAL,
                HandJointType.LITTLE_PROXIMAL,
                HandJointType.LITTLE_INTERMEDIATE,
                HandJointType.LITTLE_DISTAL,
                HandJointType.LITTLE_TIP
            )
            else -> emptyList()
        }

        // Get all joints in sequence from base to tip
        return jointTypes.mapNotNull { jointType ->
            handJoints[jointType]
        }
    }

    /**
     * Calculates extension, curl, and direction metrics for a finger.
     *
     * This function analyzes a finger's joint positions to determine how
     * extended or curled the finger is, and which direction it's pointing.
     *
     * @param jointPoses List of poses for the finger's joints (wrist to tip)
     * @return FingerMetrics containing extension (0-1), curl (0-1), and direction
     */
    private fun calculateFingerMetrics(jointPoses: List<Pose>): FingerMetrics {

        /*
            Logic Flow:

            1. Calculates the total length of the finger when fully extended
            2. Calculates the current end-to-end distance from base to tip
            3. Determines extension as the ratio of current distance to full length
            4. Calculates curl using joint angles or as inverse of extension
            5. Determines direction as normalized vector from base to tip
            6. Returns combined finger metrics
         */

        // Need at least 3 joints to calculate metrics
        if (jointPoses.size < 3) {
            return FingerMetrics(0f, 0f, Vector3.Zero)
        }

        // Calculate finger length (sum of segment lengths)
        var totalLength = 0f
        for (i in 0 until jointPoses.size - 1) {
            val segment = jointPoses[i+1].translation - jointPoses[i].translation
            totalLength += segment.length
        }

        // Calculate current end-to-end distance (base to tip)
        val baseToTipVector = jointPoses.last().translation - jointPoses.first().translation
        val baseToTipDistance = baseToTipVector.length

        // Calculate extension (ratio of current end-to-end distance to full extension)
        // Normalized to 0-1 range
        val extension = (baseToTipDistance / totalLength).coerceIn(0f, 1f)

        // Calculate curl using our improved joint angle method
        val curl = if (jointPoses.size >= 5) {
            calculateFingerCurl(jointPoses)
        } else {
            // Fallback to simpler method if we don't have enough joints
            (1f - extension).coerceIn(0f, 1f)
        }

        // Calculate direction (normalized vector from base to tip)
        val direction = if (baseToTipDistance > 0.01f) {
            baseToTipVector.toNormalized()
        } else {
            // Fallback if finger is fully curled
            val palmToFingerBase = jointPoses[1].translation - jointPoses[0].translation
            palmToFingerBase.toNormalized()
        }

        return FingerMetrics(extension, curl, direction)
    }

    /**
     * Determines which direction the palm is facing.
     *
     * This function calculates the palm normal vector to determine
     * if the palm is facing the user, away, up, down, left, or right.
     *
     * @param wristPose Pose of the wrist joint
     * @param indexMCP Pose of the index finger metacarpal joint
     * @param pinkyMCP Pose of the little finger metacarpal joint
     * @return PalmOrientation enum value
     */
    private fun calculatePalmOrientation(wristPose: Pose, indexMCP: Pose, pinkyMCP: Pose): PalmOrientation {

        /*
            Logic Flow:

            1. Calculates vectors from wrist to index and pinky metacarpals
            2. Computes palm normal vector using cross product
            3. Analyzes the dominant component of the normal vector
            4. Returns appropriate palm orientation enum
         */

        // Calculate palm normal and determine orientation
        // Implementation uses cross product and dot products
        // Calculate palm normal vector (cross product of two vectors on palm plane)
        val wristToIndex = indexMCP.translation - wristPose.translation
        val wristToPinky = pinkyMCP.translation - wristPose.translation

        // Palm normal is perpendicular to the palm plane
        val palmNormal = wristToIndex.cross(wristToPinky).toNormalized()

        // Calculate the absolute components for clearer comparisons
        val absX = abs(palmNormal.x)
        val absY = abs(palmNormal.y)
        val absZ = abs(palmNormal.z)

        // Determine orientation based on largest component
        return when {
            // Z-axis is primary
            absZ > absX && absZ > absY -> {
                if (palmNormal.z > 0) PalmOrientation.FACING_AWAY
                else PalmOrientation.FACING_USER
            }
            // Y-axis is primary
            absY > absX && absY > absZ -> {
                if (palmNormal.y > 0) PalmOrientation.FACING_DOWN
                else PalmOrientation.FACING_UP
            }
            // X-axis is primary
            else -> {
                if (palmNormal.x > 0) PalmOrientation.FACING_RIGHT
                else PalmOrientation.FACING_LEFT
            }
        }
    }

    /**
     * Determines the overall orientation of the hand.
     *
     * This function analyzes the direction from wrist to middle finger
     * to determine if the hand is upright, upside down, or rotated.
     *
     * @param wristPose Pose of the wrist joint
     * @param middleMetacarpal Pose of the middle finger metacarpal joint
     * @return HandOrientation enum value
     */
    private fun calculateHandOrientation(wristPose: Pose, middleMetacarpal: Pose): HandOrientation {

        /*
            Logic Flow:

            1. Calculates vector from wrist to middle finger metacarpal
            2. Analyzes x and y components of the normalized vector
            3. Returns appropriate hand orientation enum
         */

        // Calculate overall hand orientation
        // Get direction from wrist to middle finger MCP (base of middle finger)
        val wristToMiddle = (middleMetacarpal.translation - wristPose.translation).toNormalized()

        // Y component determines if hand is upright, upside down, or sideways
        val yComponent = wristToMiddle.y
        val xComponent = wristToMiddle.x

        return when {
            yComponent > 0.7f -> HandOrientation.UPRIGHT
            yComponent < -0.7f -> HandOrientation.UPSIDE_DOWN
            xComponent > 0.7f -> HandOrientation.ROTATED_RIGHT
            xComponent < -0.7f -> HandOrientation.ROTATED_LEFT
            else -> HandOrientation.SIDEWAYS
        }
    }

    /**
     * Calculates distances between fingertips.
     *
     * This function computes the Euclidean distance between each pair of
     * fingertips, which is useful for detecting signs where fingers touch.
     *
     * @param thumbTip Pose of the thumb tip
     * @param indexTip Pose of the index finger tip
     * @param middleTip Pose of the middle finger tip
     * @param ringTip Pose of the ring finger tip
     * @param pinkyTip Pose of the pinky finger tip
     * @return Map of finger pairs to distances in meters
     */
    private fun calculateFingerDistances(
        thumbTip: Pose, indexTip: Pose, middleTip: Pose,
        ringTip: Pose, pinkyTip: Pose
    ): Map<Pair<FingerType, FingerType>, Float> {

        /*
            Logic Flow:

            1. Maps each finger type to its tip pose
            2. Iterates through all pairs of fingers (avoiding duplicates)
            3. Calculates the Euclidean distance between each pair of fingertips
            4. Returns map of finger pairs to distances
         */

        // Calculate distances between fingertips
        val distances = mutableMapOf<Pair<FingerType, FingerType>, Float>()

        // Gather all fingertip positions
        val fingerTips = mapOf(
            FingerType.THUMB to thumbTip,
            FingerType.INDEX to indexTip,
            FingerType.MIDDLE to middleTip,
            FingerType.RING to ringTip,
            FingerType.PINKY to pinkyTip
        )

        // Calculate distances between all pairs of fingertips
        for (finger1 in FingerType.entries) {
            for (finger2 in FingerType.entries) {
                if (finger1 != finger2 &&
                    finger1.ordinal <= finger2.ordinal) { // Avoid duplicates

                    val pose1 = fingerTips[finger1]
                    val pose2 = fingerTips[finger2]

                    if (pose1 != null && pose2 != null) {
                        val distance = Vector3.distance(
                            pose1.translation,
                            pose2.translation
                        )
                        distances[Pair(finger1, finger2)] = distance
                    }
                }
            }
        }

        return distances
    }

    /**
     * Calculates the angle between three connected joints.
     *
     * This function determines the angle formed at joint2 between the vectors
     * joint1→joint2 and joint2→joint3.
     *
     * @param joint1 First joint pose
     * @param joint2 Middle joint pose (where angle is measured)
     * @param joint3 Third joint pose
     * @return Angle in degrees
     */
    private fun calculateJointAngle(joint1: Pose, joint2: Pose, joint3: Pose): Float {

        /*
            Logic Flow:

            1. Calculates vectors for the two bone segments
            2. Normalizes the vectors
            3. Calculates dot product between normalized vectors
            4. Calculates and returns angle in degrees
         */

        // Get vectors for two bone segments
        val segment1 = joint1.translation - joint2.translation
        val segment2 = joint3.translation - joint2.translation

        // Normalize vectors
        val norm1 = segment1.toNormalized()
        val norm2 = segment2.toNormalized()

        // Calculate dot product
        val dotProduct = norm1.dot(norm2)

        // Calculate angle in degrees
        val angleRadians = kotlin.math.acos(dotProduct.coerceIn(-1f, 1f))
        return toDegrees(angleRadians)
    }

    /**
     * Calculates curl value based on joint angles.
     *
     * This function uses the angles between finger segments to determine
     * how curled a finger is, providing a more accurate measurement than
     * the simple extension-based approach.
     *
     * @param jointPoses List of poses for the finger's joints (wrist to tip)
     * @return Curl value between 0 (straight) and 1 (fully curled)
     */
    private fun calculateFingerCurl(jointPoses: List<Pose>): Float {

        /*
            Logic Flow:

            1. Calculates angles at each joint in the finger
            2. Normalizes angles to 0-1 scale (0° is straight, 90° is fully bent)
            3. Applies weighted average with emphasis on PIP and DIP joints
            4. Returns combined curl value
         */

        // Need at least wrist, metacarpal, proximal, intermediate and distal for full calculation
        if (jointPoses.size < 5) return 0f

        // Calculate angles at proximal and intermediate joints
        val mcpAngle = calculateJointAngle(jointPoses[1], jointPoses[2], jointPoses[3])
        val pipAngle = calculateJointAngle(jointPoses[2], jointPoses[3], jointPoses[4])

        var mcpCurl = 1f
        var pipCurl = 1f
        // For thumb, which has fewer joints
        if (jointPoses.size < 6) {
            // Normalize to 0-1 range (assuming 0° is straight, 90° is fully bent)
            mcpCurl = (mcpAngle / 90f).coerceIn(0f, 1f)
            pipCurl = (pipAngle / 90f).coerceIn(0f, 1f)

            // Weighted average
            return (0.4f * mcpCurl + 0.6f * pipCurl)
        }

        // For other fingers, also include distal joint angle
        val dipAngle = calculateJointAngle(jointPoses[3], jointPoses[4], jointPoses[5])
        val dipCurl = (dipAngle / 90f).coerceIn(0f, 1f)

        // Weighted average (more weight to PIP and DIP joints for curl)
        return (0.2f * mcpCurl + 0.4f * pipCurl + 0.4f * dipCurl)
    }

    /**
     * Determines if a fingertip is touching the palm.
     *
     * This function checks if the distance from a fingertip to the
     * approximate palm center is below a threshold.
     *
     * @param fingerTip Pose of the fingertip
     * @param wrist Pose of the wrist joint
     * @param indexMCP Pose of the index finger metacarpal
     * @param pinkyMCP Pose of the pinky finger metacarpal
     * @return True if the finger is touching the palm, false otherwise
     */
    private fun isFingerTouchingPalm(fingerTip: Pose, wrist: Pose, indexMCP: Pose, pinkyMCP: Pose): Boolean {

        /*
            Logic Flow:

            1. Calculates approximate palm center from wrist and metacarpal positions
            2. Computes distance from fingertip to palm center
            3. Returns true if distance is below threshold (typically 3cm)
         */

        // Calculate palm center as average of wrist, index MCP, and pinky MCP
        val palmCenter = Vector3(
            (wrist.translation.x + indexMCP.translation.x + pinkyMCP.translation.x) / 3f,
            (wrist.translation.y + indexMCP.translation.y + pinkyMCP.translation.y) / 3f,
            (wrist.translation.z + indexMCP.translation.z + pinkyMCP.translation.z) / 3f
        )

        // Calculate distance from fingertip to palm center
        val distance = Vector3.distance(fingerTip.translation, palmCenter)

        // Determine if distance is below threshold (needs calibration)
        // A reasonable starting threshold might be 2-3cm
        return distance < 0.03f // 3cm threshold
    }

    /**
     * Calculates the height of each fingertip relative to the wrist.
     *
     * This function computes the y-axis (vertical) offset of each fingertip
     * from the wrist, useful for signs where finger heights are significant.
     *
     * @param thumbTip Pose of the thumb tip
     * @param indexTip Pose of the index finger tip
     * @param middleTip Pose of the middle finger tip
     * @param ringTip Pose of the ring finger tip
     * @param pinkyTip Pose of the pinky finger tip
     * @param wristPose Pose of the wrist joint
     * @return Map of finger types to relative heights in meters
     */
    private fun calculateRelativeFingerHeights(
        thumbTip: Pose,
        indexTip: Pose,
        middleTip: Pose,
        ringTip: Pose,
        pinkyTip: Pose,
        wristPose: Pose
    ): Map<FingerType, Float> {

        /*
            Logic Flow:

            1. Gets wrist y-coordinate as reference
            2. Calculates y-coordinate difference for each fingertip
            3. Returns map of finger types to relative heights
         */

        val heights = mutableMapOf<FingerType, Float>()

        // Get wrist y position as reference
        val wristY = wristPose.translation.y

        // Calculate height of each fingertip relative to wrist
        heights[FingerType.THUMB] = thumbTip.translation.y - wristY
        heights[FingerType.INDEX] = indexTip.translation.y - wristY
        heights[FingerType.MIDDLE] = middleTip.translation.y - wristY
        heights[FingerType.RING] = ringTip.translation.y - wristY
        heights[FingerType.PINKY] = pinkyTip.translation.y - wristY

        return heights
    }

    private fun getFingerMetric(metrics: HandMetrics, fingerType: FingerType): FingerMetrics {
        return when (fingerType) {
            FingerType.THUMB -> metrics.thumb
            FingerType.INDEX -> metrics.index
            FingerType.MIDDLE -> metrics.middle
            FingerType.RING -> metrics.ring
            FingerType.PINKY -> metrics.pinky
        }
    }

    /**
     * Creates default hand metrics for when tracking fails.
     *
     * This function provides a fallback set of metrics when hand
     * tracking data is incomplete or unavailable.
     *
     * @return Default HandMetrics
     */
    private fun createDefaultHandMetrics(): HandMetrics {

        /*
            Logic Flow:

            1. Creates default finger metrics (all zeros)
            2. Sets default palm and hand orientations
            3. Returns completed default metrics
         */

        // Create a default metric object for when hand tracking fails
        return HandMetrics(
            thumb = FingerMetrics(0f, 0f, Vector3.Zero),
            index = FingerMetrics(0f, 0f, Vector3.Zero),
            middle = FingerMetrics(0f, 0f, Vector3.Zero),
            ring = FingerMetrics(0f, 0f, Vector3.Zero),
            pinky = FingerMetrics(0f, 0f, Vector3.Zero),
            palmOrientation = PalmOrientation.FACING_USER,
            handOrientation = HandOrientation.UPRIGHT,
            fingerDistances = emptyMap()
        )
    }

    /**
     * Generates detailed information for debugging visualization.
     *
     * This function creates a map of debug info showing how each aspect
     * of the current hand configuration compares to the expected configuration.
     *
     * @param metrics Current hand metrics
     * @param descriptor Sign descriptor being evaluated
     * @return Map of debug info keys to values
     */
    private fun generateDebugInfo(
        metrics: HandMetrics,
        descriptor: ASL2SignDescriptor
    ): Map<String, Any> {

        /*
            Logic Flow:

            1. Creates empty debug info map
            2. For each finger, adds extension and curl match information
            3. Adds information about palm orientation match
            4. Returns completed debug info map
         */

        // Generate detailed debug info for visualization
        val debugInfo = mutableMapOf<String, Any>()

        // For each finger, add extension and curl debug info
        for (fingerType in FingerType.entries) {
            val metric = getFingerMetric(metrics, fingerType)
            val expectedExtension = descriptor.fingerExtensions[fingerType]
            val expectedCurl = descriptor.fingerCurls[fingerType]

            // Add extension match info
            if (expectedExtension != null) {
                debugInfo["${fingerType.name}_EXTENSION_MATCH"] =
                    expectedExtension.contains(metric.extension)
                debugInfo["${fingerType.name}_EXTENSION"] = metric.extension
                debugInfo["${fingerType.name}_EXTENSION_TARGET"] =
                    "${expectedExtension.min} - ${expectedExtension.max}"
            }

            // Add curl match info
            if (expectedCurl != null) {
                debugInfo["${fingerType.name}_CURL_MATCH"] =
                    expectedCurl.contains(metric.curl)
                debugInfo["${fingerType.name}_CURL"] = metric.curl
                debugInfo["${fingerType.name}_CURL_TARGET"] =
                    "${expectedCurl.min} - ${expectedCurl.max}"
            }
        }

        // Add palm orientation debug info
        debugInfo["PALM_ORIENTATION"] = metrics.palmOrientation.name
        debugInfo["PALM_ORIENTATION_MATCH"] =
            descriptor.palmOrientations.contains(metrics.palmOrientation)

        return debugInfo
    }

    /**
     * Builds the library of ASL sign descriptors.
     *
     * This function creates descriptors for all supported ASL signs
     * (letters A-Z and numbers 0-9).
     *
     * @return List of sign descriptors
     */
    private fun buildSignLibrary(): List<ASL2SignDescriptor> {
        /*
            Logic Flow:

            1. Creates descriptors for each supported ASL sign
            2. Returns complete list of descriptors
         */
        // Build complete sign library with all ASL letter and number descriptors
        return listOf(
            ASL2SignDescriptors.signA, ASL2SignDescriptors.signB, ASL2SignDescriptors.signC,
            ASL2SignDescriptors.signD, ASL2SignDescriptors.signE, ASL2SignDescriptors.signF,
            ASL2SignDescriptors.signG
            // Add descriptors for all other signs and numbers
        )
    }
}