package com.google.experiment.soundexplorer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableActionButton() {
    // State to track if the button is expanded
    var expanded by remember { mutableStateOf(false) }

    // Animate the width change
    val width by animateDpAsState(
        targetValue = if (expanded) 180.dp else 80.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "width"
    )

    Surface(
        modifier = Modifier
            .width(width)
            .clip(RoundedCornerShape(40.dp)),
        color = if (expanded) Color.DarkGray else Color.White
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left button (Refresh) with animation
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(),
                exit = fadeOut(animationSpec = tween(200)) + shrinkHorizontally()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { /* Handle refresh click */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Center Plus Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(if (expanded) 8.dp else 40.dp))
                    .background(Color.White)
                    .clickable { expanded = !expanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Right button (Play) with animation
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(),
                exit = fadeOut(animationSpec = tween(200)) + shrinkHorizontally()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { /* Handle play click */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableActionButtonWithLabel(
    onRefreshClick: () -> Unit = {},
    onPlayClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    val width by animateDpAsState(
        targetValue = if (expanded) 180.dp else 80.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "width"
    )

    Surface(
        modifier = Modifier
            .width(width)
            .clip(RoundedCornerShape(40.dp)),
        color = if (expanded) Color.DarkGray else Color.White
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left button (Refresh) with animation
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(),
                exit = fadeOut(animationSpec = tween(200)) + shrinkHorizontally()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onRefreshClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Center Plus Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(if (expanded) 8.dp else 40.dp))
                    .background(Color.White)
                    .clickable { expanded = !expanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Right button (Play) with animation
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(),
                exit = fadeOut(animationSpec = tween(200)) + shrinkHorizontally()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onPlayClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandableActionButtonPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        ExpandableActionButton()
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandableActionButtonWithLabelPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        ExpandableActionButtonWithLabel(
            onRefreshClick = { /* Handle refresh */ },
            onPlayClick = { /* Handle play */ }
        )
    }
}
