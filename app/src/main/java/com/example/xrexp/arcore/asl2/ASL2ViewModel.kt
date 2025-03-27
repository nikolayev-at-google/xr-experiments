package com.example.xrexp.arcore.asl2

import androidx.lifecycle.ViewModel
import androidx.xr.arcore.Hand
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import androidx.xr.runtime.Session as ARCoreSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * ViewModel that processes hand tracking data and exposes detection results.
 *
 * This ViewModel integrates the ASLSignDetector with Android's MVVM architecture,
 * providing reactive data flows for UI components.
 */
class ASL2ViewModel : ViewModel() {
    private val detector = ASLSignDetector()

    private val _detectionResult = MutableStateFlow<DetectionResult?>(null)
    val detectionResult: StateFlow<DetectionResult?> = _detectionResult.asStateFlow()

    private val _detectedSign = MutableStateFlow<ASLSign>(ASLSign.NONE)
    val detectedSign: StateFlow<ASLSign> = _detectedSign.asStateFlow()

    private val _parameters = MutableStateFlow(DetectionParameters())
    val parameters: StateFlow<DetectionParameters> = _parameters.asStateFlow()

    // Add flow for detection events
    private val _detectionEvents = MutableSharedFlow<DetectionEvent>()
    val detectionEvents: SharedFlow<DetectionEvent> = _detectionEvents.asSharedFlow()


    // Jobs
    var leftHandCollector : Job? = null
//    var rightHandCollector : Job? = null

    /**
     * Start tracking hands for ASL detection
     */
    fun startTracking(arCoreSession: ARCoreSession) {
        leftHandCollector = viewModelScope.launch {
            // Track left hand
            Hand.left(arCoreSession)?.state?.collect { leftHandState ->
                if (leftHandState.isActive) {
                    processHandState(leftHandState, isLeftHand = true)
                }
            }
        }

//        rightHandCollector = viewModelScope.launch {
//            // Track right hand
//            Hand.right(arCoreSession)?.state?.collect { rightHandState ->
//                if (rightHandState.isActive) {
//                    processHandState(rightHandState, isLeftHand = false)
//                }
//            }
//        }
    }

    fun stopTracking() {
        leftHandCollector?.cancel()
//        rightHandCollector?.cancel()
    }

    fun updateParameters(parameters: DetectionParameters) {
        _parameters.value = parameters
        detector.updateParameters(parameters)
    }

    fun processHandState(handState: Hand.State, isLeftHand: Boolean) {
        if (handState.isActive) {
            val result = detector.detectSign(handState)
            _detectionResult.value = result

            val newSign = result.primaryCandidate?.sign ?: ASLSign.NONE
            val previousSign = _detectedSign.value

            // Update detected sign
            _detectedSign.value = newSign

            // Emit detection event if sign changed
            if (previousSign != newSign && newSign != ASLSign.NONE) {
                viewModelScope.launch {
                    _detectionEvents.emit(DetectionEvent.SignDetected(newSign))
                }
            }
        }
    }

    sealed class DetectionEvent {
        data class SignDetected(val sign: ASLSign) : DetectionEvent()
    }
}