package com.google.experiment.soundexplorer.cur

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.ContentlessEntity
import androidx.xr.scenecore.Dimensions
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.scenecore.PanelEntity
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.sound.SoundComposition
import com.google.experiment.soundexplorer.ui.SoundObjectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var modelRepository : GlbModelRepository
    private val viewModel : MainViewModel by viewModels()
    private val sceneCoreSession by lazy { Session.create(this) }
    private var soundComponents: Array<SoundComposition.SoundCompositionComponent>? = null
    private var soundObjects: Array<SoundObjectComponent>? = null
    private val activeMenuObjects = mutableListOf<GltfModelEntity>()
    private val inactiveMenuObjects = mutableListOf<GltfModelEntity>()
    private var userDialogForward: Pose by mutableStateOf(Pose(Vector3(0.0f, 1.0f, -1.5f)))
//    private var userForward: Pose by mutableStateOf(Pose(Vector3(0.0f, -0.8f, -1.5f))) // for emulator
    private var userForward: Pose by mutableStateOf(Pose(Vector3(0.0f, -0.1f, -2.0f)))// for device

    private var modelsLoaded: Boolean by mutableStateOf(false)
    private var soundObjectsReady: Boolean by mutableStateOf(false)

    fun createSoundObjects(
        glbModels : Array<GlbModel>
    ): Array<SoundObjectComponent> {
        val soundObjs = Array<SoundObjectComponent?>(checkNotNull(soundComponents).size) { null }
        for (i in soundObjs.indices) {
            soundObjs[i] = SoundObjectComponent.createSoundObject(
                sceneCoreSession,
                sceneCoreSession.activitySpace,
                modelRepository,
                glbModels[i],
                checkNotNull(soundComponents)[i],
                mainExecutor,
                lifecycleScope)
        }
        return soundObjs.map({o -> checkNotNull(o)}).toTypedArray()
    }

    suspend fun initializeSoundsAndCreateObjects() {
        if (this.soundObjectsReady) {
            return
        }

        // wait for sounds to load
        this.viewModel.soundPool.soundsLoaded.first { x -> x }

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

        if (this.soundObjects == null) {
            this.soundObjects = createSoundObjects(GlbModel.allGlbAnimatedModels.toTypedArray())
        }

        this.soundObjectsReady = true
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sceneCoreSession.mainPanelEntity.setHidden(true)
        viewModel.initializeSoundComposition(sceneCoreSession)

        lifecycleScope.launch { initializeSoundsAndCreateObjects() }
        lifecycleScope.launch { loadModels() }

        createHeadLockedPanelUi()
        createModels(GlbModel.allGlbAnimatedModels, GlbModel.allGlbInactiveModels)

        createHeadLockedDialogUi()
    }

    private fun createPanelView(
        activity: Activity,
        contentScreen: @Composable () -> Unit
    ) : View {
        return ComposeView(activity).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                contentScreen()
            }
            setViewTreeLifecycleOwner(activity as LifecycleOwner)
            setViewTreeViewModelStoreOwner(activity as ViewModelStoreOwner)
            setViewTreeSavedStateRegistryOwner(activity as SavedStateRegistryOwner)
        }
    }

    private fun createPanelUi(
        session: Session,
        view: View,
        surfaceDimensionsPx : Dimensions,
        dimensions : Dimensions,
        panelName : String,
        pose: Pose
    ) : PanelEntity {
        return PanelEntity.create(
            session = session,
            view = view,
            surfaceDimensionsPx = surfaceDimensionsPx,// ,
            dimensions = dimensions,
            name = panelName,
            pose = pose
        ).apply {
            setParent(session.activitySpace)
        }
    }

    private fun createHeadLockedPanelUi() {
        val headLockedPanelView = createPanelView(this) {
            if (soundObjectsReady && modelsLoaded) {
                ToolbarContent()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            color = Color(0xFF2D2E31),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading...",
                        color = Color.White
                    )
                }
            }
        }
        val headLockedPanel = createPanelUi(
            session = sceneCoreSession,
            view = headLockedPanelView,
            surfaceDimensionsPx = Dimensions(440f, 170f),
            dimensions = Dimensions(2f, 7f),
            panelName = "headLockedPanel",
            pose = userForward
        )
        headLockedPanelView.postOnAnimation {
            updateHeadLockedPose(headLockedPanelView, headLockedPanel)
        }
    }

    private fun updateHeadLockedPose(view: View, panelEntity: PanelEntity) {
        sceneCoreSession.spatialUser.head?.let { projectionSource ->
            projectionSource.transformPoseTo(userForward, sceneCoreSession.activitySpace).let {
                panelEntity.setPose(it)
                viewModel.setToolbarPose(it)
            }
        }
        view.postOnAnimation { updateHeadLockedPose(view, panelEntity) }
    }

    // Loading models is slow. We need to load all of them before allowing the user to interact with the app.
    private suspend fun loadModels() {
        val loadActiveModelsJobs = GlbModel.allGlbAnimatedModels.map {
            m -> lifecycleScope.launch { modelRepository.getOrLoadModel(m) }
        }
        val loadInactiveModelsJobs = GlbModel.allGlbInactiveModels.map {
            m -> lifecycleScope.launch { modelRepository.getOrLoadModel(m) }
        }
        loadActiveModelsJobs.joinAll()
        loadInactiveModelsJobs.joinAll()
        modelsLoaded = true
    }

    private fun createModels(
        activeModels: List<GlbModel>,
        inactiveModels: List<GlbModel>
    ) {
        if (activeModels.size != inactiveModels.size) {
            throw IllegalArgumentException("The number of active and inactive sound obj models with not equal!")
        }

        lifecycleScope.launch {
            val menu = ContentlessEntity.create(sceneCoreSession, "menu")

            var delta = 0.6f
            for (i in 0..<(activeModels.size)) {
                val shapeActiveModel = modelRepository.getOrLoadModel(activeModels[i]).getOrNull() as GltfModel?
                val shapeInactiveModel = modelRepository.getOrLoadModel(inactiveModels[i]).getOrNull() as GltfModel?

                val shapeActiveEntity = GltfModelEntity.create(sceneCoreSession, checkNotNull(shapeActiveModel)).apply {
                    setParent(menu)
                    setPose(Pose(Vector3(delta, 0.15f, 0.0f), Quaternion.Identity))
                }
                val shapeInactiveEntity = GltfModelEntity.create(sceneCoreSession, checkNotNull(shapeInactiveModel)).apply {
                    setParent(menu)
                    setPose(Pose(Vector3(delta, 0.15f, 0.0f), Quaternion.Identity))
                }

                shapeActiveEntity.addComponent(
                    InteractableComponent.create(sceneCoreSession, mainExecutor) { ie ->
                        if (ie.action == InputEvent.ACTION_DOWN) {
                            shapeInactiveEntity.setHidden(false)
                            // shapeInactiveEntity.startAnimation(loop = false)
                            shapeActiveEntity.setHidden(true)

                            val initialLocation = checkNotNull(sceneCoreSession.spatialUser.head).transformPoseTo(
                                Pose(Vector3.Forward * 1.0f, Quaternion.Identity),
                                sceneCoreSession.activitySpace)

                            val soundObject = checkNotNull(soundObjects)[i]
                            soundObject.setPose(initialLocation)
                            soundObject.hidden = false
                            soundObject.soundComponent.play()
                            viewModel.updateSoundObjectsVisibility(
                                soundObjects?.all { so -> so.hidden } ?: false
                            )
                        } else if (ie.action == InputEvent.ACTION_HOVER_ENTER) {
                            shapeActiveEntity.setScale(1.4f)
                        } else if (ie.action == InputEvent.ACTION_HOVER_EXIT) {
                            shapeActiveEntity.setScale(1.0f)
                        }
                    })

                shapeInactiveEntity.addComponent(
                    InteractableComponent.create(sceneCoreSession, mainExecutor) { ie ->
                        if (ie.action == InputEvent.ACTION_DOWN) {
                            shapeActiveEntity.setHidden(false)
                            // gltfActiveModelEntity.startAnimation(loop = false)
                            shapeInactiveEntity.setHidden(true)

                            val soundObject = checkNotNull(soundObjects)[i]
                            soundObject.soundComponent.stop()
                            soundObject.hidden = true
                            viewModel.updateSoundObjectsVisibility(
                                soundObjects?.all { so -> so.hidden } ?: false
                            )
                        } else if (ie.action == InputEvent.ACTION_HOVER_ENTER) {
                            shapeInactiveEntity.setScale(1.4f)
                        } else if (ie.action == InputEvent.ACTION_HOVER_EXIT) {
                            shapeInactiveEntity.setScale(1.0f)
                        }
                    })
                shapeInactiveEntity.setHidden(true)

                activeMenuObjects.add(shapeActiveEntity)
                inactiveMenuObjects.add(shapeInactiveEntity)
                delta -= 0.15f
            }

            launch {
                viewModel.toolbarPose.collect { toolbarPose ->
                    menu.setPose(toolbarPose)
                }
            }

            launch {
                viewModel.isModelsVisible.collect {
                    menu.setHidden(!it)
                }
            }
        }
    }

    private fun createHeadLockedDialogUi() {
        val headLockedDialogPanelView = createPanelView(this) {
            RestartDialogContent()
        }
        val headLockedDialogPanel = createPanelUi(
            session = sceneCoreSession,
            view = headLockedDialogPanelView,
            surfaceDimensionsPx = Dimensions(800f, 490f),
            dimensions = Dimensions(10f, 10f),
            panelName = "headLockedDialogPanel",
            pose = userDialogForward
        )
        headLockedDialogPanel.setHidden(true)
        lifecycleScope.launch {
            viewModel.toolbarPose.collect { toolbarPose ->
                headLockedDialogPanel.setPose(
                    toolbarPose.translate(toolbarPose.up * 0.2f)
                )
            }
        }
        lifecycleScope.launch {
            viewModel.isDialogHidden.collect { hidden ->
                headLockedDialogPanel.setHidden(hidden)
            }
        }
        lifecycleScope.launch {
            viewModel.deleteAll.collect { event ->
                if (event.value) {

                    soundObjects?.forEach {
                        it.soundComponent.stop()
                        it.hidden = true
                    }
                    viewModel.updateSoundObjectsVisibility(true)
                    activeMenuObjects.forEach { it.setHidden(false) }
                    inactiveMenuObjects.forEach { it.setHidden(true) }

                    viewModel.showDialog() // switch visibility
                }
            }
        }
    }
}