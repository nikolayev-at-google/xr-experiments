@file:Suppress("SpellCheckingInspection")

package com.google.experiment.soundexplorer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.experiment.soundexplorer.ui.ActionScreen
import kotlin.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.UiComposable
import androidx.compose.ui.unit.dp
import androidx.concurrent.futures.await
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialBox
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.SpatialRow
import androidx.xr.compose.subspace.SubspaceComposable
import androidx.xr.compose.subspace.Volume
import androidx.xr.compose.subspace.layout.SpatialAlignment
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.padding
import androidx.xr.compose.subspace.layout.size
import androidx.xr.compose.subspace.layout.testTag
import androidx.xr.compose.subspace.layout.width
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.subspace.layout.alpha
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.MovableComponent
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.sample.SoundExplorerViewModel
import com.google.experiment.soundexplorer.sound.SoundCompositionSimple
import com.google.experiment.soundexplorer.sound.SoundCompositionSimple.SoundSampleType
import com.google.experiment.soundexplorer.sound.SoundPoolManager
import com.google.experiment.soundexplorer.vm.SoundExplorerUiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.roundToInt


@AndroidEntryPoint
class SoundExplorerUiActivity : ComponentActivity() {
    companion object {
        private const val TAG = "UIActivity"
    }

    private val viewModel : SoundExplorerUiViewModel by viewModels()

    @Inject
    lateinit var modelRepository : GlbModelRepository

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate started.")

//        viewModel.triggerModelLoading(modelRepository)

        lifecycleScope.launch {
            for (identifier in GlbModel.allGlbAnimatedModels) {
                launch { // Launch each retrieval job
                    Log.d(TAG, "Requesting model '${identifier.assetName}' from repository.")
                    // Call the repository's suspend function
                    val notUsed = modelRepository.getOrLoadModel(identifier)
                }
            }
        }

        setContent {
            val session = LocalSession.current
            if (session == null) {
                return@setContent
            }

            this.viewModel.initialize(session)

            Subspace { MainMenu() } }
    }

    @Composable
    fun MainMenu(viewModel: SoundExplorerUiViewModel = viewModel()) {
            SpatialBox(
                modifier = SubspaceModifier.width(600.dp).height(600.dp),
                alignment = SpatialAlignment.BottomCenter,
            ) {
                val isOpen = viewModel.addShapeMenuOpen.collectAsState()
                if (isOpen.value)
                    ShapeMenu()
                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(600.dp)
                        .offset(y = (-350).dp)
                        .movable()
                ) {
                    ActionScreen()
                }
            }
    }

    @Composable
    fun ShapeMenu() {
        val soundsReady = viewModel.soundComponentsReady.collectAsState()
        if (!soundsReady.value) {
            return
        }

        SpatialRow(SubspaceModifier.testTag("ShapeGrid")) {
            val columnPanelModifier = SubspaceModifier.size(150.dp).padding(20.dp)
            SpatialColumn {
                AppPanel(
                    modifier = columnPanelModifier.testTag("02_static"),
                    glbFileName = "glb2/02_static.glb",
                    glbModel = GlbModel.GlbModel01Anim,
                    checkNotNull(viewModel.soundComponents.instrument1Component)
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("05_static"),
                    glbFileName = "glb2/05_static.glb",
                    glbModel = GlbModel.GlbModel02Anim,
                    checkNotNull(viewModel.soundComponents.instrument2Component)
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("08_static"),
                    glbFileName = "glb2/08_static.glb",
                    glbModel = GlbModel.GlbModel03Anim,
                    checkNotNull(viewModel.soundComponents.instrument3Component)
                )
            }
            SpatialColumn {
                AppPanel(
                    modifier = columnPanelModifier.testTag("10_static"),
                    glbFileName = "glb2/10_static.glb",
                    glbModel = GlbModel.GlbModel04Anim,
                    checkNotNull(viewModel.soundComponents.instrument4Component)
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("11_static"),
                    glbFileName = "glb2/11_static.glb",
                    glbModel = GlbModel.GlbModel05Anim,
                    checkNotNull(viewModel.soundComponents.instrument5Component)
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("16_static"),
                    glbFileName = "glb2/16_static.glb",
                    glbModel = GlbModel.GlbModel06Anim,
                    checkNotNull(viewModel.soundComponents.instrument6Component)
                )
            }
            SpatialColumn {
                AppPanel(
                    modifier = columnPanelModifier.testTag("18_static"),
                    glbFileName = "glb2/18_static.glb",
                    glbModel = GlbModel.GlbModel07Anim,
                    checkNotNull(viewModel.soundComponents.instrument7Component)
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("01_animated"),
                    glbFileName = "glb2/01_animated.glb",
                    glbModel = GlbModel.GlbModel08Anim,
                    checkNotNull(viewModel.soundComponents.instrument8Component)
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("01_animated"),
                    glbFileName = "glb2/01_animated.glb",
                    glbModel = GlbModel.GlbModel09Anim,
                    checkNotNull(viewModel.soundComponents.instrument9Component)
                )
            }
        }

        // Currently, all sound components need to be added to entities before the composition can
        // be played. todo- modify to be robust to this
        val unattachedComponents = viewModel.soundComposition.unattachedComponents.collectAsState()
        if (unattachedComponents.value == 0) {
            viewModel.soundComposition.play()
        }
    }

    @SubspaceComposable
    @Composable
    fun AppPanel(
        modifier: SubspaceModifier = SubspaceModifier,
        glbFileName: String = "glb2/01_animated.glb",
        glbModel : GlbModel = GlbModel.GlbModel01,
        soundComponent: SoundCompositionSimple.SoundCompositionComponent
    ) {
        SpatialPanel(modifier = modifier.movable(
            onPoseChange = { poseEvent ->
                Log.d(TAG, "onPoseChange: $poseEvent")
                viewModel.onModelPoseChange(glbModel, poseEvent)
                false
            }
        )) {
            PanelContent(glbFileName = glbFileName, glbModel = glbModel, soundComponent = soundComponent)
        }
    }

    @UiComposable
    @Composable
    fun PanelContent(
        glbFileName: String,
        glbModel : GlbModel = GlbModel.GlbModel01,
        soundComponent: SoundCompositionSimple.SoundCompositionComponent
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

        val color by
        infiniteTransition.animateColor(
            initialValue = Color.Red,
            targetValue = Color(0xff800000), // Dark Red
            animationSpec =
                infiniteRepeatable(
                    // Linearly interpolate between initialValue and targetValue every 1000ms.
                    animation = tween(1000, easing = LinearEasing),
                    // Once [TargetValue] is reached, starts the next iteration in reverse (i.e.
                    // from
                    // TargetValue to InitialValue). Then again from InitialValue to
                    // TargetValue. This
                    // [RepeatMode] ensures that the animation value is *always continuous*.
                    repeatMode = RepeatMode.Reverse
                )
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
        var isOrbiterVisible by remember { mutableStateOf(false) }
        var shape by remember {
            mutableStateOf<GltfModel?>(null)
        }
        val gltfEntity = shape?.let {
            remember {

                Log.d(TAG, "gltfEntity[${glbModel.assetName}] executionTime: ")
                // calculate time to execute commad to load model
                val startTime = System.currentTimeMillis()
                val gltfModelEntity = GltfModelEntity.create(session, it)
                gltfModelEntity.addComponent(soundComponent)
                gltfModelEntity.apply {
                    addComponent(
                        InteractableComponent.create(session, mainExecutor) { ie ->
                        when (ie.action) {
                            InputEvent.ACTION_DOWN -> {
                                startAnimation(loop = false)

                                // todo- currently this just iterates through the different sounds on click
                                //       we need to implement the real behavior where the sound changes when moved
                                if (!soundComponent.isPlaying) {
                                    soundComponent.soundType = SoundSampleType.LOW
                                    soundComponent.play()
                                } else {
                                    when (soundComponent.soundType) {
                                        SoundSampleType.LOW ->
                                            soundComponent.soundType = SoundSampleType.MEDIUM
                                        SoundSampleType.MEDIUM ->
                                            soundComponent.soundType = SoundSampleType.HIGH
                                        SoundSampleType.HIGH ->
                                            soundComponent.stop()
                                    }
                                }
                            }
                            InputEvent.ACTION_HOVER_ENTER -> {
                                isOrbiterVisible = true
//                                setScale(1.3f)
                            }
                            InputEvent.ACTION_HOVER_EXIT -> {
                                isOrbiterVisible = false
//                                setScale(1f)
                            }
                        }
                    })
                }
                val endTime = System.currentTimeMillis()
                val executionTime = endTime - startTime

                Log.d(TAG, "gltfEntity executionTime[${glbModel.assetName}]: $executionTime")

                return@remember gltfModelEntity
            }
        }

        LaunchedEffect(glbFileName) {
            Log.d(TAG, "executionTime[${glbModel.assetName}]: ")
            // calculate time to execute commad to load model
            val startTime = System.currentTimeMillis()
            shape = modelRepository.getOrLoadModel(glbModel).getOrNull() as GltfModel?
            val endTime = System.currentTimeMillis()
            val executionTime = endTime - startTime
            Log.d(TAG, "executionTime[${glbModel.assetName}]: $executionTime")
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
            if (isOrbiterVisible) {
                Orbiter(
                    position = OrbiterEdge.Bottom,
                    offset = 64.dp
                ) {
                    IconButton(
                        onClick = {  },
                        modifier = Modifier
                            .width(56.dp)
                            .background(Color.White, shape = RoundedCornerShape(12.dp)),
                    ) {
                        Icon(imageVector = Icons.Filled.Menu, contentDescription = "Add highlight")
                    }
                }
            }
        }
    }
}