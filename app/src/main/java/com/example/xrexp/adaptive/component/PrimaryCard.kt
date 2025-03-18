package com.example.xrexp.adaptive.component


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrexp.R
import com.example.xrexp.ui.theme.XRExpTheme

@Composable
fun PrimaryCard(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(
                stringResource(R.string.primary_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                stringResource(R.string.primary_description),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview
@Composable
fun PrimaryCardPreview() {
    XRExpTheme {
        PrimaryCard()
    }
}