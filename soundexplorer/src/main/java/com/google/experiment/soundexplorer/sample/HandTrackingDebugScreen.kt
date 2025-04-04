package com.google.experiment.soundexplorer.sample

import android.graphics.Paint
import android.util.Log
import androidx.concurrent.futures.await
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.platform.LocalSession
import androidx.xr.runtime.math.Pose
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.scenecore.Session

/**
 * Debug UI for hand tracking
 */
@Composable
fun HandTrackingDebugScreen(
    viewModel: HandTrackingViewModel = viewModel()
) {
    val leftHandData by viewModel.leftHandData.collectAsState()
//    val rightHandData by viewModel.rightHandData.collectAsState()
    val leftHandGesture by viewModel.leftHandGesture.collectAsState()
//    val rightHandGesture by viewModel.rightHandGesture.collectAsState()
    val session: Session = LocalSession.current!!
    val mainExecutor = LocalContext.current.mainExecutor

    var palmModel by remember {
        mutableStateOf<GltfModel?>(null)
    }
    val palmEntity = palmModel?.let {
        remember {
            GltfModelEntity.create(session, it).apply {
                setScale(0.005f)
                addComponent(InteractableComponent.create(session, mainExecutor) { event ->
                    when (event.action) {
                        InputEvent.ACTION_DOWN -> {
                            Log.d("TAG", "InputEvent.ACTION_DOWN")
                        }
                        InputEvent.ACTION_UP -> {
                            Log.d("TAG", "InputEvent.ACTION_UP")
                        }
                        InputEvent.ACTION_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_MOVE")
                        }
                        InputEvent.ACTION_CANCEL -> {
                            Log.d("TAG", "InputEvent.ACTION_CANCEL")
                        }
                        InputEvent.ACTION_HOVER_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_MOVE")
                        }
                        InputEvent.ACTION_HOVER_ENTER -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_ENTER")
                            setScale(0.009f)
                        }
                        InputEvent.ACTION_HOVER_EXIT -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_EXIT")
                            setScale(0.005f)
                        }
                        else -> {
                            Log.d("TAG", "InputEvent.OTHER: ${event.action} event:[$event]")
                        }
                    }
                })
            }
        }
    }

    var pinkyModel by remember {
        mutableStateOf<GltfModel?>(null)
    }
    val pinkyEntity = pinkyModel?.let {
        remember {
            GltfModelEntity.create(session, it).apply {
                setScale(0.005f)
                addComponent(InteractableComponent.create(session, mainExecutor) { event ->
                    when (event.action) {
                        InputEvent.ACTION_DOWN -> {
                            Log.d("TAG", "InputEvent.ACTION_DOWN")
                        }
                        InputEvent.ACTION_UP -> {
                            Log.d("TAG", "InputEvent.ACTION_UP")
                        }
                        InputEvent.ACTION_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_MOVE")
                        }
                        InputEvent.ACTION_CANCEL -> {
                            Log.d("TAG", "InputEvent.ACTION_CANCEL")
                        }
                        InputEvent.ACTION_HOVER_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_MOVE")
                        }
                        InputEvent.ACTION_HOVER_ENTER -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_ENTER")
                            setScale(0.009f)
                        }
                        InputEvent.ACTION_HOVER_EXIT -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_EXIT")
                            setScale(0.005f)
                        }
                        else -> {
                            Log.d("TAG", "InputEvent.OTHER: ${event.action} event:[$event]")
                        }
                    }
                })
            }
        }
    }

    var ringModel by remember {
        mutableStateOf<GltfModel?>(null)
    }
    val ringEntity = ringModel?.let {
        remember {
            GltfModelEntity.create(session, it).apply {
                setScale(0.005f)
                addComponent(InteractableComponent.create(session, mainExecutor) { event ->
                    when (event.action) {
                        InputEvent.ACTION_DOWN -> {
                            Log.d("TAG", "InputEvent.ACTION_DOWN")
                        }
                        InputEvent.ACTION_UP -> {
                            Log.d("TAG", "InputEvent.ACTION_UP")
                        }
                        InputEvent.ACTION_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_MOVE")
                        }
                        InputEvent.ACTION_CANCEL -> {
                            Log.d("TAG", "InputEvent.ACTION_CANCEL")
                        }
                        InputEvent.ACTION_HOVER_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_MOVE")
                        }
                        InputEvent.ACTION_HOVER_ENTER -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_ENTER")
                            setScale(0.009f)
                        }
                        InputEvent.ACTION_HOVER_EXIT -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_EXIT")
                            setScale(0.005f)
                        }
                        else -> {
                            Log.d("TAG", "InputEvent.OTHER: ${event.action} event:[$event]")
                        }
                    }
                })
            }
        }
    }

    var middleModel by remember {
        mutableStateOf<GltfModel?>(null)
    }
    val middleEntity = middleModel?.let {
        remember {
            GltfModelEntity.create(session, it).apply {
                setScale(0.005f)
                addComponent(InteractableComponent.create(session, mainExecutor) { event ->
                    when (event.action) {
                        InputEvent.ACTION_DOWN -> {
                            Log.d("TAG", "InputEvent.ACTION_DOWN")
                        }
                        InputEvent.ACTION_UP -> {
                            Log.d("TAG", "InputEvent.ACTION_UP")
                        }
                        InputEvent.ACTION_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_MOVE")
                        }
                        InputEvent.ACTION_CANCEL -> {
                            Log.d("TAG", "InputEvent.ACTION_CANCEL")
                        }
                        InputEvent.ACTION_HOVER_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_MOVE")
                        }
                        InputEvent.ACTION_HOVER_ENTER -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_ENTER")
                            setScale(0.009f)
                        }
                        InputEvent.ACTION_HOVER_EXIT -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_EXIT")
                            setScale(0.005f)
                        }
                        else -> {
                            Log.d("TAG", "InputEvent.OTHER: ${event.action} event:[$event]")
                        }
                    }
                })
            }
        }
    }

    var indexModel by remember {
        mutableStateOf<GltfModel?>(null)
    }
    val indexEntity = indexModel?.let {
        remember {
            GltfModelEntity.create(session, it).apply {
                setScale(0.005f)
                addComponent(InteractableComponent.create(session, mainExecutor) { event ->
                    when (event.action) {
                        InputEvent.ACTION_DOWN -> {
                            Log.d("TAG", "InputEvent.ACTION_DOWN")
                        }
                        InputEvent.ACTION_UP -> {
                            Log.d("TAG", "InputEvent.ACTION_UP")
                        }
                        InputEvent.ACTION_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_MOVE")
                        }
                        InputEvent.ACTION_CANCEL -> {
                            Log.d("TAG", "InputEvent.ACTION_CANCEL")
                        }
                        InputEvent.ACTION_HOVER_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_MOVE")
                        }
                        InputEvent.ACTION_HOVER_ENTER -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_ENTER")
                            setScale(0.009f)
                        }
                        InputEvent.ACTION_HOVER_EXIT -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_EXIT")
                            setScale(0.005f)
                        }
                        else -> {
                            Log.d("TAG", "InputEvent.OTHER: ${event.action} event:[$event]")
                        }
                    }
                })
            }
        }
    }

    var thumbModel by remember {
        mutableStateOf<GltfModel?>(null)
    }
    val thumbEntity = thumbModel?.let {
        remember {
            GltfModelEntity.create(session, it).apply {
                setScale(0.005f)
                addComponent(InteractableComponent.create(session, mainExecutor) { event ->
                    when (event.action) {
                        InputEvent.ACTION_DOWN -> {
                            Log.d("TAG", "InputEvent.ACTION_DOWN")
                        }
                        InputEvent.ACTION_UP -> {
                            Log.d("TAG", "InputEvent.ACTION_UP")
                        }
                        InputEvent.ACTION_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_MOVE")
                        }
                        InputEvent.ACTION_CANCEL -> {
                            Log.d("TAG", "InputEvent.ACTION_CANCEL")
                        }
                        InputEvent.ACTION_HOVER_MOVE -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_MOVE")
                        }
                        InputEvent.ACTION_HOVER_ENTER -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_ENTER")
                            setScale(0.009f)
                        }
                        InputEvent.ACTION_HOVER_EXIT -> {
                            Log.d("TAG", "InputEvent.ACTION_HOVER_EXIT")
                            setScale(0.005f)
                        }
                        else -> {
                            Log.d("TAG", "InputEvent.OTHER: ${event.action} event:[$event]")
                        }
                    }
                })
            }
        }
    }

    LaunchedEffect(Unit) {
        palmModel = GltfModel.create(session, "glb/02static.glb").await()
        pinkyModel = GltfModel.create(session, "glb/03static.glb").await()
        ringModel = GltfModel.create(session, "glb/04static.glb").await()
        middleModel = GltfModel.create(session, "glb/05static.glb").await()
        indexModel = GltfModel.create(session, "glb/06static.glb").await()
        thumbModel = GltfModel.create(session, "glb/07static.glb").await()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Hand Tracking Debug",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Left Hand
            item {
                HandDebugCard(
                    title = "Left Hand",
                    handData = leftHandData,
                    gesture = leftHandGesture
                )
            }

            if (leftHandGesture == "Open Palm") {
                // palm
                palmEntity?.setHidden(false)
                val transformedPose =
                    session.perceptionSpace.transformPoseTo(
                        leftHandData?.palmPose!!,
                        session.activitySpace,
                    )
                val newPosition = transformedPose.translation + transformedPose.down*0.05f
                palmEntity?.setPose(Pose(newPosition, transformedPose.rotation))

                // Pinky
                pinkyEntity?.setHidden(false)
                val pinkyTransformedPose =
                    session.perceptionSpace.transformPoseTo(
                        leftHandData?.fingerTips["Pinky"]!!,
                        session.activitySpace,
                    )
                val pinkyNewPosition = pinkyTransformedPose.translation + pinkyTransformedPose.down*0.05f
                pinkyEntity?.setPose(Pose(pinkyNewPosition, pinkyTransformedPose.rotation))

                // Ring
                ringEntity?.setHidden(false)
                val ringTransformedPose =
                    session.perceptionSpace.transformPoseTo(
                        leftHandData?.fingerTips["Ring"]!!,
                        session.activitySpace,
                    )
                val ringNewPosition = ringTransformedPose.translation + ringTransformedPose.down*0.05f
                ringEntity?.setPose(Pose(ringNewPosition, ringTransformedPose.rotation))

                // Middle
                middleEntity?.setHidden(false)
                val middleTransformedPose =
                    session.perceptionSpace.transformPoseTo(
                        leftHandData?.fingerTips["Middle"]!!,
                        session.activitySpace,
                    )
                val middleNewPosition = middleTransformedPose.translation + middleTransformedPose.down*0.05f
                middleEntity?.setPose(Pose(middleNewPosition, middleTransformedPose.rotation))

                // Index
                indexEntity?.setHidden(false)
                val indexTransformedPose =
                    session.perceptionSpace.transformPoseTo(
                        leftHandData?.fingerTips["Index"]!!,
                        session.activitySpace,
                    )
                val indexNewPosition = indexTransformedPose.translation + indexTransformedPose.down*0.05f
                indexEntity?.setPose(Pose(indexNewPosition, indexTransformedPose.rotation))

                // Thumb
                thumbEntity?.setHidden(false)
                val thumbTransformedPose =
                    session.perceptionSpace.transformPoseTo(
                        leftHandData?.fingerTips["Thumb"]!!,
                        session.activitySpace,
                    )
                val thumbNewPosition = thumbTransformedPose.translation + thumbTransformedPose.down*0.05f
                thumbEntity?.setPose(Pose(thumbNewPosition, thumbTransformedPose.rotation))

            } else {
                palmEntity?.setHidden(true)
                pinkyEntity?.setHidden(true)
                ringEntity?.setHidden(true)
                middleEntity?.setHidden(true)
                indexEntity?.setHidden(true)
                thumbEntity?.setHidden(true)
            }



        }
    }
}

@Composable
fun HandDebugCard(
    title: String,
    handData: HandTrackingViewModel.HandData?,
    gesture: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Gesture: $gesture",
                    style = MaterialTheme.typography.titleMedium,
                    color = when(gesture) {
                        "Open Palm" -> Color.Green
                        "Closed Fist" -> Color.Red
                        "Not Tracking" -> Color.Gray
                        else -> Color.Yellow
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (handData == null) {
                Text("No hand data available")
            } else {
                // Palm orientation
                PalmOrientationSection(handData)

                Spacer(modifier = Modifier.height(8.dp))

                // Finger extension values
                FingerExtensionSection(handData)

                Spacer(modifier = Modifier.height(8.dp))

                // Finger to palm distances
                FingerToPalmSection(handData)

                Spacer(modifier = Modifier.height(8.dp))

                // Hand visualization
                HandVisualization(handData)
            }
        }
    }
}

@Composable
fun PalmOrientationSection(handData: HandTrackingViewModel.HandData) {
    Column {
        Text(
            text = "Palm Orientation",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Palm facing user: ${handData.isPalmFacingUser}",
                modifier = Modifier.weight(1f)
            )

            // Small visualization of palm normal
            Canvas(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.LightGray)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val normalVector = handData.palmNormalVector

                if (normalVector != null) {
                    // Project 3D to 2D (simple projection)
                    val xDir = normalVector.x
                    val yDir = normalVector.y

                    // Draw line from center to edge based on normal direction
                    val lineLength = size.width / 3
                    val end = Offset(
                        center.x + xDir * lineLength,
                        center.y - yDir * lineLength // Invert Y for screen coordinates
                    )

                    drawLine(
                        color = if (handData.isPalmFacingUser) Color.Green else Color.Red,
                        start = center,
                        end = end,
                        strokeWidth = 4f
                    )

                    // Draw arrow tip
                    drawCircle(
                        color = if (handData.isPalmFacingUser) Color.Green else Color.Red,
                        radius = 4f,
                        center = end
                    )
                }
            }
        }
    }
}

@Composable
fun FingerExtensionSection(handData: HandTrackingViewModel.HandData) {
    Column {
        Text(
            text = "Finger Extension Ratios",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        handData.fingerExtensionRatios.forEach { (finger, ratio) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$finger: ",
                    modifier = Modifier.width(60.dp)
                )

                val isExtended = when(finger) {
                    "Index" -> ratio > 1.7f
                    "Middle" -> ratio > 1.8f
                    "Ring" -> ratio > 1.7f
                    "Pinky" -> ratio > 1.5f
                    else -> ratio > 1.5f
                }

                LinearProgressIndicator(
                    progress = {
                        ratio / 2.0f // Scale to make visualization more reasonable
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = if (isExtended) Color.Green else Color.Gray,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )

                Text(
                    text = String.format("%.2f", ratio),
                    modifier = Modifier.width(50.dp),
                    color = if (isExtended) Color.Green else Color.Gray
                )
            }
        }
    }
}

@Composable
fun FingerToPalmSection(handData: HandTrackingViewModel.HandData) {
    Column {
        Text(
            text = "Finger to Palm Distances",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        handData.fingerDistancesToPalm.forEach { (finger, distance) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$finger: ",
                    modifier = Modifier.width(60.dp)
                )

                val isClose = distance < 1.0f

                LinearProgressIndicator(
                    progress = {
                        (2.0f - distance.coerceIn(0f, 2.0f)) / 2.0f // Invert scale
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = if (isClose) Color.Red else Color.Gray,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )

                Text(
                    text = String.format("%.2f", distance),
                    modifier = Modifier.width(50.dp),
                    color = if (isClose) Color.Red else Color.Gray
                )
            }
        }
    }
}

@Composable
fun HandVisualization(handData: HandTrackingViewModel.HandData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Center point
            val center = Offset(size.width / 2, size.height / 2)

            // Draw palm
            drawCircle(
                color = if (handData.isPalmFacingUser) Color.Green.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f),
                radius = 40f,
                center = center
            )

            // Calculate scale factor based on canvas size
            val scale = size.minDimension / 4

            // Get palm position as reference
            val palmPos = handData.palmPose?.translation ?: return@Canvas

            // Draw lines from palm to finger tips
            handData.fingerTips.forEach { (fingerName, fingerPose) ->
                // Get relative position from palm
                val relativePos = fingerPose.translation - palmPos

                // Scale and convert to 2D (top-down view, X-Z plane)
                val fingerX = center.x + relativePos.x * scale
                val fingerY = center.y - relativePos.z * scale // Inverted for screen coordinates

                val fingerPoint = Offset(fingerX, fingerY)

                // Draw line from palm to finger
                drawLine(
                    color = when {
                        fingerName == "Thumb" -> Color.Magenta
                        handData.fingerExtensionRatios[fingerName]?.let {
                            when(fingerName) {
                                "Index" -> it > 1.7f
                                "Middle" -> it > 1.8f
                                "Ring" -> it > 1.7f
                                "Pinky" -> it > 1.5f
                                else -> it > 1.5f
                            }
                        } ?: false -> Color.Green
                        handData.fingerDistancesToPalm[fingerName]?.let { it < 1.0f } ?: false -> Color.Red
                        else -> Color.Gray
                    },
                    start = center,
                    end = fingerPoint,
                    strokeWidth = 4f
                )

                // Draw finger joint
                drawCircle(
                    color = Color.Blue,
                    radius = 5f,
                    center = fingerPoint
                )

                // Draw finger name
                drawContext.canvas.nativeCanvas.drawText(
                    fingerName,
                    fingerX,
                    fingerY - 10,
                    Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 30f
                    }
                )
            }

            // Draw palm normal vector
            handData.palmNormalVector?.let { normal ->
                val normalEnd = Offset(
                    center.x + normal.x * 50,
                    center.y - normal.z * 50 // Using Z for top-down view
                )

                drawLine(
                    color = if (handData.isPalmFacingUser) Color.Green else Color.Red,
                    start = center,
                    end = normalEnd,
                    strokeWidth = 5f
                )

                // Arrow tip
                drawCircle(
                    color = if (handData.isPalmFacingUser) Color.Green else Color.Red,
                    radius = 7f,
                    center = normalEnd
                )
            }
        }
    }
}