package com.google.experiment.soundexplorer.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.xr.scenecore.Model
import com.google.experiment.soundexplorer.sample.SoundExplorerViewModel
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.sample.AllModelsLoadingState

// --- Composable UI (SoundExplorerMainScreen, MainSceneContent) ---
// No changes needed here, they still observe the ViewModel's state.
@Composable
fun SoundExplorerMainScreen(viewModel: SoundExplorerViewModel) {
    // State collection remains the same
    val loadingState by viewModel.loadingState.collectAsState()
    Log.d("SoundExplorerMainScreen", "Recomposing with state: ${loadingState::class.simpleName}")

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = loadingState) {
            is AllModelsLoadingState.NotStarted -> {
                // State before loading is triggered in ViewModel
                Text("Waiting to start loading...")
            }
            is AllModelsLoadingState.LoadingInitiated -> {
                // Loading has been triggered
                CircularProgressIndicator()
                Text("Initializing loading...", modifier = Modifier.padding(top = 60.dp))
            }
            is AllModelsLoadingState.InProgress -> {
                // Show progress
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(progress = state.loadedCount.toFloat() / state.totalCount.toFloat())
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading models: ${state.loadedCount} / ${state.totalCount}")
                }
            }
            is AllModelsLoadingState.Ready -> {
                // All models loaded, show main UI
                Log.i("SoundExplorerMainScreen", "All models ready. Displaying MainSceneContent.")
                MainSceneContent(models = state.models)
            }
            is AllModelsLoadingState.Error -> {
                // Show error message
                val errorDetails = state.errors.entries.joinToString("\n") {
                    "- ${it.key}: ${it.value.localizedMessage ?: "Unknown error"}"
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Warning, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Error Loading Models:", fontWeight = FontWeight.Bold, color = Color.Red)
                    Text(errorDetails, color = Color.Red, modifier = Modifier.padding(horizontal = 16.dp))
                    // Optional: Add a retry button
                    // Button(onClick = { /* viewModel.retryLoading() // Needs implementation */ }) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
fun MainSceneContent(models: Map<GlbModel, Model>) {
    // Your main UI using the loaded models
    // e.g., creating GltfModelEntity instances from the `models` map
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Main Scene Content", style = MaterialTheme.typography.headlineSmall)
        Text("Models Available: ${models.size}")
        // ... Your scene rendering code ...
    }
}