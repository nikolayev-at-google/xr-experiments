package com.example.xrexp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.concurrent.futures.await
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.SpatialCapabilities
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.*
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.scale
import androidx.xr.compose.subspace.layout.width
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.Session
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_3D_CONTENT
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_APP_ENVIRONMENT
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_EMBED_ACTIVITY
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_PASSTHROUGH_CONTROL
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_SPATIAL_AUDIO
import androidx.xr.scenecore.SpatialCapabilities.Companion.SPATIAL_CAPABILITY_UI
import androidx.xr.scenecore.addSpatialCapabilitiesChangedListener
import androidx.xr.scenecore.getSpatialCapabilities
import com.example.xrexp.ui.theme.XRExpTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class Main3DActivity : ComponentActivity() {

    companion object {
        const val TAG = "Main3DActivity"
        const val GLB_FILE_NAME = "models/animated_robot.glb"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocalSession.current?.let { session ->

                val c = session.getSpatialCapabilities()

                Log.d(TAG, "SpatialCapabilities: \n" +
                        "SPATIAL_CAPABILITY_UI:${c.hasCapability(SPATIAL_CAPABILITY_UI)}\n" +
                        "SPATIAL_CAPABILITY_3D_CONTENT:${c.hasCapability(SPATIAL_CAPABILITY_3D_CONTENT)}\n" +
                        "SPATIAL_CAPABILITY_PASSTHROUGH_CONTROL:${c.hasCapability(SPATIAL_CAPABILITY_PASSTHROUGH_CONTROL)}\n" +
                        "SPATIAL_CAPABILITY_APP_ENVIRONMENT:${c.hasCapability(SPATIAL_CAPABILITY_APP_ENVIRONMENT)}\n" +
                        "SPATIAL_CAPABILITY_SPATIAL_AUDIO:${c.hasCapability(SPATIAL_CAPABILITY_SPATIAL_AUDIO)}\n" +
                        "SPATIAL_CAPABILITY_EMBED_ACTIVITY:${c.hasCapability(SPATIAL_CAPABILITY_EMBED_ACTIVITY)}\n"
                )

                val scope = rememberCoroutineScope()
                XRExpTheme {
                    ModelInsideVolume(
                        xrSession = session,
                        spatialCapabilities = SpatialCapabilities.getOrCreate(session),
                        scope = scope
                    )
                }
            }
        }
    }

    @Composable
    fun ModelInsideVolume(
        xrSession: Session, spatialCapabilities : SpatialCapabilities, scope: CoroutineScope
    ) {
        Subspace {
            SpatialColumn {
                val context = LocalContext.current
                Volume(
                    /*
                     * the order in which Jetpack Composer’s modifier functions are called for
                     * SubspaceModifier of a Volume is very important in the Android XR app.
                     * This is what the right order of applying a chain of modifier functions
                     * should be (this may change in the future)
                     * SubspaceModifier
                     *   .offset(x, y, z)           // occurs first
                     *   .rotate(x, y, z)           // must be applied before scale
                     *   .scale(Float)
                     *   .movable()                 // must occur last
                     *   .resizable()               // must occur last
                     */
                    modifier = SubspaceModifier.offset(z = (-2000).dp).scale(0.5f).movable().resizable()
                ) {
                    scope.launch {
                        val gltfModel = GltfModel.create(xrSession, GLB_FILE_NAME).await()
                        // check for spatial capabilities
                        if (spatialCapabilities.isContent3dEnabled){
                            // create the gltf entity using the gltf file from the previous snippet
                            val gltfEntity = GltfModelEntity.create(xrSession, gltfModel)
//                            gltfEntity.setParent(xrSession.activitySpace)
                            it.addChild(gltfEntity)
                        } else {
                            Toast.makeText(context, "3D content not enabled", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                SpatialPanel(
                    SubspaceModifier.height(300.dp).width(700.dp)
                ) {
                    BottomEdgeOrbiter()
                }
            }
        }
    }

    @Composable
    fun BottomEdgeOrbiter() {
        Orbiter(
            alignment = Alignment.CenterHorizontally,
            offset = 100.dp,
            position = OrbiterEdge.Top,
        ) {
            Surface(
                Modifier
                    .clip(CircleShape)
            ) {
                Row(
                    Modifier
                        .width(700.dp)
                        .height(100.dp)
                        .background(Color(0.25f, 0.0f, 0.0f)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = GLB_FILE_NAME,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 6.em
                    )
                    Button(
                        onClick = { openSceneViewer() },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Viewer")
                    }
                }
            }
        }
    }

    private fun openSceneViewer() {
        val THREED_MODEL_URL = "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/FlightHelmet/glTF/FlightHelmet.gltf"
        val MIME_TYPE = "model/gltf-binary"
        val sceneViewerIntent = Intent(Intent.ACTION_VIEW)
        val intentUri =
            Uri.parse("https://arvr.google.com/scene-viewer/1.2")
                .buildUpon()
                .appendQueryParameter("file", THREED_MODEL_URL)
                .build()
        sceneViewerIntent.setDataAndType(intentUri, MIME_TYPE)
        startActivity(sceneViewerIntent)
    }
}
