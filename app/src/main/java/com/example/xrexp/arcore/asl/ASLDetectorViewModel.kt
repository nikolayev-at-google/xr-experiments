package com.example.xrexp.arcore.asl

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.xr.runtime.Session as ARCoreSession
import kotlinx.coroutines.launch
import androidx.xr.arcore.Hand
import kotlinx.coroutines.Job


/**
 * View Model for handling ASL detection in your application
 */
class ASLDetectorViewModel : ViewModel() {
    companion object {
        private const val TAG = "ASLDetectionViewModel"
    }

    // Debug info states for visualization
    val leftHandDebugInfo = mutableStateOf<ASLDetector.DebugInfo?>(null)
    val rightHandDebugInfo = mutableStateOf<ASLDetector.DebugInfo?>(null)

    // Current detected signs
    val leftHandSign = mutableStateOf(ASLDetector.Sign.NONE)
    val rightHandSign = mutableStateOf(ASLDetector.Sign.NONE)

    // Confidence thresholds
    private val confidenceThreshold = 0.6f

    // Options
    var detectASL = mutableStateOf(true)
    var includeDebugInfo = mutableStateOf(true)

    // Jobs
    var leftHandCollector : Job? = null
    var rightHandCollector : Job? = null

    /**
     * Start tracking hands for ASL detection
     */
    fun startTracking(arCoreSession: ARCoreSession) {
        leftHandCollector = viewModelScope.launch {
            // Track left hand
            Hand.left(arCoreSession)?.state?.collect { leftHandState ->
                if (detectASL.value) {
                    processHand(leftHandState, isLeftHand = true)
                }
            }
        }

        rightHandCollector = viewModelScope.launch {
            // Track right hand
            Hand.right(arCoreSession)?.state?.collect { rightHandState ->
                if (detectASL.value) {
                    processHand(rightHandState, isLeftHand = false)
                }
            }
        }
    }

    fun stopTracking() {
        leftHandCollector?.cancel()
        rightHandCollector?.cancel()
    }

    /**
     * Process hand state and detect ASL signs
     */
    private fun processHand(handState: Hand.State, isLeftHand: Boolean) {
        // Detect ASL sign
        val result = ASLDetector.detectSign(
            handState = handState,
            includeDebugInfo = includeDebugInfo.value
        )

        // Update debug info if needed
        if (includeDebugInfo.value) {
            if (isLeftHand) {
                leftHandDebugInfo.value = result.debugInfo
            } else {
                rightHandDebugInfo.value = result.debugInfo
            }
        }

        // Update current sign if confidence exceeds threshold
        if (result.confidence > confidenceThreshold) {
            if (isLeftHand) {
                if (leftHandSign.value != result.sign) {
                    Log.d(TAG, "Left hand sign detected: ${result.sign} (${result.confidence})")
                    leftHandSign.value = result.sign
                    onSignDetected(result.sign, isLeftHand, result.confidence)
                }
            } else {
                if (rightHandSign.value != result.sign) {
                    Log.d(TAG, "Right hand sign detected: ${result.sign} (${result.confidence})")
                    rightHandSign.value = result.sign
                    onSignDetected(result.sign, isLeftHand, result.confidence)
                }
            }
        } else {
            // Reset sign if confidence drops too low
            if (isLeftHand && leftHandSign.value != ASLDetector.Sign.NONE) {
                leftHandSign.value = ASLDetector.Sign.NONE
            } else if (!isLeftHand && rightHandSign.value != ASLDetector.Sign.NONE) {
                rightHandSign.value = ASLDetector.Sign.NONE
            }
        }
    }

    /**
     * Called when a sign is detected with sufficient confidence
     */
    private fun onSignDetected(sign: ASLDetector.Sign, isLeftHand: Boolean, confidence: Float) {
        // Implement your application logic for handling detected signs
        // Examples:
        // - Show visual feedback
        // - Play sound
        // - Add to recognized text
        // - Trigger action based on sign
    }

    /**
     * Update configuration for ASL detection
     */
    fun updateConfig(enableDetection: Boolean, enableDebug: Boolean) {
        detectASL.value = enableDetection
        includeDebugInfo.value = enableDebug
    }
}