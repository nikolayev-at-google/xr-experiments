package com.google.experiment.soundexplorer.cur

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Subspace
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
import com.google.experiment.soundexplorer.ui.Toolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
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
    private var userForward: Pose by mutableStateOf(Pose(Vector3(0.0f, -0.8f, -1.5f)))
//    private var userForward: Pose by mutableStateOf(Pose(Vector3(0.0f, 0.0f, -1.5f)))

    fun createSoundObjects(
        session : Session,
        modelRepository : GlbModelRepository,
        glbModels : Array<GlbModel>
    ): Array<SoundObjectComponent> {
        var soundObjs = Array<SoundObjectComponent?>(checkNotNull(soundComponents).size) { null }
        for (i in soundObjs.indices) {
            soundObjs[i] = SoundObjectComponent.createSoundObject(
                session,
                session.activitySpace,
                modelRepository,
                glbModels[i],
                checkNotNull(soundComponents)[i],
                mainExecutor,
                lifecycleScope)

            checkNotNull(soundObjs[i]).setHidden(true)
        }
        return soundObjs.map({o -> checkNotNull(o)}).toTypedArray()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sceneCoreSession.mainPanelEntity.setHidden(true)
        viewModel.initializeSoundComposition(sceneCoreSession)

//        setContent {
//            val session = LocalSession.current
//            if (session == null) {
//                return@setContent
//            }
//
//            this.viewModel.initializeSoundComposition(session)
//
//            val soundsLoaded = this.viewModel.soundPool.soundsLoaded.collectAsState()
//            if (!soundsLoaded.value) { // do something while sounds are loading?
//                return@setContent
//            }
//
//            if (soundComponents == null) {
//                soundComponents = arrayOf(
//                    viewModel.soundComposition.addComponent(
//                        viewModel.soundPool.inst01lowId, viewModel.soundPool.inst01midId, viewModel.soundPool.inst01highId),
//                    viewModel.soundComposition.addComponent(
//                        viewModel.soundPool.inst02lowId, viewModel.soundPool.inst02midId, viewModel.soundPool.inst02highId),
//                    viewModel.soundComposition.addComponent(
//                        viewModel.soundPool.inst03lowId, viewModel.soundPool.inst03midId, viewModel.soundPool.inst03highId),
//                    viewModel.soundComposition.addComponent(
//                        viewModel.soundPool.inst04lowId, viewModel.soundPool.inst04midId, viewModel.soundPool.inst04highId),
//                    viewModel.soundComposition.addComponent(
//                        viewModel.soundPool.inst05lowId, viewModel.soundPool.inst05midId, viewModel.soundPool.inst05highId),
//                    viewModel.soundComposition.addComponent(
//                        viewModel.soundPool.inst06lowId, viewModel.soundPool.inst06midId, viewModel.soundPool.inst06highId),
//                    viewModel.soundComposition.addComponent(
//                        viewModel.soundPool.inst07lowId, viewModel.soundPool.inst07midId, viewModel.soundPool.inst07highId),
//                    viewModel.soundComposition.addComponent(
//                        viewModel.soundPool.inst08lowId, viewModel.soundPool.inst08midId, viewModel.soundPool.inst08highId),
//                    viewModel.soundComposition.addComponent(
//                        viewModel.soundPool.inst09lowId, viewModel.soundPool.inst09midId, viewModel.soundPool.inst09highId))
//            }
//
//            if (this.soundObjects == null) {
//                this.soundObjects = createSoundObjects(
//                    session,
//                    modelRepository,
//                    GlbModel.allGlbAnimatedModels.toTypedArray()
//                )
//            }
//
//            Subspace {
//                MainScreen(modelRepository, checkNotNull(soundObjects))
//            }
//        }

        createHeadLockedPanelUi()
        createModels()
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
            ToolbarContent()
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

    private fun createModels() {
        lifecycleScope.launch {

            val menu = ContentlessEntity.create(sceneCoreSession, "menu")

            var delta = 0.6f
            var i = 0
            GlbModel.allGlbStaticModels.forEach {
                val shape = modelRepository.getOrLoadModel(it).getOrNull() as GltfModel?
                GltfModelEntity.create(sceneCoreSession, shape!!).apply {
                    setParent(menu)
                    setPose(
                        Pose.Identity
                            .translate(Pose.Identity.up * 0.15f)
                            .translate(Pose.Identity.left * delta)
                    )
                    delta -= 0.15f

                    addComponent(
                        InteractableComponent.create(sceneCoreSession, mainExecutor) { ie ->
                            when (ie.action) {
                                InputEvent.ACTION_HOVER_ENTER -> {
                                    setScale(1.3f)
                                }
                                InputEvent.ACTION_HOVER_EXIT -> {
                                    setScale(1f)
                                }
                            }
                        }
                    )
                    i++
                }
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
}