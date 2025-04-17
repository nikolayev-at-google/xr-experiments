package com.google.experiment.soundexplorer.cur

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialBox
import androidx.xr.compose.subspace.SpatialLayoutSpacer
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.Volume
import androidx.xr.compose.subspace.layout.SpatialAlignment
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.alpha
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.compose.subspace.layout.size
import androidx.xr.compose.subspace.layout.width
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.ContentlessEntity
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.sound.SoundComposition
import com.google.experiment.soundexplorer.ui.SoundObjectComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.roundToInt

// MainViewModel.kt


// ToolbarComposable.kt
@Composable
fun Toolbar(
    onRefreshClick: () -> Unit,
    onAddClick: () -> Unit,
    viewModel: MainViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val isModelsVisible by viewModel.isModelsVisible.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = Color(0xFF2D2E31),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Refresh button
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White
                )
            }

            // Add button
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isModelsVisible) Color(0xFFC2E7FF) else Color( 0xFFE6E6E6), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.Black
                )
            }

            // play/pause button
            when (viewModel.soundComposition.state.collectAsState().value) {
                SoundComposition.State.LOADING -> {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Loading",
                            tint = Color.Gray
                        )
                    }
                }
                SoundComposition.State.READY -> {
                    IconButton(onClick = { viewModel.soundComposition.play() }) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White
                        )
                    }
                }
                SoundComposition.State.PLAYING -> {
                    IconButton(onClick = { viewModel.soundComposition.stop() }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(com.google.experiment.soundexplorer.R.drawable.ic_pause),
                            contentDescription = "Pause",
                            tint = Color.White
                        )
                    }
                }
                SoundComposition.State.STOPPED -> {
                    IconButton(onClick = { viewModel.soundComposition.play() }) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

// MainScreen.kt
@Composable
fun MainScreen(
    modelRepository : GlbModelRepository,
    soundObjects: Array<SoundObjectComponent>,
    viewModel: MainViewModel = viewModel()
) {
    val isDialogVisible by viewModel.isDialogVisible.collectAsState()
    val isModelsVisible by viewModel.isModelsVisible.collectAsState()

    SpatialBox(
        alignment = SpatialAlignment.Center,
    ) {

        if (isModelsVisible) {
            ModelsSpatialPanelRow(modelRepository, soundObjects)
        }

        SpatialPanel {} // need to anchor orbiter
        Orbiter(
            position = OrbiterEdge.Bottom,
            offset = 96.dp
        ) {
            ToolbarContent()
        }

        // Dialog
        if (isDialogVisible) {
            RestartDialog()
        }
    }
}

@Composable
fun ToolbarContent(
    viewModel: MainViewModel = viewModel()
) {
    Toolbar(
        onRefreshClick = { viewModel.showDialog() },
        onAddClick = { viewModel.showModels() },
        modifier = Modifier
            .padding(bottom = 16.dp)
            .width(160.dp)
    )
}

@Composable
fun RestartDialog() {
    SpatialPanel(
        modifier = SubspaceModifier.width(400.dp).height(240.dp)
    ) {
        RestartDialogContent()
    }
}

@Composable
fun RestartDialogContent() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2F2F2)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Title
            Text(
                text = "Start Fresh?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "All shapes will be removed from your space. You can rebuild anytime.",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Delete Button
                Button(
                    onClick = {},
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3C3C3C)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Delete All",
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Cancel Button (positioned to the right of Delete)
                TextButton(
                    onClick = {  },
//                            modifier = Modifier
//                                .padding(start = 160.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.DarkGray,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PanelContent(
    modelRepository : GlbModelRepository,
    glbModelActive : GlbModel = GlbModel.GlbModel01Static,
    glbModelInactive : GlbModel = GlbModel.GlbModel01Inactive,
    soundObject : SoundObjectComponent
) {

    val infiniteTransition = rememberInfiniteTransition()
    val singleRotationDurationMs = 5000

    val rotationValue by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 359f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(singleRotationDurationMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
    )

    val axisAngle by
    infiniteTransition.animateValue(
        initialValue = Vector3(0.1f, 0.0f, 0.0f),
        targetValue = Vector3(1.1f, 1.1f, 1.1f),
        typeConverter =
            TwoWayConverter(
                {
                    val axisSingleValue =
                        (it.x.roundToInt()) +
                                (2 * it.y.roundToInt()) +
                                (4 * it.z.roundToInt())
                    AnimationVector1D(axisSingleValue.toFloat() / 7)
                },
                {
                    val scaledAnimationValue = (it.value * 7) + 1.0f
                    val x = floor(scaledAnimationValue % 2)
                    val y = floor((scaledAnimationValue / 2) % 2)
                    val z = floor((scaledAnimationValue / 4) % 2)

                    Vector3(x, y, z)
                },
            ),
        animationSpec =
            infiniteRepeatable(
                animation = tween(singleRotationDurationMs * 7, easing = LinearEasing)
            ),
    )

    val session = checkNotNull(LocalSession.current) {
        "LocalSession.current was null. Session must be available."
    }

    var shapeActive by remember { mutableStateOf<GltfModel?>(null) }
    var shapeInactive by remember { mutableStateOf<GltfModel?>(null) }

    val scope = rememberCoroutineScope()
    /* var initializeSoundObjectJob by remember {
        mutableStateOf<Job?>(null)
    } */

    val mainExecutor = LocalActivity.current!!.mainExecutor

    val gltfEntity: Entity? =
        if (shapeActive == null || shapeInactive == null) {
            null
        } else {
            remember {
                Log.d("TAG", "gltfEntity[${glbModelActive.assetName}, ${glbModelInactive.assetName}] executionTime: ")
                // calculate time to execute commad to load model
                val startTime = System.currentTimeMillis()

                val shapeEntity = ContentlessEntity.create(session, "ModelIcon")

                val gltfActiveModelEntity = GltfModelEntity.create(session, checkNotNull(shapeActive))
                gltfActiveModelEntity.setParent(shapeEntity)

                val gltfInactiveModelEntity = GltfModelEntity.create(session, checkNotNull(shapeInactive))
                gltfInactiveModelEntity.setParent(shapeEntity)

                gltfActiveModelEntity.apply {
                    addComponent(
                        InteractableComponent.create(session, mainExecutor) { ie ->
                            if (ie.action == InputEvent.ACTION_DOWN) {
                                gltfInactiveModelEntity.setHidden(false)
                                // gltfInactiveModelEntity.startAnimation(loop = false)
                                gltfActiveModelEntity.setHidden(true)

                                /* if (initializeSoundObjectJob == null) {
                                    initializeSoundObjectJob = scope.launch(Dispatchers.Main) {
                                        if (session.spatialUser.head == null) {
                                            return@launch
                                        }

                                        val initialLocation = checkNotNull(session.spatialUser.head).transformPoseTo(
                                            Pose(Vector3.Forward * 1.0f, Quaternion.Identity),
                                            session.activitySpace)

                                        soundObject.initialize(initialLocation)
                                        soundObject.hidden = false
                                        soundObject.soundComponent.play()
                                    }
                                } else { */
                                    val initialLocation = checkNotNull(session.spatialUser.head).transformPoseTo(
                                        Pose(Vector3.Forward * 1.0f, Quaternion.Identity),
                                        session.activitySpace)

                                    soundObject.setPose(initialLocation)
                                    soundObject.hidden = false
                                    soundObject.soundComponent.play()
                                // }
                            } else if (ie.action == InputEvent.ACTION_HOVER_ENTER) {
                                gltfActiveModelEntity.setScale(1.4f)
                            } else if (ie.action == InputEvent.ACTION_HOVER_EXIT) {
                                gltfActiveModelEntity.setScale(1.0f)
                            }
                        })
                }

                gltfInactiveModelEntity.apply {
                    addComponent(
                        InteractableComponent.create(session, mainExecutor) { ie ->
                            if (ie.action == InputEvent.ACTION_DOWN) {
                                gltfActiveModelEntity.setHidden(false)
                                // gltfActiveModelEntity.startAnimation(loop = false)
                                gltfInactiveModelEntity.setHidden(true)
                                soundObject.soundComponent.stop()
                                soundObject.hidden = true
                            } else if (ie.action == InputEvent.ACTION_HOVER_ENTER) {
                                gltfInactiveModelEntity.setScale(1.4f)
                            } else if (ie.action == InputEvent.ACTION_HOVER_EXIT) {
                                gltfInactiveModelEntity.setScale(1.0f)
                            }
                        }
                    )
                }
                gltfInactiveModelEntity.setHidden(true)

                val endTime = System.currentTimeMillis()
                val executionTime = endTime - startTime

                Log.d("TAG", "gltfEntity executionTime[${glbModelActive.assetName}, ${glbModelInactive.assetName}]: $executionTime")

                return@remember shapeEntity
            }
        }

    LaunchedEffect(glbModelActive.assetName) {
        Log.d("TAG", "executionTime[${glbModelActive.assetName}]: ")
        // calculate time to execute commad to load model
        val startTime = System.currentTimeMillis()
        shapeActive = modelRepository.getOrLoadModel(glbModelActive).getOrNull() as GltfModel?
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        Log.d("TAG", "executionTime[${glbModelActive.assetName}]: $executionTime")
    }

    LaunchedEffect(glbModelInactive.assetName) {
        Log.d("TAG", "executionTime[${glbModelInactive.assetName}]: ")
        // calculate time to execute commad to load model
        val startTime = System.currentTimeMillis()
        shapeInactive = modelRepository.getOrLoadModel(glbModelInactive).getOrNull() as GltfModel?
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        Log.d("TAG", "executionTime[${glbModelInactive.assetName}]: $executionTime")
    }

    Box(
        contentAlignment = Alignment.Center,
    ) {
        if (gltfEntity != null) {
            Subspace {
                Volume(
                    modifier = SubspaceModifier.rotate(axisAngle, rotationValue) // .alpha(0.1f)
                ) {
                    gltfEntity.setParent(it)
                }
            }
        }
    }
}

@Composable
fun ModelsSpatialPanelRow(
    modelRepository : GlbModelRepository,
    soundObjects: Array<SoundObjectComponent>
) {
    SpatialRow {
        SpatialPanel(
            modifier = SubspaceModifier.width(64.dp).height(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                PanelContent(
                    modelRepository = modelRepository,
                    glbModelActive = GlbModel.GlbModel01Animated,
                    glbModelInactive = GlbModel.GlbModel01Inactive,
                    soundObjects[0])
            }
        }
        SpatialLayoutSpacer(modifier = SubspaceModifier.size(48.dp))
        SpatialPanel(
            modifier = SubspaceModifier.width(64.dp).height(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                PanelContent(
                    modelRepository = modelRepository,
                    glbModelActive = GlbModel.GlbModel02Animated,
                    glbModelInactive = GlbModel.GlbModel02Inactive,
                    soundObjects[1])
            }
        }
        SpatialLayoutSpacer(modifier = SubspaceModifier.size(48.dp))
        SpatialPanel(
            modifier = SubspaceModifier.width(64.dp).height(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                PanelContent(
                    modelRepository = modelRepository,
                    glbModelActive = GlbModel.GlbModel03Animated,
                    glbModelInactive = GlbModel.GlbModel03Inactive,
                    soundObjects[2])
            }
        }
        SpatialLayoutSpacer(modifier = SubspaceModifier.size(48.dp))
        SpatialPanel(
            modifier = SubspaceModifier.width(64.dp).height(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                PanelContent(
                    modelRepository = modelRepository,
                    glbModelActive = GlbModel.GlbModel04Animated,
                    glbModelInactive = GlbModel.GlbModel04Inactive,
                    soundObjects[3])
            }
        }
        SpatialLayoutSpacer(modifier = SubspaceModifier.size(48.dp))
        SpatialPanel(
            modifier = SubspaceModifier.width(64.dp).height(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                PanelContent(
                    modelRepository = modelRepository,
                    glbModelActive = GlbModel.GlbModel05Animated,
                    glbModelInactive = GlbModel.GlbModel05Inactive,
                    soundObjects[4])
            }
        }
        SpatialLayoutSpacer(modifier = SubspaceModifier.size(48.dp))
        SpatialPanel(
            modifier = SubspaceModifier.width(64.dp).height(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                PanelContent(
                    modelRepository = modelRepository,
                    glbModelActive = GlbModel.GlbModel06Animated,
                    glbModelInactive = GlbModel.GlbModel06Inactive,
                    soundObjects[5])
            }
        }
        SpatialLayoutSpacer(modifier = SubspaceModifier.size(48.dp))
        SpatialPanel(
            modifier = SubspaceModifier.width(64.dp).height(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                PanelContent(
                    modelRepository = modelRepository,
                    glbModelActive = GlbModel.GlbModel07Animated,
                    glbModelInactive = GlbModel.GlbModel07Inactive,
                    soundObjects[6])
            }
        }
        SpatialLayoutSpacer(modifier = SubspaceModifier.size(48.dp))
        SpatialPanel(
            modifier = SubspaceModifier.width(64.dp).height(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                PanelContent(
                    modelRepository = modelRepository,
                    glbModelActive = GlbModel.GlbModel08Animated,
                    glbModelInactive = GlbModel.GlbModel08Inactive,
                    soundObjects[7])
            }
        }
        SpatialLayoutSpacer(modifier = SubspaceModifier.size(48.dp))
        SpatialPanel(
            modifier = SubspaceModifier.width(64.dp).height(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                PanelContent(
                    modelRepository = modelRepository,
                    glbModelActive = GlbModel.GlbModel09Animated,
                    glbModelInactive = GlbModel.GlbModel09Inactive,
                    soundObjects[8])
            }
        }
    }
}
