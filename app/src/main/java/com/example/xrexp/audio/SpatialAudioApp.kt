package com.example.xrexp.audio

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SubspaceComposable
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.alpha
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.padding
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.size
import androidx.xr.compose.subspace.layout.width
import com.example.xrexp.audio.ambisonic.AmbisonicAudioPlayerPanel
import com.example.xrexp.audio.positional.PositionalAudioControlPanel
import com.example.xrexp.audio.stereo.StereoAudioPlayerPanel
import com.example.xrexp.audio.surround.SurroundAudioPlayerPanel
import com.example.xrexp.ui.theme.LocalSpacing


@Composable
fun SpatialAudioApp() {
    if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
        // This is a top-level subspace
        if (LocalSpatialCapabilities.current.isSpatialAudioEnabled) {
            // Spatial audio is supported – we can proceed to use spatial audio features
            SpatialAudioFrame()
        } else {
            // Fallback: spatial audio not available
            Log.e("SpatialAudioApp", "Spatial audio not enabled!!!", )
        }
    } else {
        NonSpatialAudioFrame()
        Log.e("SpatialAudioApp", "Spatial Ui not enabled!!!", )
    }
}

@Composable
fun SpatialAudioFrame() {
    Subspace {
        SpatialColumn {
            SpatialPanel(
                SubspaceModifier.width(1280.dp).height(72.dp).padding(bottom = LocalSpacing.current.m)
            ) {
                SpatialAudioSupportCard()
            }
            SpatialPanel(
                SubspaceModifier.width(1280.dp).height(330.dp).padding(bottom = LocalSpacing.current.m)
            ) {
                PositionalAudioCard()
            }
            SpatialPanel(
                SubspaceModifier.width(1280.dp).padding(bottom = LocalSpacing.current.m)
            ) {
                StereoSoundCard()
            }
            SpatialPanel(
                SubspaceModifier.width(1280.dp).padding(bottom = LocalSpacing.current.m)
            ) {
                SurroundSoundCard()
            }
            SpatialPanel(
                SubspaceModifier.width(1280.dp).padding(bottom = LocalSpacing.current.m)
            ) {
                AmbisonicAudioCard()
            }
        }
    }
}

@Composable
fun NonSpatialAudioFrame() {
    Subspace {
        SpatialPanel(
            SubspaceModifier
                .width(1280.dp)
                .height(1080.dp)
                .resizable()
                .movable()
        ) {
            Surface {
                MainContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(LocalSpacing.current.xl)
                )
            }
        }
    }
}

/**
 * Main debug visualization composable
 */
@Composable
fun MainContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(LocalSpacing.current.m)
    ) {
        SpatialAudioSupportCard()

        Spacer(modifier = Modifier.height(LocalSpacing.current.m))

        PositionalAudioCard()

        Spacer(modifier = Modifier.height(LocalSpacing.current.m))

        StereoSoundCard()

        Spacer(modifier = Modifier.height(LocalSpacing.current.m))

        SurroundSoundCard()

        Spacer(modifier = Modifier.height(LocalSpacing.current.m))

        AmbisonicAudioCard()
    }
}

@Composable
fun SpatialAudioSupportCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (LocalSpatialCapabilities.current.isSpatialAudioEnabled)
                Color(0xFF4CAF50)
            else
                Color(0xFFF44336)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalSpacing.current.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (LocalSpatialCapabilities.current.isSpatialAudioEnabled)
                    "Application may use spatial audio"
                else
                    "ERROR: Spatial audio not supported!!!",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (LocalSpatialCapabilities.current.isSpatialAudioEnabled)
                        Color.White
                    else
                        Color.DarkGray
                )
            )
        }
    }
}

@Composable
fun PositionalAudioCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalSpacing.current.m)
        ) {
            Row {
                Text(
                    text = "Positional Audio (3D Positional Sound)",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            PositionalAudioControlPanel()
        }
    }
}

@Composable
fun StereoSoundCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalSpacing.current.m)
        ) {
            Text(
                text = "Stereo Sound",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            StereoAudioPlayerPanel()
        }
    }
}

@Composable
fun SurroundSoundCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalSpacing.current.m)
        ) {
            Text(
                text = "Surround Sound (5.1 Multi-Channel Audio)",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            SurroundAudioPlayerPanel()
        }
    }
}

@Composable
fun AmbisonicAudioCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalSpacing.current.m)
        ) {
            Text(
                text = "Ambisonic Audio (360° Sound Field)",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            AmbisonicAudioPlayerPanel()
        }
    }
}

