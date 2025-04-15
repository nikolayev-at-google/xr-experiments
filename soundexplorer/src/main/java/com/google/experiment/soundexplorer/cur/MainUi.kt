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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.core.GlbModelRepository
import kotlin.math.floor
import kotlin.math.roundToInt

// MainViewModel.kt


// ToolbarComposable.kt
@Composable
fun Toolbar(
    onRefreshClick: () -> Unit,
    onAddClick: () -> Unit,
    onPauseClick: () -> Unit,
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

            // Pause button
            IconButton(onClick = onPauseClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Pause",
                    tint = Color.White
                )
            }
        }
    }
}

// MainScreen.kt
@Composable
fun MainScreen(
    modelRepository : GlbModelRepository,
    viewModel: MainViewModel = viewModel()
) {
    val isDialogVisible by viewModel.isDialogVisible.collectAsState()
    val isModelsVisible by viewModel.isModelsVisible.collectAsState()

    SpatialBox(
        alignment = SpatialAlignment.Center,
    ) {

        if (isModelsVisible) {
            ModelsSpatialPanelRow(modelRepository)
        }

        SpatialPanel {} // need to anchor orbiter
        Orbiter(
            position = OrbiterEdge.Bottom,
            offset = 96.dp
        ) {
            Toolbar(
                onRefreshClick = { viewModel.showDialog() },
                onAddClick = { viewModel.showModels() },
                onPauseClick = { /* Pause functionality */ },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .width(160.dp)
            )
        }

        // Dialog
        if (isDialogVisible) {
            RestartDialog()
        }
    }
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
    glbModel : GlbModel = GlbModel.GlbModel01Static,
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
    var shape by remember {
        mutableStateOf<GltfModel?>(null)
    }
    val mainExecutor = LocalActivity.current!!.mainExecutor
    val gltfEntity = shape?.let {
        remember {

            Log.d("TAG", "gltfEntity[${glbModel.assetName}] executionTime: ")
            // calculate time to execute commad to load model
            val startTime = System.currentTimeMillis()
            val gltfModelEntity = GltfModelEntity.create(session, it)
            gltfModelEntity.apply {
                addComponent(
                    InteractableComponent.create(session, mainExecutor) { ie ->
                        when (ie.action) {
                            InputEvent.ACTION_DOWN -> {
                                startAnimation(loop = false)
                            }
                            InputEvent.ACTION_HOVER_ENTER -> {
                            }
                            InputEvent.ACTION_HOVER_EXIT -> {
                            }
                        }
                    })
            }
            val endTime = System.currentTimeMillis()
            val executionTime = endTime - startTime

            Log.d("TAG", "gltfEntity executionTime[${glbModel.assetName}]: $executionTime")

            return@remember gltfModelEntity
        }
    }

    LaunchedEffect(glbModel.assetName) {
        Log.d("TAG", "executionTime[${glbModel.assetName}]: ")
        // calculate time to execute commad to load model
        val startTime = System.currentTimeMillis()
        shape = modelRepository.getOrLoadModel(glbModel).getOrNull() as GltfModel?
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        Log.d("TAG", "executionTime[${glbModel.assetName}]: $executionTime")
    }

    Box(
        contentAlignment = Alignment.Center,
    ) {
        if (gltfEntity != null) {
            Subspace {
                Volume(
                    modifier = SubspaceModifier.rotate(axisAngle, rotationValue).alpha(0.1f)
                ) {
                    gltfEntity.setParent(it)
                }
            }
        }
    }
}

@Composable
fun ModelsSpatialBoxRow(
    modelRepository : GlbModelRepository
) {
    SpatialRow(curveRadius = 300.dp) {
        val session = checkNotNull(LocalSession.current) {
            "LocalSession.current was null. Session must be available."
        }
        SpatialBox {
            var shape by remember { mutableStateOf<GltfModel?>(null) }
            val gltfEntity = shape?.let {
                remember {
                    GltfModelEntity.create(session, it)
                }
            }
            LaunchedEffect(Unit) {
                shape = modelRepository.getOrLoadModel(GlbModel.GlbModel01Static).getOrNull() as GltfModel?
            }
            if (gltfEntity != null) {
                Volume {
                    gltfEntity.setScale(1.0f)
                    gltfEntity.setParent(it)
                }
            }
        }
//            SpatialLayoutSpacer(modifier = SubspaceModifier.size(16.dp))
        SpatialBox {
            var shape by remember { mutableStateOf<GltfModel?>(null) }
            val gltfEntity = shape?.let {
                remember {
                    GltfModelEntity.create(session, it)
                }
            }
            LaunchedEffect(Unit) {
                shape = modelRepository.getOrLoadModel(GlbModel.GlbModel02Static).getOrNull() as GltfModel?
            }
            if (gltfEntity != null) {
                Volume {
                    gltfEntity.setAlpha(0.1f)
                    gltfEntity.setParent(it)
                }
            }
        }
//            SpatialLayoutSpacer(modifier = SubspaceModifier.size(16.dp))
        SpatialBox {
            var shape by remember { mutableStateOf<GltfModel?>(null) }
            val gltfEntity = shape?.let {
                remember {
                    GltfModelEntity.create(session, it)
                }
            }
            LaunchedEffect(Unit) {
                shape = modelRepository.getOrLoadModel(GlbModel.GlbModel03Inactive).getOrNull() as GltfModel?
            }
            if (gltfEntity != null) {
                Volume {
                    gltfEntity.setAlpha(0.1f)
                    gltfEntity.setParent(it)
                }
            }
        }
//            SpatialLayoutSpacer(modifier = SubspaceModifier.size(16.dp))
        SpatialBox {
            var shape by remember { mutableStateOf<GltfModel?>(null) }
            val gltfEntity = shape?.let {
                remember {
                    GltfModelEntity.create(session, it)
                }
            }
            LaunchedEffect(Unit) {
                shape = modelRepository.getOrLoadModel(GlbModel.GlbModel04Animated).getOrNull() as GltfModel?
            }
            if (gltfEntity != null) {
                Volume {
                    gltfEntity.setAlpha(0.1f)
                    gltfEntity.setParent(it)
                }
            }
        }
//            SpatialLayoutSpacer(modifier = SubspaceModifier.size(16.dp))
        SpatialBox {
            var shape by remember { mutableStateOf<GltfModel?>(null) }
            val gltfEntity = shape?.let {
                remember {
                    GltfModelEntity.create(session, it)
                }
            }
            LaunchedEffect(Unit) {
                shape = modelRepository.getOrLoadModel(GlbModel.GlbModel05Inactive).getOrNull() as GltfModel?
            }
            if (gltfEntity != null) {
                Volume {
                    gltfEntity.setAlpha(0.1f)
                    gltfEntity.setParent(it)
                }
            }
        }
//            SpatialLayoutSpacer(modifier = SubspaceModifier.size(16.dp))
        SpatialBox {
            var shape by remember { mutableStateOf<GltfModel?>(null) }
            val gltfEntity = shape?.let {
                remember {
                    GltfModelEntity.create(session, it)
                }
            }
            LaunchedEffect(Unit) {
                shape = modelRepository.getOrLoadModel(GlbModel.GlbModel06Static).getOrNull() as GltfModel?
            }
            if (gltfEntity != null) {
                Volume {
                    gltfEntity.setAlpha(0.1f)
                    gltfEntity.setParent(it)
                }
            }
        }
//            SpatialLayoutSpacer(modifier = SubspaceModifier.size(16.dp))
        SpatialBox {
            var shape by remember { mutableStateOf<GltfModel?>(null) }
            val gltfEntity = shape?.let {
                remember {
                    GltfModelEntity.create(session, it)
                }
            }
            LaunchedEffect(Unit) {
                shape = modelRepository.getOrLoadModel(GlbModel.GlbModel07Inactive).getOrNull() as GltfModel?
            }
            if (gltfEntity != null) {
                Volume {
                    gltfEntity.setAlpha(0.1f)
                    gltfEntity.setParent(it)
                }
            }
        }
//            SpatialLayoutSpacer(modifier = SubspaceModifier.size(16.dp))
        SpatialBox {
            var shape by remember { mutableStateOf<GltfModel?>(null) }
            val gltfEntity = shape?.let {
                remember {
                    GltfModelEntity.create(session, it)
                }
            }
            LaunchedEffect(Unit) {
                shape = modelRepository.getOrLoadModel(GlbModel.GlbModel08Animated).getOrNull() as GltfModel?
            }
            if (gltfEntity != null) {
                Volume {
                    gltfEntity.setAlpha(0.1f)
                    gltfEntity.setParent(it)
                }
            }
        }
//            SpatialLayoutSpacer(modifier = SubspaceModifier.size(16.dp))
        SpatialBox {
            var shape by remember { mutableStateOf<GltfModel?>(null) }
            val gltfEntity = shape?.let {
                remember {
                    GltfModelEntity.create(session, it)
                }
            }
            LaunchedEffect(Unit) {
                shape = modelRepository.getOrLoadModel(GlbModel.GlbModel09Inactive).getOrNull() as GltfModel?
            }
            if (gltfEntity != null) {
                Volume {
                    gltfEntity.setAlpha(0.1f)
                    gltfEntity.setParent(it)
                }
            }
        }
    }
}

@Composable
fun ModelsSpatialPanelRow(
    modelRepository : GlbModelRepository
) {
    SpatialRow {
        SpatialPanel(
            modifier = SubspaceModifier.width(64.dp).height(64.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
            ) {
                PanelContent(modelRepository = modelRepository, glbModel = GlbModel.GlbModel01Animated)
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
                PanelContent(modelRepository = modelRepository, glbModel = GlbModel.GlbModel02Animated)
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
                PanelContent(modelRepository = modelRepository, glbModel = GlbModel.GlbModel03Animated)
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
                PanelContent(modelRepository = modelRepository, glbModel = GlbModel.GlbModel04Animated)
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
                PanelContent(modelRepository = modelRepository, glbModel = GlbModel.GlbModel05Animated)
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
                PanelContent(modelRepository = modelRepository, glbModel = GlbModel.GlbModel06Animated)
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
                PanelContent(modelRepository = modelRepository, glbModel = GlbModel.GlbModel07Animated)
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
                PanelContent(modelRepository = modelRepository, glbModel = GlbModel.GlbModel08Animated)
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
                PanelContent(modelRepository = modelRepository, glbModel = GlbModel.GlbModel09Animated)
            }
        }
    }
}
