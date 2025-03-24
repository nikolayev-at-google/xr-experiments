package com.example.xrexp.arcore.asl2

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


/**
 * Main Compose screen for ASL sign detection visualization.
 *
 * @param viewModel ASL2ViewModel that provides detection data
 */
@Composable
fun ASL2DetectorScreen(viewModel: ASL2ViewModel) {
    val detectionResult by viewModel.detectionResult.collectAsState()
    val detectedSign by viewModel.detectedSign.collectAsState()
    val parameters by viewModel.parameters.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Main content header
        Text(
            "ASL Sign Detector",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Current detection
        CurrentDetectionDisplay(detectedSign)

        // Hand visualization
        HandVisualization(
            detectionResult?.handMetrics,
            detectionResult?.primaryCandidate?.debugInfo
        )

        // Confidence meters for candidates
        ConfidenceMeters(detectionResult?.alternativeCandidates ?: emptyList())

        // Hand metrics display
        HandMetricsDisplay(detectionResult?.handMetrics)

        // Parameter adjustment
        ParameterAdjustment(
            parameters = parameters,
            onParametersChanged = { viewModel.updateParameters(it) }
        )
    }
}

@Composable
fun CurrentDetectionDisplay(sign: ASLSign) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Detected Sign",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                when (sign) {
                    ASLSign.NONE -> "None"
                    else -> sign.name.replace("NUMBER_", "")
                },
                style = MaterialTheme.typography.headlineMedium,
                color = if (sign == ASLSign.NONE) Color.Gray else Color.Blue
            )
        }
    }
}

@Suppress("unused")
@Composable
fun HandVisualization(
    handMetrics: HandMetrics?,
    debugInfo: Map<String, Any>?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(bottom = 16.dp)
    ) {
        if (handMetrics == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hand detected")
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("This would be a custom drawing canvas to visualize the hand\n" +
                        "Would use Canvas composable to draw joints and connections\n" +
                        "Project 3D data to 2D visualization with proper angle\n" +
                        "Color-code based on extension/curl metrics and debug info")
            }
        }
    }
}

@Composable
fun ConfidenceMeters(candidates: List<SignCandidate>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Sign Candidates",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            candidates.forEach { candidate ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Sign name
                    val displayName = when {
                        candidate.sign.name.startsWith("NUMBER_") ->
                            candidate.sign.name.replace("NUMBER_", "")
                        else -> candidate.sign.name
                    }

                    Text(
                        displayName,
                        modifier = Modifier.width(40.dp)
                    )

                    // Confidence bar
                    LinearProgressIndicator(
                        progress = { candidate.confidence },
                        modifier = Modifier.weight(1f).height(12.dp),
                        color = when {
                            candidate.confidence > 0.8f -> Color.Green
                            candidate.confidence > 0.5f -> Color.Yellow
                            else -> Color.Red
                        },
                        trackColor = ProgressIndicatorDefaults.linearTrackColor,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )

                    // Percentage
                    Text(
                        "${(candidate.confidence * 100).toInt()}%",
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun HandMetricsDisplay(handMetrics: HandMetrics?) {
    if (handMetrics == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Hand Metrics",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Palm Orientation
            MetricRow("Palm Facing", handMetrics.palmOrientation.name.replace("FACING_", ""))

            // Hand Orientation
            MetricRow("Hand Orientation", handMetrics.handOrientation.name)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // Finger metrics
            Text("Finger Extension/Curl", style = MaterialTheme.typography.labelSmall)

            FingerMetricRow("Thumb", handMetrics.thumb)
            FingerMetricRow("Index", handMetrics.index)
            FingerMetricRow("Middle", handMetrics.middle)
            FingerMetricRow("Ring", handMetrics.ring)
            FingerMetricRow("Pinky", handMetrics.pinky)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            // Finger distances
            Text("Finger Distances", style = MaterialTheme.typography.labelSmall)

            handMetrics.fingerDistances.forEach { (fingerPair, distance) ->
                MetricRow(
                    "${fingerPair.first.name} to ${fingerPair.second.name}",
                    String.format("%.2f", distance)
                )
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun FingerMetricRow(fingerName: String, metrics: FingerMetrics) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            fingerName,
            modifier = Modifier.width(60.dp)
        )

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Ext:",
                    modifier = Modifier.width(30.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                LinearProgressIndicator(
                    progress = { metrics.extension },
                    modifier = Modifier.weight(1f).height(8.dp),
                    color = Color.Blue,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )

                Text(
                    String.format("%.2f", metrics.extension),
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Curl:",
                    modifier = Modifier.width(30.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                LinearProgressIndicator(
                    progress = { metrics.curl },
                    modifier = Modifier.weight(1f).height(8.dp),
                    color = Color.Red,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )

                Text(
                    String.format("%.2f", metrics.curl),
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ParameterAdjustment(
    parameters: DetectionParameters,
    onParametersChanged: (DetectionParameters) -> Unit
) {
    var localParameters by remember { mutableStateOf(parameters) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Detection Parameters",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Confidence threshold slider
            Text(
                "Confidence Threshold: ${String.format("%.2f", localParameters.confidenceThreshold)}",
                style = MaterialTheme.typography.titleMedium
            )

            Slider(
                value = localParameters.confidenceThreshold,
                onValueChange = {
                    localParameters = localParameters.copy(confidenceThreshold = it)
                },
                onValueChangeFinished = {
                    onParametersChanged(localParameters)
                },
                valueRange = 0f..1f,
                steps = 20
            )

            // Extension threshold slider
            Text(
                "Extension Threshold: ${String.format("%.2f", localParameters.fingerExtensionThreshold)}",
                style = MaterialTheme.typography.titleMedium
            )

            Slider(
                value = localParameters.fingerExtensionThreshold,
                onValueChange = {
                    localParameters = localParameters.copy(fingerExtensionThreshold = it)
                },
                onValueChangeFinished = {
                    onParametersChanged(localParameters)
                },
                valueRange = 0f..1f,
                steps = 20
            )

            // Curl threshold slider
            Text(
                "Curl Threshold: ${String.format("%.2f", localParameters.fingerCurlThreshold)}",
                style = MaterialTheme.typography.titleMedium
            )

            Slider(
                value = localParameters.fingerCurlThreshold,
                onValueChange = {
                    localParameters = localParameters.copy(fingerCurlThreshold = it)
                },
                onValueChangeFinished = {
                    onParametersChanged(localParameters)
                },
                valueRange = 0f..1f,
                steps = 20
            )

            // Distance threshold slider
            Text(
                "Distance Threshold: ${String.format("%.2f", localParameters.distanceThreshold)}",
                style = MaterialTheme.typography.titleMedium
            )

            Slider(
                value = localParameters.distanceThreshold,
                onValueChange = {
                    localParameters = localParameters.copy(distanceThreshold = it)
                },
                onValueChangeFinished = {
                    onParametersChanged(localParameters)
                },
                valueRange = 0.01f..0.2f,
                steps = 19
            )
        }
    }
}