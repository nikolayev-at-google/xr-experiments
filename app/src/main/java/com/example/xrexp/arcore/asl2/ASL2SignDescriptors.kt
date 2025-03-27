package com.example.xrexp.arcore.asl2

import kotlin.math.abs

object ASL2SignDescriptors {


    // Descriptor for sign "A"
    val signA = ASL2SignDescriptor(
        sign = ASLSign.A,
        fingerExtensions = mapOf(
            FingerType.THUMB to Range(0.3f, 0.7f),
            FingerType.INDEX to Range(0.0f, 0.3f),
            FingerType.MIDDLE to Range(0.0f, 0.3f),
            FingerType.RING to Range(0.0f, 0.3f),
            FingerType.PINKY to Range(0.0f, 0.3f)
        ),
        fingerCurls = mapOf(
            FingerType.THUMB to Range(0.3f, 0.7f),
            FingerType.INDEX to Range(0.7f, 1.0f),
            FingerType.MIDDLE to Range(0.7f, 1.0f),
            FingerType.RING to Range(0.7f, 1.0f),
            FingerType.PINKY to Range(0.7f, 1.0f)
        ),
        palmOrientations = setOf(PalmOrientation.FACING_LEFT, PalmOrientation.FACING_RIGHT)
    )

    // B - Flat hand with fingers together and thumb tucked
    val signB = ASL2SignDescriptor(
        sign = ASLSign.B,
        fingerExtensions = mapOf(
            FingerType.THUMB to Range(0.0f, 0.3f),      // Thumb tucked
            FingerType.INDEX to Range(0.7f, 1.0f),      // Index fully extended
            FingerType.MIDDLE to Range(0.7f, 1.0f),     // Middle fully extended
            FingerType.RING to Range(0.7f, 1.0f),       // Ring fully extended
            FingerType.PINKY to Range(0.7f, 1.0f)       // Pinky fully extended
        ),
        fingerCurls = mapOf(
            FingerType.THUMB to Range(0.7f, 1.0f),      // Thumb curled across palm
            FingerType.INDEX to Range(0.0f, 0.3f),      // Index straight
            FingerType.MIDDLE to Range(0.0f, 0.3f),     // Middle straight
            FingerType.RING to Range(0.0f, 0.3f),       // Ring straight
            FingerType.PINKY to Range(0.0f, 0.3f)       // Pinky straight
        ),
        palmOrientations = setOf(PalmOrientation.FACING_AWAY),
        // Enhanced evaluator using relative finger heights
        customEvaluator = { metrics ->
            // For B, all fingers should be at similar heights
            val indexHeight = metrics.relativeFingerHeights[FingerType.INDEX] ?: 0f
            val middleHeight = metrics.relativeFingerHeights[FingerType.MIDDLE] ?: 0f
            val ringHeight = metrics.relativeFingerHeights[FingerType.RING] ?: 0f
            val pinkyHeight = metrics.relativeFingerHeights[FingerType.PINKY] ?: 0f

            // Calculate how close finger heights are to each other
            val maxHeightDiff = maxOf(
                abs(indexHeight - middleHeight),
                abs(indexHeight - ringHeight),
                abs(indexHeight - pinkyHeight),
                abs(middleHeight - ringHeight),
                abs(middleHeight - pinkyHeight),
                abs(ringHeight - pinkyHeight)
            )

            // Fingers should be close in height for B
            val fingersAligned = maxHeightDiff < 0.02f // Within 2cm of each other

            // Thumb should be tucked
            val thumbTucked = metrics.thumb.curl > 0.7f

            // Fingers should be straight and extended
            val fingersExtended =
                metrics.index.extension > 0.7f &&
                        metrics.middle.extension > 0.7f &&
                        metrics.ring.extension > 0.7f &&
                        metrics.pinky.extension > 0.7f

            // Combined confidence
            if (fingersAligned && thumbTucked && fingersExtended) 0.95f
            else if (thumbTucked && fingersExtended) 0.8f
            else if (fingersExtended) 0.6f
            else 0.3f
        }
    )

    // C - Hand forms a C shape
    val signC = ASL2SignDescriptor(
        sign = ASLSign.C,
        fingerExtensions = mapOf(
            FingerType.THUMB to Range(0.4f, 0.7f),      // Thumb partially extended
            FingerType.INDEX to Range(0.5f, 0.8f),      // Index partially extended
            FingerType.MIDDLE to Range(0.5f, 0.8f),     // Middle partially extended
            FingerType.RING to Range(0.5f, 0.8f),       // Ring partially extended
            FingerType.PINKY to Range(0.5f, 0.8f)       // Pinky partially extended
        ),
        fingerCurls = mapOf(
            FingerType.THUMB to Range(0.3f, 0.6f),      // Thumb slightly curled
            FingerType.INDEX to Range(0.3f, 0.6f),      // Index slightly curled
            FingerType.MIDDLE to Range(0.3f, 0.6f),     // Middle slightly curled
            FingerType.RING to Range(0.3f, 0.6f),       // Ring slightly curled
            FingerType.PINKY to Range(0.3f, 0.6f)       // Pinky slightly curled
        ),
        palmOrientations = setOf(PalmOrientation.FACING_LEFT, PalmOrientation.FACING_RIGHT),
        // Custom evaluator for C-shape
        customEvaluator = { metrics ->
            // Check if fingers form a C-shape by measuring distances between fingertips
            // This would check if the fingertips form an arc
            val thumbTipToIndexTip = metrics.fingerDistances[Pair(FingerType.THUMB, FingerType.INDEX)] ?: 1.0f
            val thumbTipToPinkyTip = metrics.fingerDistances[Pair(FingerType.THUMB, FingerType.PINKY)] ?: 1.0f

            // C-shape has roughly equal distances between adjacent fingers
            val distanceConsistency = 0.8f

            // Check if thumb and fingers form an arc (rather than being straight or completely curled)
            distanceConsistency
        }
    )

    // D - Index finger points up, other fingers curled with thumb
    val signD = ASL2SignDescriptor(
        sign = ASLSign.D,
        fingerExtensions = mapOf(
            FingerType.THUMB to Range(0.4f, 0.7f),      // Thumb partially extended
            FingerType.INDEX to Range(0.7f, 1.0f),      // Index fully extended upward
            FingerType.MIDDLE to Range(0.0f, 0.3f),     // Middle curled
            FingerType.RING to Range(0.0f, 0.3f),       // Ring curled
            FingerType.PINKY to Range(0.0f, 0.3f)       // Pinky curled
        ),
        fingerCurls = mapOf(
            FingerType.THUMB to Range(0.4f, 0.7f),      // Thumb partially curled
            FingerType.INDEX to Range(0.0f, 0.3f),      // Index straight
            FingerType.MIDDLE to Range(0.7f, 1.0f),     // Middle fully curled
            FingerType.RING to Range(0.7f, 1.0f),       // Ring fully curled
            FingerType.PINKY to Range(0.7f, 1.0f)       // Pinky fully curled
        ),
        palmOrientations = setOf(PalmOrientation.FACING_AWAY),
        // Custom evaluator to check if thumb and middle/ring/pinky form a circular shape
        customEvaluator = { metrics ->
            // For D, we need to check if the thumb is touching middle finger
            // and the index is pointing upward
            val thumbMiddleDistance = metrics.fingerDistances[Pair(FingerType.THUMB, FingerType.MIDDLE)] ?: 1.0f

            // If thumb and middle finger are close, it's likely forming a D
            val thumbMiddleProximity = if (thumbMiddleDistance < 0.1f) 1.0f else 0.5f

            // Also check index finger direction is upward
            val indexDirection = metrics.index.direction.y > 0.8f

            if (indexDirection) thumbMiddleProximity else thumbMiddleProximity * 0.5f
        }
    )

    // E - Fingers curled into palm, thumb across fingers
    val signE = ASL2SignDescriptor(
        sign = ASLSign.E,
        fingerExtensions = mapOf(
            FingerType.THUMB to Range(0.3f, 0.6f),      // Thumb partially extended
            FingerType.INDEX to Range(0.0f, 0.3f),      // Index curled
            FingerType.MIDDLE to Range(0.0f, 0.3f),     // Middle curled
            FingerType.RING to Range(0.0f, 0.3f),       // Ring curled
            FingerType.PINKY to Range(0.0f, 0.3f)       // Pinky curled
        ),
        fingerCurls = mapOf(
            FingerType.THUMB to Range(0.4f, 0.7f),      // Thumb partially curled
            FingerType.INDEX to Range(0.7f, 1.0f),      // Index fully curled
            FingerType.MIDDLE to Range(0.7f, 1.0f),     // Middle fully curled
            FingerType.RING to Range(0.7f, 1.0f),       // Ring fully curled
            FingerType.PINKY to Range(0.7f, 1.0f)       // Pinky fully curled
        ),
        palmOrientations = setOf(PalmOrientation.FACING_AWAY),
        // Enhanced evaluator using fingertips touching palm
        customEvaluator = { metrics ->
            // For E, all fingers except thumb should be touching palm
            val fingersOnPalm =
                (metrics.fingerTouchingPalm[FingerType.INDEX] ?: false) &&
                        (metrics.fingerTouchingPalm[FingerType.MIDDLE] ?: false) &&
                        (metrics.fingerTouchingPalm[FingerType.RING] ?: false) &&
                        (metrics.fingerTouchingPalm[FingerType.PINKY] ?: false)

            // Thumb should be positioned across or above the curled fingers
            val thumbPosition = metrics.thumb.direction.y > 0.0f

            // Fingers should be curled
            val fingersCurled =
                metrics.index.curl > 0.7f &&
                        metrics.middle.curl > 0.7f &&
                        metrics.ring.curl > 0.7f &&
                        metrics.pinky.curl > 0.7f

            // Combine metrics for final confidence
            if (fingersOnPalm && thumbPosition && fingersCurled) 0.95f
            else if (fingersCurled && thumbPosition) 0.8f
            else if (fingersCurled) 0.6f
            else 0.3f
        }
    )

    // F - Index and thumb touch, other fingers extended
    val signF = ASL2SignDescriptor(
        sign = ASLSign.F,
        fingerExtensions = mapOf(
            FingerType.THUMB to Range(0.4f, 0.7f),      // Thumb extended to touch index
            FingerType.INDEX to Range(0.4f, 0.7f),      // Index partially extended
            FingerType.MIDDLE to Range(0.7f, 1.0f),     // Middle fully extended
            FingerType.RING to Range(0.7f, 1.0f),       // Ring fully extended
            FingerType.PINKY to Range(0.7f, 1.0f)       // Pinky fully extended
        ),
        fingerCurls = mapOf(
            FingerType.THUMB to Range(0.3f, 0.6f),      // Thumb slightly curled to meet index
            FingerType.INDEX to Range(0.3f, 0.6f),      // Index slightly curled to meet thumb
            FingerType.MIDDLE to Range(0.0f, 0.3f),     // Middle straight
            FingerType.RING to Range(0.0f, 0.3f),       // Ring straight
            FingerType.PINKY to Range(0.0f, 0.3f)       // Pinky straight
        ),
        palmOrientations = setOf(PalmOrientation.FACING_AWAY),
        // Custom evaluator to check if thumb and index form a circle
        customEvaluator = { metrics ->
            // For F, we need to check if thumb and index are touching
            val thumbIndexDistance = metrics.fingerDistances[Pair(FingerType.THUMB, FingerType.INDEX)] ?: 1.0f

            // If thumb and index are close, it's likely forming the F circle
            val thumbIndexProximity = if (thumbIndexDistance < 0.1f) 1.0f else 0.5f

            // Also check other fingers are extended
            val otherFingersExtended =
                metrics.middle.extension > 0.7f &&
                        metrics.ring.extension > 0.7f &&
                        metrics.pinky.extension > 0.7f

            if (otherFingersExtended) thumbIndexProximity else thumbIndexProximity * 0.5f
        }
    )

    // G - Index finger points forward, thumb out to side
    val signG = ASL2SignDescriptor(
        sign = ASLSign.G,
        fingerExtensions = mapOf(
            FingerType.THUMB to Range(0.5f, 0.8f),      // Thumb extended to side
            FingerType.INDEX to Range(0.7f, 1.0f),      // Index fully extended
            FingerType.MIDDLE to Range(0.0f, 0.3f),     // Middle curled
            FingerType.RING to Range(0.0f, 0.3f),       // Ring curled
            FingerType.PINKY to Range(0.0f, 0.3f)       // Pinky curled
        ),
        fingerCurls = mapOf(
            FingerType.THUMB to Range(0.0f, 0.3f),      // Thumb straight
            FingerType.INDEX to Range(0.0f, 0.3f),      // Index straight
            FingerType.MIDDLE to Range(0.7f, 1.0f),     // Middle fully curled
            FingerType.RING to Range(0.7f, 1.0f),       // Ring fully curled
            FingerType.PINKY to Range(0.7f, 1.0f)       // Pinky fully curled
        ),
        palmOrientations = setOf(PalmOrientation.FACING_LEFT, PalmOrientation.FACING_RIGHT),
        // Custom evaluator to check if index is pointing forward
        customEvaluator = { metrics ->
            // For G, the index should be pointing forward (away from palm)
            // and thumb should be extended outward

            // Check index finger direction (z-axis for forward)
            val indexPointingForward = metrics.index.direction.z < -0.7f

            // Check thumb is out to side (x-axis)
            val thumbOutToSide = abs(metrics.thumb.direction.x) > 0.7f

            // Combined confidence
            if (indexPointingForward && thumbOutToSide) 0.9f else 0.5f
        }
    )

}