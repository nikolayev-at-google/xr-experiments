package com.example.xrexp.adaptive.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrexp.adaptive.component.PrimaryCard
import com.example.xrexp.adaptive.component.SecondaryCardList
import com.example.xrexp.ui.theme.XRExpTheme

@Composable
fun ExpandedLayout(
    primaryContent: @Composable () -> Unit,
    secondaryContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .weight(3f)
        ) {
            primaryContent()
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .weight(2f)
        ) {
            secondaryContent()
        }
    }
}

@Preview(widthDp = 800)
@Composable
fun ExpandedLayoutPreview() {
    XRExpTheme {
        ExpandedLayout(
            primaryContent = {
                PrimaryCard()
            },
            secondaryContent = {
                SecondaryCardList()
            }
        )
    }
}