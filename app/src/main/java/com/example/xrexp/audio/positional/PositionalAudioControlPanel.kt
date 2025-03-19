package com.example.xrexp.audio.positional

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.Volume
import androidx.xr.scenecore.GltfModelEntity
import com.example.xrexp.ui.theme.LocalSpacing
import com.example.xrexp.ui.theme.XRExpTheme


private const val TAG = "PositionalAudioControlPanel"

@Composable
fun PositionalAudioControlPanel(
    modifier: Modifier = Modifier,
    viewModel: PositionalAudioControlViewModel = viewModel()
) {
    val uiState = viewModel.uiState.value
    val session = LocalSession.current

    if (session != null)
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
            value = uiState.angle,
            onValueChange = { viewModel.onAngleChanged(it) },
            valueRange = 0f..360f,
            enabled = uiState.slidersEnabled
        )

        // Distance Slider
        PositionalAudioSliderWithTitle(
            title = "Distance",
            value = uiState.distance,
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
                DropDownWidget()

                Spacer(modifier = Modifier.padding(LocalSpacing.current.m))

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
                val model = viewModel.gltfModel.collectAsStateWithLifecycle()
                var modelEntity by remember { mutableStateOf<GltfModelEntity?>(null) }
                val modelPose = viewModel.modelPose.collectAsStateWithLifecycle()

                Volume { volumeEntity ->
                    // check for spatial capabilities
                    if (localSpatialCapabilities.isContent3dEnabled) {
                        model.value?.let { model ->
                            if (session != null) {
                                GltfModelEntity.create(session, model)?.let {
                                    modelEntity = it
                                    volumeEntity.addChild(it)
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "3D content not enabled", Toast.LENGTH_LONG).show()
                    }
                }

                modelEntity?.let {
                    modelEntity?.setPose(modelPose.value)
                    modelEntity?.setScale(0.5f)
                }
            }
        }
    }
}

@Composable
fun PositionalAudioSliderWithTitle(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean
) {
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
                text = if (title == "Angle") "${value.toInt()}°" else "%.1f".format(value),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
            steps = if (title == "Angle") 36 else 10
        )
    }
}

@Composable
fun DropDownWidget(
    viewModel: PositionalAudioControlViewModel = viewModel()
) {
    // Collect states from ViewModel
    val items by viewModel.items.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val isExpanded by viewModel.isDropdownExpanded.collectAsState()

    // Dropdown menu
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(220.dp)
    ) {
        OutlinedTextField(
            value = selectedItem ?: "Select an sound",
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .clickable { viewModel.onDropdownExpandedChange(!isExpanded) },
            trailingIcon = {
                IconButton(onClick = { viewModel.onDropdownExpandedChange(!isExpanded) }) {
                    Text(text = if (isExpanded) "▲" else "▼")
                }
            }
        )

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { viewModel.onDropdownExpandedChange(false) }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = { viewModel.onItemSelected(item) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DropDownWidgetPreview() {
    XRExpTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            DropDownWidget()
        }
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