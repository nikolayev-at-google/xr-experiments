package com.example.xrexp.adaptive.layout


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrexp.adaptive.component.PrimaryCard
import com.example.xrexp.adaptive.component.SecondaryCardList
import com.example.xrexp.ui.theme.XRExpTheme

@Composable
fun CompactLayout(
    modifier: Modifier = Modifier,
    primaryContent: @Composable () -> Unit,
    secondaryContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        primaryContent()
        Spacer(modifier = Modifier.height(32.dp))
        secondaryContent()
    }
}

@Preview
@Composable
fun CompactLayoutPreview() {
    XRExpTheme {
        CompactLayout(
            primaryContent = {
                PrimaryCard()
            },
            secondaryContent = {
                SecondaryCardList()
            }
        )
    }
}