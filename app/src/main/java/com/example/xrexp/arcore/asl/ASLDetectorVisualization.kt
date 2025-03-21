package com.example.xrexp.arcore.asl

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.min

import com.example.xrexp.R

/**
 * Main visualization component for ASL detection debugging
 */
@Composable
fun ASLDetectorVisualization(
    debugInfo: ASLDetector.DebugInfo,
    isLeftHand: Boolean,
    modifier: Modifier = Modifier
) {
    val handLabel = if (isLeftHand) "LEFT HAND" else "RIGHT HAND"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "ASL SIGN DETECTION - $handLabel",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Hand tracking status
        DebugStatusCard(
            title = "Tracking Status",
            isActive = debugInfo.isActive && debugInfo.hasAllRequiredJoints
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Detected sign display
        DetectedSignCard(debugInfo.detectedSign, debugInfo.confidenceScores)

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Finger metrics
        FingerMetricsPanel(debugInfo)
    }
}

/**
 * Card showing tracking status
 */
@Composable
fun DebugStatusCard(title: String, isActive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF4CAF50) else Color(0xFFE57373)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (isActive) "ACTIVE" else "INACTIVE",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

/**
 * Card showing the currently detected ASL sign
 */
@Composable
fun DetectedSignCard(
    detectedSign: ASLDetector.Sign,
    confidenceScores: Map<ASLDetector.Sign, Float>
) {
    val isSignDetected = detectedSign != ASLDetector.Sign.NONE

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSignDetected) Color(0xFF2196F3) else Color(0xFFE0E0E0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detected Sign:",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSignDetected) Color.White else Color.Black
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (isSignDetected) detectedSign.name else "NONE",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSignDetected) Color.White else Color.Gray
                    )
                )
            }

            if (isSignDetected) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Confidence: ${String.format("%.2f", confidenceScores[detectedSign] ?: 0f)}",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confidence bars for all signs
            Text(
                text = "Sign Confidence Scores:",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSignDetected) Color.White else Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sort signs by confidence
            val sortedScores = confidenceScores.entries
                .sortedByDescending { it.value }
                .filter { it.key != ASLDetector.Sign.NONE }

            sortedScores.forEach { (sign, confidence) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sign ${sign.name}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = if (sign == detectedSign) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSignDetected) Color.White else Color.Black
                        ),
                        modifier = Modifier.width(70.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    LinearProgressIndicator(
                        progress = { confidence },
                        modifier = Modifier.weight(1f),
                        color = if (sign == detectedSign) Color.White else Color(0xFFBBDEFB),
                        trackColor = Color(0x33FFFFFF)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = String.format("%.2f", confidence),
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = if (isSignDetected) Color.White else Color.Black
                        )
                    )
                }
            }
        }
    }
}

/**
 * Panel displaying finger extension and curl metrics
 */
@SuppressLint("DefaultLocale")
@Composable
fun FingerMetricsPanel(debugInfo: ASLDetector.DebugInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Finger Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Extension metrics
            Text(
                text = "Finger Extensions",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            FingerMetricsRow(
                fingerName = "Thumb",
                value = debugInfo.fingerExtensions["thumb"] ?: 0f,
                valueRange = 0f..2f,
                goodRange = 1.3f..1.8f,
                format = "Extension"
            )

            FingerMetricsRow(
                fingerName = "Index",
                value = debugInfo.fingerExtensions["index"] ?: 0f,
                valueRange = 0f..2f,
                goodRange = 1.3f..1.8f,
                format = "Extension"
            )

            FingerMetricsRow(
                fingerName = "Middle",
                value = debugInfo.fingerExtensions["middle"] ?: 0f,
                valueRange = 0f..2f,
                goodRange = 1.3f..1.8f,
                format = "Extension"
            )

            FingerMetricsRow(
                fingerName = "Ring",
                value = debugInfo.fingerExtensions["ring"] ?: 0f,
                valueRange = 0f..2f,
                goodRange = 1.3f..1.8f,
                format = "Extension"
            )

            FingerMetricsRow(
                fingerName = "Little",
                value = debugInfo.fingerExtensions["little"] ?: 0f,
                valueRange = 0f..2f,
                goodRange = 1.3f..1.8f,
                format = "Extension"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Curl metrics
            Text(
                text = "Finger Curls",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            FingerMetricsRow(
                fingerName = "Thumb",
                value = debugInfo.fingerCurls["thumb"] ?: 0f,
                valueRange = 0f..1f,
                goodRange = 0.3f..0.7f,
                format = "Curl"
            )

            FingerMetricsRow(
                fingerName = "Index",
                value = debugInfo.fingerCurls["index"] ?: 0f,
                valueRange = 0f..1f,
                goodRange = 0.3f..0.7f,
                format = "Curl"
            )

            FingerMetricsRow(
                fingerName = "Middle",
                value = debugInfo.fingerCurls["middle"] ?: 0f,
                valueRange = 0f..1f,
                goodRange = 0.3f..0.7f,
                format = "Curl"
            )

            FingerMetricsRow(
                fingerName = "Ring",
                value = debugInfo.fingerCurls["ring"] ?: 0f,
                valueRange = 0f..1f,
                goodRange = 0.3f..0.7f,
                format = "Curl"
            )

            FingerMetricsRow(
                fingerName = "Little",
                value = debugInfo.fingerCurls["little"] ?: 0f,
                valueRange = 0f..1f,
                goodRange = 0.3f..0.7f,
                format = "Curl"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Palm orientation
            Text(
                text = "Palm Orientation (${String.format("%.2f", debugInfo.palmOrientation.x)}, " +
                        "${String.format("%.2f", debugInfo.palmOrientation.y)}, " +
                        "${String.format("%.2f", debugInfo.palmOrientation.z)})",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * Row showing a finger metric with value and color-coded bar
 */
@SuppressLint("DefaultLocale")
@Composable
fun FingerMetricsRow(
    fingerName: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    goodRange: ClosedFloatingPointRange<Float>,
    format: String
) {
    val normalizedValue = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    val isInGoodRange = value in goodRange

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = fingerName,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.width(60.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalizedValue.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isInGoodRange) Color(0xFF4CAF50) else Color(0xFFFFA000))
            )

            // Show good range visually
            val goodRangeStart = (goodRange.start - valueRange.start) /
                    (valueRange.endInclusive - valueRange.start)
            val goodRangeEnd = (goodRange.endInclusive - valueRange.start) /
                    (valueRange.endInclusive - valueRange.start)

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(goodRangeEnd.coerceIn(0f, 1f))
                    .fillMaxWidth(fraction = goodRangeStart.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(6.dp))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = String.format("%.2f", value),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = if (isInGoodRange) FontWeight.Bold else FontWeight.Normal,
                color = if (isInGoodRange) Color(0xFF4CAF50) else Color(0xFFFFA000)
            )
        )
    }
}

/**
 * Reference guide composable for ASL signs
 */
@Composable
fun ASLReferenceGuide() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "ASL Reference Guide",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display reference images or descriptions for each sign
        SingleSignReference(
            sign = ASLDetector.Sign.A,
            description = "A fist with the thumb positioned at the side of the fist"
        )

        SingleSignReference(
            sign = ASLDetector.Sign.B,
            description = "Hand held up, palm facing away, all fingers extended upward and together, with thumb tucked inward"
        )

        SingleSignReference(
            sign = ASLDetector.Sign.C,
            description = "Hand with fingers together and curved in a C shape"
        )

        SingleSignReference(
            sign = ASLDetector.Sign.D,
            description = "Index finger pointing up with the thumb and other fingers making a small O shape"
        )

        SingleSignReference(
            sign = ASLDetector.Sign.E,
            description = "Fingers curled in toward palm, thumb tucked across fingers"
        )

        Image(
            modifier = Modifier.size(400.dp).align(Alignment.CenterHorizontally),
            painter = painterResource(id = R.drawable.asl),
            contentScale = ContentScale.Fit,
            contentDescription = "ASL guide"
        )
    }
}

/**
 * Reference information for a single ASL sign
 */
@Composable
fun SingleSignReference(
    sign: ASLDetector.Sign,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sign letter in a circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sign.name,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Sign description
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 16.sp
                )
            )
        }
    }
}

/**
 * Usage in an Activity or Fragment
 */
@Composable
fun ASLDetectionScreen(
    viewModel: ASLDetectorViewModel = viewModel()
) {
    // State for debug information
    var leftHandDebugInfo by remember { viewModel.leftHandDebugInfo }
    var rightHandDebugInfo by remember { viewModel.rightHandDebugInfo }
    var showReferenceGuide by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ASL Sign Detection",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Button to toggle reference guide
            Button(
                onClick = { showReferenceGuide = !showReferenceGuide },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = if (showReferenceGuide) "Hide Guide" else "Show Guide"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showReferenceGuide) {
            // Show reference guide
            ASLReferenceGuide()
        } else {
            // Show detection UI
            var selectedTabIndex by remember { mutableStateOf(0) }

            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Left Hand") }
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Right Hand") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show the selected hand debug info
            when (selectedTabIndex) {

                0 -> {
                    // Left hand
                    leftHandDebugInfo?.let { debugInfo ->
                        ASLDetectorVisualization(
                            debugInfo = debugInfo,
                            isLeftHand = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailedSignDebugView(
                            debugInfo = debugInfo,
                            isLeftHand = true,
                            sign = ASLDetector.Sign.A
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailedSignDebugView(
                            debugInfo = debugInfo,
                            isLeftHand = true,
                            sign = ASLDetector.Sign.E
                        )
                    } ?: Text("No left hand tracking data available")
                }

                1 -> {
                    // Right hand
                    rightHandDebugInfo?.let { debugInfo ->
                        ASLDetectorVisualization(
                            debugInfo = debugInfo,
                            isLeftHand = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailedSignDebugView(
                            debugInfo = debugInfo,
                            isLeftHand = false,
                            sign = ASLDetector.Sign.A
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailedSignDebugView(
                            debugInfo = debugInfo,
                            isLeftHand = false,
                            sign = ASLDetector.Sign.E
                        )
                    } ?: Text("No right hand tracking data available")
                }
            }
        }
    }
}