package com.example.xrexp.arcore.asl2

import androidx.xr.runtime.math.Vector3

// Core data structures for hand metrics analysis

/**
 * Describes the expected hand configuration for a specific ASL sign.
 *
 * @property sign The ASL sign being described
 * @property fingerExtensions Expected extension ranges for each finger (0-1)
 * @property fingerCurls Expected curl ranges for each finger (0-1)
 * @property palmOrientations Allowed palm orientations for this sign
 * @property customEvaluator Optional function for complex sign evaluation logic
 */
data class ASL2SignDescriptor(
    val sign: ASLSign,
    val fingerExtensions: Map<FingerType, Range<Float>>,
    val fingerCurls: Map<FingerType, Range<Float>>,
    val palmOrientations: Set<PalmOrientation>,
    val customEvaluator: ((HandMetrics) -> Float)? = null  // For complex signs
)

data class FingerMetrics(
    val extension: Float, // 0-1 where 1 is fully extended
    val curl: Float,      // 0-1 where 1 is fully curled
    val direction: Vector3 // Direction the finger is pointing
)

/**
 * Comprehensive metrics describing hand pose and finger positions.
 *
 * @property thumb Metrics for thumb finger
 * @property index Metrics for index finger
 * @property middle Metrics for middle finger
 * @property ring Metrics for ring finger
 * @property pinky Metrics for pinky finger
 * @property palmOrientation Direction the palm is facing
 * @property handOrientation Overall orientation of the hand
 * @property fingerDistances Map of distances between finger tips
 * @property fingerTouchingPalm Map indicating which fingers are touching the palm
 * @property relativeFingerHeights Map of finger heights relative to the wrist
 */
data class HandMetrics(
    val thumb: FingerMetrics,
    val index: FingerMetrics,
    val middle: FingerMetrics,
    val ring: FingerMetrics,
    val pinky: FingerMetrics,
    val palmOrientation: PalmOrientation,
    val handOrientation: HandOrientation,
    val fingerDistances: Map<Pair<FingerType, FingerType>, Float>,
    val fingerTouchingPalm: Map<FingerType, Boolean> = emptyMap(),  // Added
    val relativeFingerHeights: Map<FingerType, Float> = emptyMap()  // Added
)

// Orientation enums
enum class PalmOrientation {
    FACING_USER, FACING_AWAY, FACING_UP, FACING_DOWN, FACING_LEFT, FACING_RIGHT
}

enum class HandOrientation {
    UPRIGHT, SIDEWAYS, UPSIDE_DOWN, ROTATED_LEFT, ROTATED_RIGHT
}

enum class FingerType {
    THUMB, INDEX, MIDDLE, RING, PINKY
}

// ASL Signs enum
enum class ASLSign {
    // Letters
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
    // Numbers
    NUMBER_0, NUMBER_1, NUMBER_2, NUMBER_3, NUMBER_4, NUMBER_5,
    NUMBER_6, NUMBER_7, NUMBER_8, NUMBER_9,
    // No detection
    NONE
}

// Detection results
data class SignCandidate(
    val sign: ASLSign,
    val confidence: Float, // 0-1 where 1 is 100% confidence
    val debugInfo: Map<String, Any> = emptyMap() // For debugging visualization
)

data class DetectionResult(
    val primaryCandidate: SignCandidate?,
    val alternativeCandidates: List<SignCandidate>,
    val handMetrics: HandMetrics
)

/**
 * Configurable parameters for tuning the sensitivity of ASL sign detection.
 *
 * @property confidenceThreshold Minimum confidence required to consider a sign detected (0-1)
 * @property fingerExtensionThreshold Threshold for considering a finger extended (0-1)
 * @property fingerCurlThreshold Threshold for considering a finger curled (0-1)
 * @property distanceThreshold Threshold for finger proximity detection in meters
 */
data class DetectionParameters(
    val confidenceThreshold: Float = 0.7f,
    val fingerExtensionThreshold: Float = 0.7f,
    val fingerCurlThreshold: Float = 0.7f,
    val distanceThreshold: Float = 0.05f
)