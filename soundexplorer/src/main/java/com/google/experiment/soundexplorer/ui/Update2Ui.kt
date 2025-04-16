package com.google.experiment.soundexplorer.ui

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateColor
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
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
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.core.GlbModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.roundToInt

// MainViewModel.kt
@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel() {
    // UI state to track if the dialog is showing
    private val _isDialogVisible = MutableStateFlow(false)
    val isDialogVisible = _isDialogVisible.asStateFlow()

    private val _isModelsVisible = MutableStateFlow(false)
    val isModelsVisible = _isModelsVisible.asStateFlow()

    private val _toolbarPose = MutableStateFlow(Pose())
    val toolbarPose = _toolbarPose.asStateFlow()

    // Action to show dialog
    fun showDialog() {
        _isDialogVisible.value = !_isDialogVisible.value
        _isModelsVisible.value = false
    }

    fun showModels() {
        _isModelsVisible.value = !_isModelsVisible.value
        _isDialogVisible.value = false
    }

    fun setToolbarPose(pose: Pose) {
        _toolbarPose.value = pose
    }
}

// ToolbarComposable.kt
@Composable
fun Toolbar(
    onRefreshClick: () -> Unit,
    onAddClick: () -> Unit,
    onPauseClick: () -> Unit,
    modelRepository : GlbModelRepository,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
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
                    .background(Color(0xFFE6E6E6), CircleShape)
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

@Composable
fun ModelsRow(
    modelRepository : GlbModelRepository,
    viewModel: MainViewModel = viewModel()
) {
    val isModelsVisible by viewModel.isModelsVisible.collectAsState()
    val toolbarPose by viewModel.toolbarPose.collectAsState()

    Subspace {
        val session = checkNotNull(LocalSession.current) {
            "LocalSession.current was null. Session must be available."
        }

        var shape01 by remember { mutableStateOf<GltfModel?>(null) }
        val gltfEntity01 = shape01?.let {
            remember {
                val entity = GltfModelEntity.create(session, it)
                entity.setHidden(true)
                entity
            }
        }

        var shape02 by remember { mutableStateOf<GltfModel?>(null) }
        val gltfEntity02 = shape02?.let {
            remember {
                val entity = GltfModelEntity.create(session, it)
                entity.setHidden(true)
                entity
            }
        }
//
        var shape03 by remember { mutableStateOf<GltfModel?>(null) }
        val gltfEntity03 = shape03?.let {
            remember {
                val entity = GltfModelEntity.create(session, it)
                entity.setHidden(true)
                entity
            }
        }
//
        var shape04 by remember { mutableStateOf<GltfModel?>(null) }
        val gltfEntity04 = shape04?.let {
            remember {
                val entity = GltfModelEntity.create(session, it)
                entity.setHidden(true)
                entity
            }
        }
//
        var shape05 by remember { mutableStateOf<GltfModel?>(null) }
        val gltfEntity05 = shape05?.let {
            remember {
                val entity = GltfModelEntity.create(session, it)
                entity.setHidden(true)
                entity
            }
        }
//
        var shape06 by remember { mutableStateOf<GltfModel?>(null) }
        val gltfEntity06 = shape06?.let {
            remember {
                val entity = GltfModelEntity.create(session, it)
                entity.setHidden(true)
                entity
            }
        }
        //
        var shape07 by remember { mutableStateOf<GltfModel?>(null) }
        val gltfEntity07 = shape07?.let {
            remember {
                val entity = GltfModelEntity.create(session, it)
                entity.setHidden(true)
                entity
            }
        }
        //
        var shape08 by remember { mutableStateOf<GltfModel?>(null) }
        val gltfEntity08 = shape08?.let {
            remember {
                val entity = GltfModelEntity.create(session, it)
                entity.setHidden(true)
                entity
            }
        }
        //
        var shape09 by remember { mutableStateOf<GltfModel?>(null) }
        val gltfEntity09 = shape09?.let {
            remember {
                val entity = GltfModelEntity.create(session, it)
                entity.setHidden(true)
                entity
            }
        }



        gltfEntity01?.setPose(
            toolbarPose
                .translate(toolbarPose.up*0.15f)
        )
        gltfEntity02?.setPose(
            toolbarPose
                .translate(toolbarPose.left*0.15f)
                .translate(toolbarPose.up*0.15f)
        )
        gltfEntity03?.setPose(
            toolbarPose
                .translate(toolbarPose.left*0.3f)
                .translate(toolbarPose.up*0.15f)
        )
        gltfEntity04?.setPose(
            toolbarPose
                .translate(toolbarPose.left*0.45f)
                .translate(toolbarPose.up*0.15f)
        )
        gltfEntity05?.setPose(
            toolbarPose
                .translate(toolbarPose.left*0.6f)
                .translate(toolbarPose.up*0.15f)
        )
        gltfEntity06?.setPose(
            toolbarPose
                .translate(toolbarPose.up*0.15f)
                .translate(toolbarPose.right*0.15f)
        )
        gltfEntity07?.setPose(
            toolbarPose
                .translate(toolbarPose.up*0.15f)
                .translate(toolbarPose.right*0.3f)
        )
        gltfEntity08?.setPose(
            toolbarPose
                .translate(toolbarPose.up*0.15f)
                .translate(toolbarPose.right*0.45f)
        )
        gltfEntity09?.setPose(
            toolbarPose
                .translate(toolbarPose.up*0.15f)
                .translate(toolbarPose.right*0.6f)
        )

        gltfEntity01?.setHidden(isModelsVisible)
        gltfEntity02?.setHidden(isModelsVisible)
        gltfEntity03?.setHidden(isModelsVisible)
        gltfEntity04?.setHidden(isModelsVisible)
        gltfEntity05?.setHidden(isModelsVisible)
        gltfEntity06?.setHidden(isModelsVisible)
        gltfEntity07?.setHidden(isModelsVisible)
        gltfEntity08?.setHidden(isModelsVisible)
        gltfEntity09?.setHidden(isModelsVisible)

        LaunchedEffect(Unit) {
            shape01 = modelRepository.getOrLoadModel(GlbModel.GlbModel01Static).getOrNull() as GltfModel?
            shape02 = modelRepository.getOrLoadModel(GlbModel.GlbModel02Static).getOrNull() as GltfModel?
            shape03 = modelRepository.getOrLoadModel(GlbModel.GlbModel03Static).getOrNull() as GltfModel?
            shape04 = modelRepository.getOrLoadModel(GlbModel.GlbModel04Static).getOrNull() as GltfModel?
            shape05 = modelRepository.getOrLoadModel(GlbModel.GlbModel05Static).getOrNull() as GltfModel?
            shape06 = modelRepository.getOrLoadModel(GlbModel.GlbModel06Static).getOrNull() as GltfModel?
            shape07 = modelRepository.getOrLoadModel(GlbModel.GlbModel07Static).getOrNull() as GltfModel?
            shape08 = modelRepository.getOrLoadModel(GlbModel.GlbModel08Static).getOrNull() as GltfModel?
            shape09 = modelRepository.getOrLoadModel(GlbModel.GlbModel09Static).getOrNull() as GltfModel?
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
                modelRepository = modelRepository,
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
    SpatialRow(curveRadius = 90.dp) {
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
