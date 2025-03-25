package com.google.experiment.soundexplorer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.spatial.Subspace
import com.google.experiment.soundexplorer.ui.theme.SoundExplorerTheme
import com.google.experiment.soundexplorer.vm.SoundExplorerViewModel

@Composable
fun SoundExplorerMainScreen(
    viewModel: SoundExplorerViewModel = viewModel()
) {
    SoundExplorerTheme {
        MainPanel()
    }
}

@Composable
fun MainPanel() {
    Subspace {
        Row {
            Orbiter(
                position = OrbiterEdge.Bottom
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("text")
                }
            }
        }
    }
}