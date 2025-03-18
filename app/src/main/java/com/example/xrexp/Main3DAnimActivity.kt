package com.example.xrexp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.concurrent.futures.await
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.*
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.width
import com.example.xrexp.ui.theme.XRExpTheme
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.Session
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.InteractableComponent
import java.util.concurrent.Executors


class Main3DAnimActivity : ComponentActivity() {

    companion object {
        const val GLB_FILE_NAME = "models/shiba_inu_texture_updated.glb"
    }

    private lateinit var GLB : GltfModelEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val session = LocalSession.current!!
            XRExpTheme {
                AnimatedSceneInFullSpace(session)
            }
        }

    }

    @Composable
    fun AnimatedSceneInFullSpace(xrSession: Session) {
        val root: Entity = xrSession.activitySpaceRoot
        val executor by lazy { Executors.newSingleThreadExecutor() }

        Subspace {
            LaunchedEffect(Unit) {
                // Model Loading
                val gltfModel = GltfModel.create(xrSession, GLB_FILE_NAME).await()
                val gltfEntity = GltfModelEntity.create(xrSession, gltfModel)

                // Global pty initialization
                GLB = gltfEntity

                // Transformations
                val translation = Vector3(0.5f, -2.7f, -2.4f)
                val orientation = Quaternion.fromEulerAngles(0f, -90f, 0f)
                val pose = Pose(translation, orientation)
                gltfEntity.setPose(pose)
                gltfEntity.setScale(1.5f)

                // Playing and stopping Animation
                gltfEntity.startAnimation(loop = false, animationName = "sitting_skeletal.3")
                gltfEntity.stopAnimation()

                // Setting an Interactable Component
                val interactable = InteractableComponent.create(xrSession, executor) { ie ->
                    when (ie.action) {
                        InputEvent.ACTION_HOVER_ENTER -> {
                            gltfEntity.setScale(2.7f)
                            println(gltfEntity.getScale())
                        }
                        InputEvent.ACTION_HOVER_EXIT -> {
                            gltfEntity.setScale(1.5f)
                            println(gltfEntity.getScale())
                        }
                    }
                }
                gltfEntity.addComponent(interactable)
                gltfEntity.setParent(root)
            }
            SpatialPanel(
                SubspaceModifier
                    .height(80.dp)
                    .width(320.dp)
                    .offset(x = 50.dp, y = 120.dp)
            ) {
                AnimationSwitch()
            }
        }
    }

    @Composable
    private fun ButtonPrototype(
        modifier: Modifier = Modifier,
        tint: Color,
        onClick: () -> Unit
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier.padding(16.dp),
        ) {
            Icon(
                tint = tint,
                modifier = Modifier.scale(1.75f),
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null
            )
        }
    }

    @Composable
    private fun AnimationSwitch() {

        /*
        *  "Static Pose"
        *   "play_dead_skeletal.3"
        *   "rollover_skeletal.3"
        *   "shake_skeletal.3"
        *  "sitting_skeletal.3"
        *   "standing_skeletal.3"
         */

        Surface(
            Modifier.clip(CircleShape)
        ) {
            Row(
                Modifier.width(IntrinsicSize.Min)
            ) {
                ButtonPrototype(tint = Color.Magenta) {
                    GLB.startAnimation(loop = true, animationName = "standing_skeletal.3")
                }
                ButtonPrototype(tint = Color.Green) {
                    GLB.startAnimation(loop = true, animationName = "rollover_skeletal.3")
                }
                ButtonPrototype(tint = Color.Yellow) {
                    GLB.startAnimation(loop = true, animationName = "shake_skeletal.3")
                }
                ButtonPrototype(tint = Color.Cyan) {
                    GLB.startAnimation(loop = true, animationName = "play_dead_skeletal.3")
                }
            }
        }
    }
}
