package com.example.xrexp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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
import com.example.xrexp.ui.FullSpaceModeIconButton
import com.example.xrexp.ui.HomeSpaceModeIconButton
import com.example.xrexp.ui.SURFACE_TYPE_SURFACE_VIEW
import com.example.xrexp.ui.VideoPlayerSurface
import com.example.xrexp.ui.theme.XRExpTheme

class MainVideoActivity : ComponentActivity() {

    companion object {
        val videos =
            listOf(
                "https://cdn.bitmovin.com/content/assets/playhouse-vr/progressive.mp4",
                "https://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_720p_h264.mov",
                "https://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_1080p_h264.mov",
                "https://download.blender.org/peach/bigbuckbunny_movies/big_buck_bunny_1080p_surround.avi",
                "https://html5demos.com/assets/dizzy.mp4",
                "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/gear0/fileSequence0.aac",
                "https://storage.googleapis.com/exoplayer-test-media-1/gen-3/screens/dash-vod-single-segment/video-vp9-360.webm",
            )
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            XRExpTheme {
                val session = LocalSession.current!!
                if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    Subspace {
                        VideoSpatialContent(onRequestHomeSpaceMode = { session.spatialEnvironment.requestHomeSpaceMode() })
                    }
                } else {
                    Video2DContent(onRequestFullSpaceMode = { session.spatialEnvironment.requestFullSpaceMode() })
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun VideoSpatialContent(onRequestHomeSpaceMode: () -> Unit) {
        SpatialPanel(SubspaceModifier.width(1280.dp).height(800.dp).resizable().movable()) {
            Surface {
                VideoMainContent(
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
    fun Video2DContent(onRequestFullSpaceMode: () -> Unit) {
        Surface {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VideoMainContent(modifier = Modifier.padding(48.dp))
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
    fun VideoMainContent(modifier: Modifier = Modifier) {
        Surface {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val context = LocalContext.current
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(videos[1]))
                        prepare()
                        playWhenReady = true
                        repeatMode = Player.REPEAT_MODE_ONE
                    }
                }
                VideoPlayerSurface(
                    player = exoPlayer,
                    surfaceType = SURFACE_TYPE_SURFACE_VIEW,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }

}
