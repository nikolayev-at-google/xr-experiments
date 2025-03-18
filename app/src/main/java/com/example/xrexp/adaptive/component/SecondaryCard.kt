package com.example.xrexp.adaptive.component


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun SecondaryCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        onClick = {}
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Text(
                stringResource(R.string.secondary_title),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                stringResource(R.string.secondary_description),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SecondaryCardList(modifier: Modifier = Modifier, numItems: Int = 15) {
    Column(
        modifier = modifier,
    ) {
        repeat(numItems) { index ->
            SecondaryCard()

            if (index != numItems - 1) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview
@Composable
fun SecondaryCardPreview() {
    XRExpTheme {
        SecondaryCard()
    }
}

@Preview
@Composable
fun SecondaryCardListPreview() {
    XRExpTheme {
        SecondaryCardList()
    }
}