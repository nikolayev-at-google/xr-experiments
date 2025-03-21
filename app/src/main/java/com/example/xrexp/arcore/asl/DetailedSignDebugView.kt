package com.example.xrexp.arcore.asl

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * Enhanced visualization for ASL Sign A and E debugging
 */
@Composable
fun DetailedSignDebugView(
    debugInfo: ASLDetector.DebugInfo,
    isLeftHand: Boolean,
    sign: ASLDetector.Sign
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Detailed Debug: Sign ${sign.name}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confidence score
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Confidence Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val confidence = debugInfo.confidenceScores[sign] ?: 0f
                LinearProgressIndicator(
                    progress = { confidence },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    color = if (confidence > 0.6f) Color(0xFF4CAF50) else Color(0xFFFFA000),
                    trackColor = Color(0xFFE0E0E0)
                )

                Text(
                    text = String.format("%.2f", confidence),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (confidence > 0.6f) Color(0xFF4CAF50) else Color(0xFFFFA000)
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Finger curl values
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Finger Curl Values",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // A sign: low curl values are good (more curled)
                // E sign: medium curl values are good (partly curled)
                val curlTargetRange = when (sign) {
                    ASLDetector.Sign.A -> 0f..0.6f
                    ASLDetector.Sign.E -> 0.15f..0.65f
                    else -> 0f..1f
                }

                debugInfo.fingerCurls.entries.sortedBy {
                    when (it.key) {
                        "thumb" -> 0
                        "index" -> 1
                        "middle" -> 2
                        "ring" -> 3
                        "little" -> 4
                        else -> 5
                    }
                }.forEach { (finger, curl) ->
                    val isInGoodRange = curl in curlTargetRange
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = finger.capitalize(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.width(70.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        LinearProgressIndicator(
                            progress = { curl },
                            modifier = Modifier.weight(1f),
                            color = if (isInGoodRange) Color(0xFF4CAF50) else Color(0xFFE57373),
                            trackColor = Color(0xFFE0E0E0)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = String.format("%.2f", curl),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isInGoodRange) Color(0xFF4CAF50) else Color(0xFFE57373)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Label for good range
                Text(
                    text = when (sign) {
                        ASLDetector.Sign.A -> "Good range: 0.00-0.60 (more curled)"
                        ASLDetector.Sign.E -> "Good range: 0.15-0.65 (partly curled)"
                        else -> "Curl values (0.0 = fully curled, 1.0 = straight)"
                    },
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Finger extension values
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Finger Extension Values",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // A sign: low extension values are good
                // E sign: low-medium extension values are good
                val extensionTargetRange = when (sign) {
                    ASLDetector.Sign.A -> 0f..1.3f
                    ASLDetector.Sign.E -> 0f..1.4f
                    else -> 0f..2f
                }

                debugInfo.fingerExtensions.entries.sortedBy {
                    when (it.key) {
                        "thumb" -> 0
                        "index" -> 1
                        "middle" -> 2
                        "ring" -> 3
                        "little" -> 4
                        else -> 5
                    }
                }.forEach { (finger, extension) ->
                    val isInGoodRange = extension in extensionTargetRange
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = finger.capitalize(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.width(70.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        LinearProgressIndicator(
                            progress = { extension / 2f },
                            modifier = Modifier.weight(1f),
                            color = if (isInGoodRange) Color(0xFF4CAF50) else Color(0xFFE57373),
                            trackColor = Color(0xFFE0E0E0)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = String.format("%.2f", extension),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isInGoodRange) Color(0xFF4CAF50) else Color(0xFFE57373)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Label for good range
                Text(
                    text = when (sign) {
                        ASLDetector.Sign.A -> "Good range: 0.00-1.30 (less extended)"
                        ASLDetector.Sign.E -> "Good range: 0.00-1.40 (slightly extended)"
                        else -> "Extension values (higher = more extended)"
                    },
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Thumb position data (especially important for A and E)
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Thumb Position Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (debugInfo.thumbPositionData.isNotEmpty()) {
                    // Group the thumb position data into categories
                    val thumbToFingerData = debugInfo.thumbPositionData
                        .filter { it.key.startsWith("thumb_to_") }

                    val heightData = debugInfo.thumbPositionData
                        .filter { it.key.endsWith("_height") }

                    val otherData = debugInfo.thumbPositionData
                        .filter { !it.key.startsWith("thumb_to_") && !it.key.endsWith("_height") }

                    // Thumb to finger distances
                    if (thumbToFingerData.isNotEmpty()) {
                        Text(
                            text = "Thumb to Finger Distances:",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        thumbToFingerData.entries.forEach { (key, value) ->
                            // Get expected range based on sign
                            val isGood = when (sign) {
                                ASLDetector.Sign.A -> {
                                    if (key == "thumb_to_index_proximal") value < 0.12f else true
                                }
                                ASLDetector.Sign.E -> {
                                    if (key == "thumb_to_middle_proximal" ||
                                        key == "thumb_to_index_proximal") value < 0.12f else true
                                }
                                else -> true
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = key.replace("thumb_to_", "")
                                        .replace("_", " ")
                                        .capitalize(),
                                    style = TextStyle(
                                        fontSize = 13.sp
                                    ),
                                    modifier = Modifier.width(110.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = String.format("%.3f", value),
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = if (isGood) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isGood) Color(0xFF4CAF50) else Color.Gray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Height data
                    if (heightData.isNotEmpty()) {
                        Text(
                            text = "Finger Heights:",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Find min and max values for normalization
                        val minHeight = heightData.values.minOrNull() ?: 0f
                        val maxHeight = heightData.values.maxOrNull() ?: 1f
                        val range = maxHeight - minHeight

                        heightData.entries.sortedBy {
                            when {
                                it.key.startsWith("thumb") -> 0
                                it.key.startsWith("index") -> 1
                                it.key.startsWith("middle") -> 2
                                it.key.startsWith("ring") -> 3
                                it.key.startsWith("little") -> 4
                                else -> 5
                            }
                        }.forEach { (key, value) ->
                            // For sign A, thumb should be lower than index
                            val isThumbLower = key == "thumb_height" &&
                                    heightData["index_height"]?.let { value < it } == true

                            val isGood = when (sign) {
                                ASLDetector.Sign.A -> key == "thumb_height" && isThumbLower
                                else -> true
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = key.replace("_height", "").capitalize(),
                                    style = TextStyle(
                                        fontSize = 13.sp
                                    ),
                                    modifier = Modifier.width(70.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                LinearProgressIndicator(
                                    progress = { if (range > 0.001f) (value - minHeight) / range else 0.5f },
                                    modifier = Modifier.weight(1f),
                                    color = if (isGood) Color(0xFF4CAF50) else Color.Gray,
                                    trackColor = Color(0xFFE0E0E0)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = String.format("%.3f", value),
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = if (isGood) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isGood) Color(0xFF4CAF50) else Color.Gray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Other thumb data
                    if (otherData.isNotEmpty()) {
                        Text(
                            text = "Other Thumb Data:",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        otherData.entries.forEach { (key, value) ->
                            val isGood = when (sign) {
                                ASLDetector.Sign.A -> {
                                    if (key == "thumb_crossing_palm") value < 0.3f else true
                                }
                                ASLDetector.Sign.E -> {
                                    if (key == "thumb_crossing_palm") value > 0.3f else true
                                }
                                else -> true
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = key.replace("_", " ").capitalize(),
                                    style = TextStyle(
                                        fontSize = 13.sp
                                    ),
                                    modifier = Modifier.width(130.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = String.format("%.3f", value),
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = if (isGood) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isGood) Color(0xFF4CAF50) else Color.Gray
                                    )
                                )

                                // Add explanation text for special values
                                if (key == "thumb_crossing_palm") {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (sign) {
                                            ASLDetector.Sign.A -> "(should be < 0.3)"
                                            ASLDetector.Sign.E -> "(should be > 0.3)"
                                            else -> ""
                                        },
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No thumb position data available",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hand visualization focused on specific sign
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Hand Visualization for Sign ${sign.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Canvas for drawing the hand with sign-specific highlights
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color(0xFFF5F5F5))
                ) {
                    // Center point
                    val center = Offset(size.width / 2, size.height / 2)
                    val scale = min(size.width, size.height) * 0.4f

                    // Draw the hand skeleton with specific highlights for the sign
                    drawHandSkeletonForSign(center, scale, isLeftHand, debugInfo, sign)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sign-specific instruction
                Text(
                    text = getSignInstructionText(sign),
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                )
            }
        }
    }
}

/**
 * Get instruction text for the specific sign
 */
private fun getSignInstructionText(sign: ASLDetector.Sign): String {
    return when (sign) {
        ASLDetector.Sign.A -> "For sign A: Make a fist with your thumb positioned at the side of your index finger. " +
                "All fingers should be tightly curled, and the thumb should be slightly extended but close to the index finger."

        ASLDetector.Sign.E -> "For sign E: Curl your fingers toward your palm, but not as tightly as a fist. " +
                "The thumb should be tucked across the fingers, positioned near the middle finger's base."

        ASLDetector.Sign.B -> "For sign B: Hold your hand up with palm facing away, all fingers extended upward and close together, " +
                "with thumb tucked inward."

        ASLDetector.Sign.C -> "For sign C: Form your hand into a C shape with fingers together and curved. " +
                "The thumb and pinky should be positioned to form the C shape."

        ASLDetector.Sign.D -> "For sign D: Point your index finger up with the thumb and other fingers making a small O shape. " +
                "The palm should be facing sideways."

        else -> "Make the sign as shown in the reference guide."
    }
}

/**
 * Draw the hand skeleton with highlights specific to the sign
 */
private fun DrawScope.drawHandSkeletonForSign(
    center: Offset,
    scale: Float,
    isLeftHand: Boolean,
    debugInfo: ASLDetector.DebugInfo,
    sign: ASLDetector.Sign
) {
    val palmColor = Color(0xFFE0E0E0)
    val wristColor = Color(0xFF9E9E9E)
    val xDirection = if (isLeftHand) -1 else 1

    // Draw palm
    drawCircle(
        color = palmColor,
        radius = scale * 0.3f,
        center = center
    )

    // Draw wrist
    drawLine(
        color = wristColor,
        start = Offset(center.x - scale * 0.3f, center.y + scale * 0.3f),
        end = Offset(center.x + scale * 0.3f, center.y + scale * 0.3f),
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )

    // Helper function to determine finger color based on specific criteria for each sign
    fun getFingerColorForSign(fingerName: String): Color {
        val extension = debugInfo.fingerExtensions[fingerName] ?: 0f
        val curl = debugInfo.fingerCurls[fingerName] ?: 0f

        return when (sign) {
            ASLDetector.Sign.A -> {
                // For A: curled fingers are good (except thumb)
                if (fingerName == "thumb") {
                    // Thumb should be slightly extended but not fully
                    if (extension < 1.7f && curl < 0.9f) {
                        // Thumb position matters
                        val thumbToIndexDist = debugInfo.thumbPositionData["thumb_to_index_proximal"] ?: 1.0f
                        if (thumbToIndexDist < 0.12f) Color(0xFF4CAF50) else Color(0xFFFFEB3B)
                    } else {
                        Color(0xFFF44336)
                    }
                } else {
                    // Other fingers should be curled
                    if (curl < 0.6f && extension < 1.3f) Color(0xFF4CAF50) else Color(0xFFF44336)
                }
            }
            ASLDetector.Sign.E -> {
                // For E: partly curled fingers, thumb tucked across
                if (fingerName == "thumb") {
                    // Thumb position matters a lot
                    val thumbCrossingPalm = debugInfo.thumbPositionData["thumb_crossing_palm"] ?: 0f
                    if (curl < 0.7f && thumbCrossingPalm > 0.3f) Color(0xFF4CAF50) else Color(0xFFFFEB3B)
                } else {
                    // Other fingers should be partly curled
                    if (curl < 0.65f && curl > 0.15f) Color(0xFF4CAF50) else Color(0xFFF44336)
                }
            }
            ASLDetector.Sign.B -> {
                if (fingerName == "thumb") {
                    // Thumb should be tucked
                    if (curl < 0.5f) Color(0xFF4CAF50) else Color(0xFFF44336)
                } else {
                    // Other fingers should be extended and straight
                    if (extension > 1.6f && curl > 0.7f) Color(0xFF4CAF50) else Color(0xFFF44336)
                }
            }
            ASLDetector.Sign.C -> {
                // All fingers should be curved (medium curl)
                if (curl < 0.7f && curl > 0.3f && extension > 1.3f) Color(0xFF4CAF50) else Color(0xFFF44336)
            }
            ASLDetector.Sign.D -> {
                if (fingerName == "index") {
                    // Index should be extended
                    if (extension > 1.5f && curl > 0.7f) Color(0xFF4CAF50) else Color(0xFFF44336)
                } else if (fingerName == "thumb") {
                    // Thumb should be curved
                    if (curl < 0.7f && curl > 0.3f) Color(0xFF4CAF50) else Color(0xFFF44336)
                } else {
                    // Other fingers should be curled
                    if (curl < 0.5f) Color(0xFF4CAF50) else Color(0xFFF44336)
                }
            }
            else -> Color(0xFF9E9E9E)
        }
    }

    // Draw fingers with appropriate colors for the specific sign
    drawFingerForSign(center, "thumb", getFingerColorForSign("thumb"), scale, xDirection, -30f)
    drawFingerForSign(center, "index", getFingerColorForSign("index"), scale, xDirection, -15f)
    drawFingerForSign(center, "middle", getFingerColorForSign("middle"), scale, xDirection, 0f)
    drawFingerForSign(center, "ring", getFingerColorForSign("ring"), scale, xDirection, 15f)
    drawFingerForSign(center, "little", getFingerColorForSign("little"), scale, xDirection, 30f)

    // Add sign-specific visualizations
    when (sign) {
        ASLDetector.Sign.A -> {
            // Highlight the area where thumb should be positioned
            val indexBaseX = center.x + xDirection * scale * 0.12f * Math.sin(Math.toRadians(-15.0)).toFloat()
            val indexBaseY = center.y + scale * 0.12f * Math.cos(Math.toRadians(-15.0)).toFloat()

            drawCircle(
                color = Color(0x334CAF50),
                radius = scale * 0.12f,
                center = Offset(indexBaseX, indexBaseY)
            )

            drawCircle(
                color = Color(0x334CAF50),
                radius = scale * 0.12f,
                center = Offset(indexBaseX, indexBaseY)
            )
        }
        ASLDetector.Sign.E -> {
            // Highlight area where thumb should cross palm
            val middleBaseX = center.x + xDirection * scale * 0.12f * Math.sin(Math.toRadians(0.0)).toFloat()
            val middleBaseY = center.y + scale * 0.12f * Math.cos(Math.toRadians(0.0)).toFloat()

            // Draw an arrow to indicate thumb crossing direction
            val arrowStart = Offset(
                center.x + xDirection * scale * 0.25f * Math.sin(Math.toRadians(-30.0)).toFloat(),
                center.y + scale * 0.25f * Math.cos(Math.toRadians(-30.0)).toFloat()
            )

            val arrowEnd = Offset(
                center.x + xDirection * scale * 0.12f * Math.sin(Math.toRadians(0.0)).toFloat(),
                center.y + scale * 0.12f * Math.cos(Math.toRadians(0.0)).toFloat()
            )

            drawLine(
                color = Color(0xFF4CAF50),
                start = arrowStart,
                end = arrowEnd,
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )

            // Arrowhead
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 4f,
                center = arrowEnd
            )
        }
        else -> {
            // Additional visualizations for other signs could be added here
        }
    }
}

/**
 * Helper function to draw a finger in the hand visualization for a specific sign
 */
private fun DrawScope.drawFingerForSign(
    center: Offset,
    fingerName: String,
    color: Color,
    scale: Float,
    xDirection: Int,
    angle: Float
) {
    // Draw simplified finger based on angle from center
    val fingerLength = when (fingerName) {
        "thumb" -> scale * 0.5f
        else -> scale * 0.6f
    }

    val radians = Math.toRadians(angle.toDouble())

    // Adjust starting point based on finger
    val xOffset = when (fingerName) {
        "thumb" -> (Math.sin(radians) * xDirection * 0.25f * scale).toFloat()
        else -> (Math.sin(radians) * xDirection * 0.2f * scale).toFloat()
    }

    val yOffset = when (fingerName) {
        "thumb" -> (Math.cos(radians) * -0.1f * scale).toFloat()
        else -> (Math.cos(radians) * -0.2f * scale).toFloat()
    }

    val fingerStart = Offset(center.x + xOffset, center.y + yOffset)

    // For curved fingers, add some curvature
    val fingerEnd = if (color == Color(0xFFFFEB3B)) {
        // For partly curved (yellow) fingers
        Offset(
            fingerStart.x + (Math.sin(radians) * xDirection * fingerLength * 0.7f).toFloat(),
            fingerStart.y + (Math.cos(radians) * -fingerLength * 0.7f).toFloat()
        )
    } else if (color == Color(0xFFF44336) && fingerName != "thumb") {
        // For incorrect (red) fingers, make them straighter to show contrast
        Offset(
            fingerStart.x + (Math.sin(radians) * xDirection * fingerLength).toFloat(),
            fingerStart.y + (Math.cos(radians) * -fingerLength).toFloat()
        )
    } else {
        // Standard finger
        Offset(
            fingerStart.x + (Math.sin(radians) * xDirection * fingerLength).toFloat(),
            fingerStart.y + (Math.cos(radians) * -fingerLength).toFloat()
        )
    }

    // Draw the finger line
    drawLine(
        color = color,
        start = fingerStart,
        end = fingerEnd,
        strokeWidth = 8f,
        cap = StrokeCap.Round
    )

    // Draw a circle at the tip
    drawCircle(
        color = color,
        radius = 6f,
        center = fingerEnd
    )

    // Draw joint circles
    val joint1 = Offset(
        fingerStart.x + (fingerEnd.x - fingerStart.x) * 0.33f,
        fingerStart.y + (fingerEnd.y - fingerStart.y) * 0.33f
    )

    val joint2 = Offset(
        fingerStart.x + (fingerEnd.x - fingerStart.x) * 0.66f,
        fingerStart.y + (fingerEnd.y - fingerStart.y) * 0.66f
    )

    drawCircle(
        color = color.copy(alpha = 0.7f),
        radius = 4f,
        center = joint1
    )

    drawCircle(
        color = color.copy(alpha = 0.7f),
        radius = 4f,
        center = joint2
    )
}

/**
 * String extension to capitalize first letter
 */
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}