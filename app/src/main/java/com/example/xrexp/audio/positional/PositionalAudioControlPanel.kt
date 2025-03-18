package com.example.xrexp.audio.positional

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.Volume
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.GltfModelEntity
import com.example.xrexp.ui.theme.XRExpTheme


private const val TAG = "PositionalAudioControlPanel"

@Composable
fun PositionalAudioControlPanel(
    modifier: Modifier = Modifier,
    viewModel: PositionalAudioControlViewModel = viewModel()
) {
    val uiState = viewModel.uiState.value
    val session = LocalSession.current!!

    LaunchedEffect(Unit) {
        Log.i(TAG, "PositionalAudioControlPanel  -  LaunchedEffect")
        viewModel.loadModel(session)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Angle Slider
        PositionalAudioSliderWithTitle(
            title = "Angle",
            onValueChange = { viewModel.onAngleChanged(it) },
            valueRange = 0f..360f,
            enabled = uiState.slidersEnabled
        )

        // Distance Slider
        PositionalAudioSliderWithTitle(
            title = "Distance",
            onValueChange = { viewModel.onDistanceChanged(it) },
            valueRange = 0f..10f,
            enabled = uiState.slidersEnabled
        )

        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play Button
            Button(
                onClick = { viewModel.onPlayClicked() },
                enabled = !uiState.isPlaying
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play")
            }

            // Stop Button
            Button(
                onClick = { viewModel.onStopClicked() },
                enabled = uiState.isPlaying
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Stop"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Stop")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Loop Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.loop,
                    onCheckedChange = { viewModel.onLoopChanged(it) }
                )
                Text("Loop")
            }
        }
    }

    // Dialog
    if (uiState.showDialog) {
        Subspace {
            SpatialColumn {
                val context = LocalContext.current
                val localSpatialCapabilities = LocalSpatialCapabilities.current
                val distance = viewModel.distance.collectAsStateWithLifecycle()
                val model = viewModel.gltfModel.collectAsStateWithLifecycle()
                var modelEntity by remember { mutableStateOf<GltfModelEntity?>(null) }

                Log.d(TAG, "-----------------: ${distance.value}")

                Volume { volumeEntity ->
                    // check for spatial capabilities
                    if (localSpatialCapabilities.isContent3dEnabled) {
                        model.value?.let { model ->

                            Log.d(TAG, "============================: ${distance.value}")

                            GltfModelEntity.create(session, model)?.let {
                                modelEntity = it
                                volumeEntity.addChild(it)
                            }
                        }
                    } else {
                        Toast.makeText(context, "3D content not enabled", Toast.LENGTH_LONG).show()
                    }
                }

                modelEntity?.let {

                    Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>: ${distance.value}")

                    // Transformations
                    val translation = Vector3(0f, 0f, -distance.value)
                    val orientation = Quaternion.fromEulerAngles(0f, -45f, 0f)
                    val pose = Pose(translation, orientation)
                    modelEntity?.setPose(pose)
                    modelEntity?.setScale(0.5f)
                }
            }
        }
    }
}

@Composable
fun PositionalEntityPanel(
    viewModel: PositionalAudioControlViewModel = viewModel()
) {

    val uiState = viewModel.uiState.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Animation in Progress",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Angle: ${uiState.angle.toInt()}°\nDistance: ${"%.1f".format(viewModel.distance.value)} units",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.onDismissDialog() }
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
fun PositionalAudioSliderWithTitle(
    title: String,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    viewModel: PositionalAudioControlViewModel = viewModel()
) {
    val distance = viewModel.distance.collectAsStateWithLifecycle()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (title == "Angle") "${distance.value.toInt()}°" else "${"%.1f".format(distance.value)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Slider(
            value = distance.value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
//            steps = if (title == "Angle") 36 else 10
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ControlPanelPreview() {
    XRExpTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PositionalAudioControlPanel()
        }
    }
}