package com.google.experiment.soundexplorer.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


// --- Composable Functions ---

/**
 * Represents the main screen structure with the dark background and bottom actions.
 */
@Composable
fun ActionScreen() {
    // State to control the visibility of the dropdown menu
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D2F31)) // Dark background color from image
    ) {

        // Box to anchor the DropdownMenu. Position it slightly above the button area.
        // In a real app, this Box might wrap the actual menu button in the BottomActions.
        // For this example, we place it manually in the Box layout.
        // Positioning it precisely requires more context, but placing it near TopStart
        // with offset can approximate the visual if needed when 'showMenu' is true.
        // However, the standard way is anchoring it directly to the triggering button.
        // Let's keep the state here but render the menu within BottomActions near the anchor.

        // --- Bottom Action Bar ---
        BottomActions(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp), // Added vertical padding
            showMenu = showMenu, // Pass state down
            onShowMenuChange = { showMenu = it }, // Lambda to update state
            onAddShapeClick = { /* TODO: Implement Add Shape action */ },
            onPlayClick = { /* TODO: Implement Play action */ }
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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // Use SpaceBetween for alignment
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Menu Button and Dropdown ---
        Box { // Needed to anchor the DropdownMenu to the IconButton
            IconButton(
                onClick = { onShowMenuChange(!showMenu) }, // Toggle menu visibility
                modifier = Modifier
                    .size(56.dp)
                    // Slightly lighter background than main screen for the button
                    .background(Color(0xFF3F4143), shape = RoundedCornerShape(16.dp))
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White // Icon color
                )
            }

            // --- Dropdown Menu Definition ---
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { onShowMenuChange(false) }, // Close when clicking outside
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(8.dp)) // White bg, rounded
            ) {
                // Custom composable for menu items to match styling
                CustomDropdownMenuItem(
                    text = "Edit Shapes",
                    icon = Icons.Default.Edit,
                    onClick = {
                        onShowMenuChange(false) // Close menu
                        // TODO: Implement Edit Shapes action
                    }
                )
                CustomDropdownMenuItem(
                    text = "Delete All",
                    icon = Icons.Default.Delete,
                    onClick = {
                        onShowMenuChange(false) // Close menu
                        // TODO: Implement Delete All action
                    }
                )
                CustomDropdownMenuItem(
                    text = "Close App",
                    icon = Icons.Default.Close,
                    onClick = {
                        onShowMenuChange(false) // Close menu
                        // TODO: Implement Close App action
                    }
                )
            }
        } // End of Box for Menu Button and Dropdown

        // --- Add Shape Button ---
        Button(
            onClick = onAddShapeClick,
            shape = RoundedCornerShape(percent = 50), // Pill shape
//            colors = ButtonDefaults.buttonColors(
//                backgroundColor = Color(0xFF4A80F0) // Blue color from image
//            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp) // Adjust padding
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null, // Decorative
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text("Add Shape", color = Color.White)
            }
        }

        // --- Play Button ---
        IconButton(
            onClick = onPlayClick,
            modifier = Modifier
                .size(56.dp)
                // Same style as Menu button
                .background(Color(0xFF3F4143),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White // Icon color
            )
        }
    }
}

/**
 * Custom composable for Dropdown menu items to precisely match the image style.
 */
@Composable
fun CustomDropdownMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick,
        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) }
    )
}


// --- Preview ---

@Preview(showBackground = true, widthDp = 380, heightDp = 700) // Simulate device size
@Composable
fun ActionScreenPreview() {
    // You can wrap with a theme if you have one defined
    // YourAppTheme {
    ActionScreen()
    // }
}

// Preview with the menu initially open to match the screenshot
@Preview(showBackground = true, widthDp = 380, heightDp = 700)
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