package com.example.xrexp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.xr.compose.platform.LocalHasXrSpatialFeature
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.EdgeOffset
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SpatialRoundedCornerShape
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.width
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_3D_CONTENT
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_APP_ENVIRONMENT
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_EMBED_ACTIVITY
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_PASSTHROUGH_CONTROL
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_SPATIAL_AUDIO
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_UI
import androidx.xr.scenecore.addSpatialCapabilitiesChangedListener
import com.example.xrexp.ui.FullSpaceModeIconButton
import com.example.xrexp.ui.HomeSpaceModeIconButton
import com.example.xrexp.ui.theme.XRExpTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            XRExpTheme {
                val session = LocalSession.current!!

                session.addSpatialCapabilitiesChangedListener {
                    Log.d(TAG, "SpatialCapabilitiesChangedListener: \n" +
                            "SPATIAL_CAPABILITY_UI:${it.hasCapability(SPATIAL_CAPABILITY_UI)}\n" +
                            "SPATIAL_CAPABILITY_3D_CONTENT:${it.hasCapability(SPATIAL_CAPABILITY_3D_CONTENT)}\n" +
                            "SPATIAL_CAPABILITY_PASSTHROUGH_CONTROL:${it.hasCapability(SPATIAL_CAPABILITY_PASSTHROUGH_CONTROL)}\n" +
                            "SPATIAL_CAPABILITY_APP_ENVIRONMENT:${it.hasCapability(SPATIAL_CAPABILITY_APP_ENVIRONMENT)}\n" +
                            "SPATIAL_CAPABILITY_SPATIAL_AUDIO:${it.hasCapability(SPATIAL_CAPABILITY_SPATIAL_AUDIO)}\n" +
                            "SPATIAL_CAPABILITY_EMBED_ACTIVITY:${it.hasCapability(SPATIAL_CAPABILITY_EMBED_ACTIVITY)}\n"
                    )
                }

                if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    Subspace {
                        MySpatialContent(onRequestHomeSpaceMode = { session.spatialEnvironment.requestHomeSpaceMode() })
                    }
                } else {
                    My2DContent(onRequestFullSpaceMode = { session.spatialEnvironment.requestFullSpaceMode() })
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun MySpatialContent(onRequestHomeSpaceMode: () -> Unit) {
    SpatialPanel(SubspaceModifier.width(1280.dp).height(800.dp).resizable().movable()) {
        Surface {
            MainContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp)
            )
        }
        Orbiter(
            position = OrbiterEdge.Top,
            offset = EdgeOffset.inner(offset = 20.dp),
            alignment = Alignment.End,
            shape = SpatialRoundedCornerShape(CornerSize(28.dp))
        ) {
            HomeSpaceModeIconButton(
                onClick = onRequestHomeSpaceMode,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun My2DContent(onRequestFullSpaceMode: () -> Unit) {
    Surface {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MainContent(modifier = Modifier.padding(48.dp))
            if (LocalHasXrSpatialFeature.current) {
                FullSpaceModeIconButton(
                    onClick = onRequestFullSpaceMode,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    Text(text = stringResource(R.string.hello_android_xr), modifier = modifier)
}

@PreviewLightDark
@Composable
fun My2dContentPreview() {
    XRExpTheme {
        My2DContent(onRequestFullSpaceMode = {})
    }
}

@Preview(showBackground = true)
@Composable
fun FullSpaceModeButtonPreview() {
    XRExpTheme {
        FullSpaceModeIconButton(onClick = {})
    }
}

@PreviewLightDark
@Composable
fun HomeSpaceModeButtonPreview() {
    XRExpTheme {
        HomeSpaceModeIconButton(onClick = {})
    }
}