package com.google.experiment.soundexplorer.sample


import android.app.Activity
import com.google.experiment.soundexplorer.R
import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.google.experiment.soundexplorer.ui.theme.SoundExplorerTheme
import com.google.experiment.soundexplorer.sample.SoundExplorerViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.width


@Composable
fun SoundExplorerMainScreen2() {
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
                modifier = SubspaceModifier.width(800.dp)
            ) {
                ControlPanelRow()
            }
        }
    }
}

@Preview
@Composable
fun MainPanelPreview() {
    SoundExplorerMainScreen2()
}


@Composable
fun ControlPanelRow() {
    Row(
        modifier = Modifier
            .width(600.dp) // Total width constraint
            .height(157.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Panel with 3 Icon Buttons ---
        Toolbar()

        // Space between the two regions
        Spacer(modifier = Modifier.width(40.dp))

        // --- Play Icon Button ---
        Fab()
    }
}


@Composable
fun Toolbar(
    viewModel: SoundExplorerViewModel = viewModel()
) {

    val context = LocalContext.current

    // State to control the visibility of the popup menu
    var showMenuPopup by remember { mutableStateOf(false) }
    val panelBackgroundColor = Color(0xFFF0F4F9)
    val region1ButtonSize = 132.dp

    Surface(
        modifier = Modifier
            .width(412.dp)
            .height(157.dp),
        shape = RoundedCornerShape(79.dp),
        color = panelBackgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize() // Fill the Surface
                .padding(horizontal = (412.dp - (region1ButtonSize * 3 + 8.dp * 2)) / 2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between buttons
        ) {
            // Eraser Button
            IconButton(
                onClick = { viewModel.onEraserClick() },
                modifier = Modifier.size(region1ButtonSize)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_eraser),
                    contentDescription = "Eraser",
                    modifier = Modifier.size(75.dp)
                )
            }

            // Clear All Button
            IconButton(
                onClick = { viewModel.onClearAllClick() },
                modifier = Modifier.size(region1ButtonSize)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Clear All",
                    modifier = Modifier.size(75.dp)
                )
            }

            // Menu Button (relative positioning needed for Popup)
            Box(modifier = Modifier
                .size(region1ButtonSize)
            ) { // Wrap IconButton in Box to easily anchor Popup
                IconButton(
                    onClick = { showMenuPopup = true },
                    modifier = Modifier.matchParentSize() // Make IconButton fill the Box
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Menu",
                        modifier = Modifier.size(75.dp)
                    )
                }

                // --- Popup Menu ---
                if (showMenuPopup) {
                    val density = LocalDensity.current

                    Popup(
                        // Anchoring popup to the center top of the button.
                        alignment = Alignment.TopCenter,
                        // Offset needed to place it *above* the button
//                        offset = IntOffset(0, -200),
                        onDismissRequest = { showMenuPopup = false },
                        properties = PopupProperties(focusable = true) // Allow dismissing by clicking outside
                    ) {
                        // Popup content
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = panelBackgroundColor
                        ) {
                            Button(
                                onClick = {
                                    showMenuPopup = false // Close the popup
                                    // Close the Activity
                                    (context as? Activity)?.finish()
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null, // Text describes the action
                                        modifier = Modifier.size(45.dp) // Updated icon size
                                    )
                                    Spacer(Modifier.width(ButtonDefaults.IconSpacing)) // Standard spacing
                                    Text("Exit App")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ToolbarPreview() {
    Toolbar()
}

@Composable
fun Fab(
    viewModel: SoundExplorerViewModel = viewModel()
) {
    val panelBackgroundColor = Color(0xFFF0F4F9)
    Surface( // Use Surface for rounded background clipping
        modifier = Modifier.size(141.dp),
        shape = RoundedCornerShape(36.dp),
        color = panelBackgroundColor
    ) {
        IconButton(
            onClick = { viewModel.onPlayClick() },
            modifier = Modifier.fillMaxSize() // Fill the Surface
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                modifier = Modifier.size(75.dp) // Adjust icon size
            )
        }
    }
}

@Preview
@Composable
fun FabPreview() {
    Fab()
}