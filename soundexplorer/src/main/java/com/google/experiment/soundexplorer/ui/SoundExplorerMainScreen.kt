package com.google.experiment.soundexplorer.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.google.experiment.soundexplorer.ui.theme.SoundExplorerTheme
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.fillMaxWidth
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.width


@Composable
fun SoundExplorerMainScreen() {
    SoundExplorerTheme {
        MainPanel()
    }
}

@Composable
fun MainPanel() {
    Log.d("TAG", "MainPanel()")

    Subspace {
        SpatialRow {
            SpatialPanel(
                modifier = SubspaceModifier.width(550.dp).height(400.dp)
            ) {
                ActionScreen()
            }
        }
    }
}

@Preview
@Composable
fun SpatialActionScreenPreview() {
    SpatialPanel(
        modifier = SubspaceModifier
            .width(400.dp)
            .height(500.dp)
    ) {
        ActionScreen()
    }
}

@Preview
@Composable
fun MainPanelPreview() {
    SoundExplorerMainScreen()
}