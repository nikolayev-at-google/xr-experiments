@file:Suppress("SpellCheckingInspection")

package com.google.experiment.soundexplorer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.experiment.soundexplorer.ui.ActionScreen
import kotlin.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
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
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.subspace.layout.height
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.InputEventListener
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.sound.SoundComposition
import com.google.experiment.soundexplorer.ui.EntityMoveInteractionHandler
import com.google.experiment.soundexplorer.ui.SimpleSimulationComponent
import com.google.experiment.soundexplorer.ui.SoundEntityMovementHandler
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

    private var soundComponents: Array<SoundComposition.SoundCompositionComponent>? = null

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

            this.viewModel.initializeSoundComposition(session)

            val soundsLoaded = this.viewModel.soundPool.soundsLoaded.collectAsState()
            if (!soundsLoaded.value) { // do something while sounds are loading?
                return@setContent
            }

            if (soundComponents == null) {
                soundComponents = arrayOf(
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst01lowId, viewModel.soundPool.inst01midId, viewModel.soundPool.inst01highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst02lowId, viewModel.soundPool.inst02midId, viewModel.soundPool.inst02highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst03lowId, viewModel.soundPool.inst03midId, viewModel.soundPool.inst03highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst04lowId, viewModel.soundPool.inst04midId, viewModel.soundPool.inst04highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst05lowId, viewModel.soundPool.inst05midId, viewModel.soundPool.inst05highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst06lowId, viewModel.soundPool.inst06midId, viewModel.soundPool.inst06highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst07lowId, viewModel.soundPool.inst07midId, viewModel.soundPool.inst07highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst08lowId, viewModel.soundPool.inst08midId, viewModel.soundPool.inst08highId),
                    viewModel.soundComposition.addComponent(
                        viewModel.soundPool.inst09lowId, viewModel.soundPool.inst09midId, viewModel.soundPool.inst09highId))
            }

            Subspace { MainMenu(checkNotNull(soundComponents)) } }
    }

    @Composable
    fun MainMenu(soundComponents: Array<SoundComposition.SoundCompositionComponent>,
                 viewModel: SoundExplorerUiViewModel = viewModel()) {
            SpatialBox(
                modifier = SubspaceModifier.width(600.dp).height(600.dp),
                alignment = SpatialAlignment.BottomCenter,
            ) {
                val isOpen = viewModel.addShapeMenuOpen.collectAsState()
                if (isOpen.value)
                    ShapeMenu(soundComponents)
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
    fun ShapeMenu(soundComponents: Array<SoundComposition.SoundCompositionComponent>) {
        SpatialRow(SubspaceModifier.testTag("ShapeGrid")) {
            val columnPanelModifier = SubspaceModifier.size(150.dp).padding(20.dp)
            SpatialColumn {
                AppPanel(
                    modifier = columnPanelModifier.testTag("02_static"),
                    glbFileName = "glb2/02_static.glb",
                    glbModel = GlbModel.GlbModel01Animated,
                    soundComponents[0]
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("05_static"),
                    glbFileName = "glb2/05_static.glb",
                    glbModel = GlbModel.GlbModel02Animated,
                    soundComponents[1]
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("08_static"),
                    glbFileName = "glb2/08_static.glb",
                    glbModel = GlbModel.GlbModel03Animated,
                    soundComponents[2]
                )
            }
            SpatialColumn {
                AppPanel(
                    modifier = columnPanelModifier.testTag("10_static"),
                    glbFileName = "glb2/10_static.glb",
                    glbModel = GlbModel.GlbModel04Animated,
                    soundComponents[3]
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("11_static"),
                    glbFileName = "glb2/11_static.glb",
                    glbModel = GlbModel.GlbModel05Animated,
                    soundComponents[4]
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("16_static"),
                    glbFileName = "glb2/16_static.glb",
                    glbModel = GlbModel.GlbModel06Animated,
                    soundComponents[5]
                )
            }
            SpatialColumn {
                AppPanel(
                    modifier = columnPanelModifier.testTag("18_static"),
                    glbFileName = "glb2/18_static.glb",
                    glbModel = GlbModel.GlbModel07Animated,
                    soundComponents[6]
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("01_animated"),
                    glbFileName = "glb2/01_animated.glb",
                    glbModel = GlbModel.GlbModel08Animated,
                    soundComponents[7]
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("01_animated"),
                    glbFileName = "glb2/01_animated.glb",
                    glbModel = GlbModel.GlbModel09Animated,
                    soundComponents[8]
                )
            }
        }
    }

    @SubspaceComposable
    @Composable
    fun AppPanel(
        modifier: SubspaceModifier = SubspaceModifier,
        glbFileName: String = "glb2/01_animated.glb",
        glbModel : GlbModel = GlbModel.GlbModel01Static,
        soundComponent: SoundComposition.SoundCompositionComponent
    ) {
        SpatialPanel(modifier = modifier /* .movable(
            onPoseChange = { poseEvent ->
                Log.d(TAG, "onPoseChange: $poseEvent")
                viewModel.onModelPoseChange(glbModel, poseEvent)
                false
            }
        ) */) {
            PanelContent(glbFileName = glbFileName, glbModel = glbModel, soundComponent)
        }
    }

    // @UiComposable
    @Composable
    fun PanelContent(
        glbFileName: String,
        glbModel : GlbModel = GlbModel.GlbModel01Static,
        soundComponent: SoundComposition.SoundCompositionComponent
    ) {

        /* val infiniteTransition = rememberInfiniteTransition()
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
        ) */

        val session = checkNotNull(LocalSession.current) {
                "LocalSession.current was null. Session must be available."
            }
        // var isOrbiterVisible by remember { mutableStateOf(false) }
        var shape by remember {
            mutableStateOf<GltfModel?>(null)
        }
        val gltfEntity = shape?.let {
            remember {
                Log.d(TAG, "gltfEntity[${glbModel.assetName}] executionTime: ")
                // calculate time to execute commad to load model
                val startTime = System.currentTimeMillis()
                val gltfModelEntity = GltfModelEntity.create(session, it)

                /* gltfModelEntity.apply {
                    addComponent(
                        InteractableComponent.create(session, mainExecutor) { ie ->
                        when (ie.action) {
                            InputEvent.ACTION_HOVER_ENTER -> {
                                isOrbiterVisible = true
                                // setScale(1.2f)
                            }
                            InputEvent.ACTION_HOVER_EXIT -> {
                                isOrbiterVisible = false
                                // setScale(1f)
                            }
                        }
                    })
                } */
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

        val scope = rememberCoroutineScope()

        if (gltfEntity != null) {
            var manipulationEntity by remember {
                mutableStateOf<Entity?>(null)
            }
            Subspace {
                Volume {
                    entity ->
                    scope.launch {
                        manipulationEntity = entity // todo - probably a less brittle way to set this up
                    }
                }
                if (manipulationEntity != null) {
                    Volume(
                        // modifier = SubspaceModifier.rotate(axisAngle, rotationValue) // .alpha(0.1f)
                    ) {
                        entity ->
                        scope.launch {
                            entity.setParent(checkNotNull(manipulationEntity))
                            gltfEntity.setParent(entity)

                            val tapHandler = object : InputEventListener {
                                override fun onInputEvent(ie: InputEvent) {
                                    when (ie.action) {
                                        InputEvent.ACTION_UP -> {
                                            gltfEntity.startAnimation(loop = false)

                                            if (!soundComponent.isPlaying) {
                                                soundComponent.play()
                                            } else {
                                                soundComponent.stop()
                                            }
                                        }
                                    }
                                }
                            }

                            gltfEntity.addComponent(soundComponent)

                            val lowBehavior = {
                                    e: Entity, dT: Double ->
                                e.setPose(Pose(
                                    e.getPose().translation,
                                    e.getPose().rotation * Quaternion.fromAxisAngle(Vector3.Up, 20.0f * dT.toFloat())
                                ))
                            }

                            val mediumBehavior = {
                                    e: Entity, dT: Double ->
                                e.setPose(Pose(
                                    e.getPose().translation,
                                    e.getPose().rotation * Quaternion.fromAxisAngle(Vector3.One, 40.0f * dT.toFloat())
                                ))
                            }

                            val highBehavior = {
                                    e: Entity, dT: Double ->
                                e.setPose(Pose(
                                    e.getPose().translation,
                                    e.getPose().rotation * Quaternion.fromAxisAngle(Vector3.Right, 70.0f * dT.toFloat())
                                ))
                            }

                            val simComponent = SimpleSimulationComponent(lifecycleScope, mediumBehavior)

                            gltfEntity.addComponent(simComponent)

                            soundComponent.onPropertyChanged = {
                                if (!soundComponent.isPlaying) {
                                    simComponent.paused = true
                                } else {
                                    simComponent.paused = false
                                    simComponent.updateFn = when (soundComponent.soundType) {
                                        SoundComposition.SoundSampleType.LOW -> lowBehavior
                                        SoundComposition.SoundSampleType.MEDIUM -> mediumBehavior
                                        SoundComposition.SoundSampleType.HIGH -> highBehavior
                                    }
                                }
                            }

                            gltfEntity.addComponent(InteractableComponent.create(session, mainExecutor,
                                SoundEntityMovementHandler(
                                    manipulationEntity!!,
                                    soundComponent,
                                    heightToChangeSound = 0.2f,
                                    debounceThreshold = 0.05f)))

                            gltfEntity.addComponent(InteractableComponent.create(session, mainExecutor,
                                EntityMoveInteractionHandler(
                                    manipulationEntity!!, // checkNotNull(gltfEntity.getParent()!!.getParent()),
                                    linearAcceleration = 3.0f,
                                    deadZone = 0.02f,
                                    onInputEventBubble = tapHandler,
                                    onMovementStarted = { soundComponent.play() })))
                        }
                    }
                }
            }
            /* if (isOrbiterVisible) {
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
            } */
        }
    }
}