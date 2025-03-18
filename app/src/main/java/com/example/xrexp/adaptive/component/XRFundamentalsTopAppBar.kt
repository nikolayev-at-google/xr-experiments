package com.example.xrexp.adaptive.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.xr.compose.platform.LocalHasXrSpatialFeature
import com.example.xrexp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun XRFundamentalsTopAppBar() {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        actions = {
            // Only show the mode toggle if the device supports spatial UI
            if (LocalHasXrSpatialFeature.current) {
                ToggleSpaceModeButton()
            }
        }
    )
}