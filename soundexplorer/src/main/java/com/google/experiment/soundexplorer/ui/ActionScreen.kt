package com.google.experiment.soundexplorer.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.google.experiment.soundexplorer.R
import com.google.experiment.soundexplorer.vm.SoundExplorerViewModel


// --- Composable Functions ---

/**
 * Represents the main screen structure with the dark background and bottom actions.
 */
@Composable
fun ActionScreen(viewModel: SoundExplorerViewModel = viewModel()) {
    // State to control the visibility of the dropdown menu
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
//            .background(Color(0xFF2D2F31))
            .width(550.dp)
            .height(400.dp)
    ) {

        // --- Bottom Action Bar ---
        BottomActions(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp), // Added vertical padding
            showMenu = showMenu, // Pass state down
            onShowMenuChange = { showMenu = it }, // Lambda to update state
            onAddShapeClick = { viewModel.onAddShapeClick() },
            onPlayClick = { viewModel.onPlayClick() }
        )
    }
}

/**
 * Composable for the bottom row containing action buttons and the dropdown menu.
 */
@Composable
fun BottomActions(
    modifier: Modifier = Modifier,
    showMenu: Boolean, // Receive state
    onShowMenuChange: (Boolean) -> Unit, // Receive lambda to update state
    onAddShapeClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = modifier
            .width(550.dp)
            .height(400.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // --- Menu Button and Dropdown ---
        Box { // Needed to anchor the DropdownMenu to the IconButton
            IconButton(
                onClick = { onShowMenuChange(!showMenu) }, // Toggle menu visibility
                modifier = Modifier
                    .size(96.dp)
                    .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(12.dp))
                    // Slightly lighter background than main screen for the button
                    .background(Color(0xFF3F4143),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = "Menu",
                    tint = Color.White // Icon color
                )
            }

            // --- Dropdown Menu Definition ---
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { onShowMenuChange(false) }, // Close when clicking outside
                offset = DpOffset(x = (-10).dp, y = (-40).dp), // Adjust these values as needed
                modifier = Modifier
                    .width(200.dp) // Set custom width
                    .heightIn(min = 100.dp, max = 300.dp) // Set minimum and maximum height
                    .background(Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) // White bg, rounded
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Shapes") },
                    onClick = {
                        onShowMenuChange(false) // Close menu
                        // TODO: Implement Edit action
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = null
                        )
                    },
                    enabled = true
                )
                DropdownMenuItem(
                    text = { Text("Delete All") },
                    onClick = {
                        onShowMenuChange(false) // Close menu
                        // TODO: Implement Exit action
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_trash),
                            contentDescription = null
                        )
                    },
                    enabled = true
                )
                DropdownMenuItem(
                    text = { Text("Exit App") },
                    onClick = {
                        onShowMenuChange(false) // Close menu
                        // TODO: Implement Exit action
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_cross),
                            contentDescription = null
                        )
                    },
                    enabled = true
                )
            }
        } // End of Box for Menu Button and Dropdown

        Spacer(modifier = Modifier.width(28.dp))

        // --- Add Shape Button ---
        Button(
            modifier = Modifier.width(258.dp).height(96.dp),
            onClick = onAddShapeClick,
            shape = RoundedCornerShape(12.dp), // Pill shape
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2962FF)
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp) // Adjust padding
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = null, // Decorative
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text("Add Shape",
                    color = Color.White,
                    fontSize = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(28.dp))

        // --- Play Button ---
        IconButton(
            onClick = onPlayClick,
            modifier = Modifier
                .size(96.dp)
                // Same style as Menu button
                .background(Color(0xFFC2E7FF),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_play),
                contentDescription = "Play",
                tint = Color(0xFF004A77) // Icon color
            )
        }
    }
}


// --- Preview ---

@Preview(showBackground = true, widthDp = 550, heightDp = 400) // Simulate device size
@Composable
fun ActionScreenPreview() {
    // You can wrap with a theme if you have one defined
    // YourAppTheme {
    ActionScreen()
    // }
}

// Preview with the menu initially open to match the screenshot
@Preview(showBackground = true, widthDp = 550, heightDp = 400)
@Composable
fun ActionScreenMenuOpenPreview() {
    var showMenu by remember { mutableStateOf(true) } // Start with menu open

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2D2F31))) {
        BottomActions(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            showMenu = showMenu,
            onShowMenuChange = { showMenu = it },
            onAddShapeClick = { },
            onPlayClick = { }
        )
    }
}