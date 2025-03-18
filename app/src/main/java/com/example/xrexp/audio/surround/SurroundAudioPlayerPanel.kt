package com.example.xrexp.audio.surround

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.xrexp.R
import com.example.xrexp.ui.theme.XRExpTheme


@Composable
fun SurroundAudioPlayerPanel(
    modifier: Modifier = Modifier,
    viewModel: SurroundAudioPlayerViewModel = viewModel()
) {
    val context = LocalContext.current

    // Initialize the player when the composable is first launched
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display current position and duration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = viewModel.formatTime(viewModel.currentPosition))
            Text(text = viewModel.formatTime(viewModel.audioDuration))
        }

        // Progress bar
        LinearProgressIndicator(
            progress = {
                if (viewModel.audioDuration > 0) {
                    viewModel.currentPosition.toFloat() / viewModel.audioDuration.toFloat()
                } else {
                    0f
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Media controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play button
            IconButton(
                onClick = { viewModel.play() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.play_circle_24px),
                    contentDescription = "Play",
                    tint = if (viewModel.isAudioPlaying) Color.Gray else MaterialTheme.colorScheme.primary
                )
            }

            // Pause button
            IconButton(
                onClick = { viewModel.pause() },
                enabled = viewModel.isPauseEnabled
            ) {
                Icon(
                    painter = painterResource(R.drawable.pause_circle_24px),
                    contentDescription = "Pause",
                    tint = if (viewModel.isPauseEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            // Stop button
            IconButton(
                onClick = { viewModel.stop() },
                enabled = viewModel.isStopEnabled
            ) {
                Icon(
                    painter = painterResource(R.drawable.stop_circle_24px),
                    contentDescription = "Stop",
                    tint = if (viewModel.isStopEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            // Loop checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.isLooping,
                    onCheckedChange = { viewModel.toggleLoop() }
                )
                Text("Loop")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AudioPlayerPreview() {
    XRExpTheme {
        Surface {
            SurroundAudioPlayerPanel()
        }
    }
}
