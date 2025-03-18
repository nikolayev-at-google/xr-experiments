package com.example.xrexp.ui


import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.scenecore.Session
import com.example.xrexp.R
import com.example.xrexp.environment.EnvironmentController
import com.example.xrexp.ui.theme.XRExpTheme

/**
 * Controls for changing the user's Environment, and toggling between Home Space and Full Space
 */
@Composable
fun EnvironmentControls(modifier: Modifier = Modifier) {
    // If we aren't able to access the session, these buttons wouldn't work and shouldn't be shown
    val activity = LocalActivity.current
    if (LocalSession.current != null && activity is ComponentActivity) {
        val uiIsSpatialized = LocalSpatialCapabilities.current.isSpatialUiEnabled
        val environmentController = remember(activity) {
            val session = Session.create(activity)
            EnvironmentController(session, activity.lifecycleScope)
        }
        //load the model early so it's in memory for when we need it
        val environmentModelName = "env/green_hills_ktx2_mipmap.glb"
        environmentController.loadModelAsset(environmentModelName)

        Surface(modifier.clip(CircleShape)) {
            Row(Modifier.width(IntrinsicSize.Min)) {
                if (uiIsSpatialized) {
                    SetVirtualEnvironmentButton {
                        environmentController.requestCustomEnvironment(
                            environmentModelName
                        )
                    }
                    SetPassthroughButton { environmentController.requestPassthrough(1f) }
                    VerticalDivider(
                        modifier = Modifier
                            .height(32.dp)
                            .align(Alignment.CenterVertically),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    RequestHomeSpaceButton { environmentController.requestHomeSpaceMode() }
                } else {
                    RequestFullSpaceButton { environmentController.requestFullSpaceMode() }
                }
            }
        }
    }
}

@Composable
fun SetVirtualEnvironmentButton(
    modifier: Modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.onSecondary, CircleShape),
    @DrawableRes iconResId: Int = R.drawable.environment_24px,
    @StringRes stringResId: Int = R.string.set_virtual_environment,
    onclick: () -> Unit
) {
    IconButton(
        onClick = onclick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = stringResource(stringResId),
        )
    }
}

@Composable
fun SetPassthroughButton(
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int = R.drawable.passthrough_24px,
    @StringRes stringResId: Int = R.string.set_passthrough,
    onclick: () -> Unit
) {
    IconButton(
        onClick = onclick,
        modifier = modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.onSecondary, CircleShape)
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = stringResource(stringResId),
        )
    }
}

@Composable
fun RequestHomeSpaceButton(onclick: () -> Unit) {
    IconButton(
        onClick = onclick,
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.onSecondary, CircleShape)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_request_home_space),
            contentDescription = stringResource(R.string.switch_to_home_space_mode)
        )
    }
}

@Composable
fun RequestFullSpaceButton(onclick: () -> Unit) {
    IconButton(
        onClick = onclick, modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_request_full_space),
            contentDescription = stringResource(R.string.switch_to_full_space_mode)
        )
    }
}

@Preview
@Composable
private fun PreviewSetVirtualEnvironmentButton() {
    XRExpTheme {
        SetVirtualEnvironmentButton {}
    }
}

@Preview
@Composable
private fun PreviewRequestHomeSpaceButton() {
    XRExpTheme {
        RequestHomeSpaceButton {}
    }
}

@Preview
@Composable
private fun PreviewRequestFullSpaceButton() {
    XRExpTheme {
        RequestFullSpaceButton {}
    }
}

@Preview
@Composable
private fun PreviewSetPassthroughButton() {
    XRExpTheme {
        SetPassthroughButton {}
    }
}