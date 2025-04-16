package com.google.experiment.soundexplorer.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
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
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.runtime.math.Pose
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue


@AndroidEntryPoint
class Main14Activity : ComponentActivity() {

    @Inject
    lateinit var modelRepository : GlbModelRepository
    private val viewModel : MainViewModel by viewModels()

    private val sceneCoreSession by lazy { Session.create(this) }

//    private var userForward: Pose by mutableStateOf(Pose(Vector3(0.0f, -0.8f, -1.5f)))
    private var userForward: Pose by mutableStateOf(Pose(Vector3(0.0f, 0.0f, -1.5f)))
    private lateinit var dialogPanel : PanelEntity



    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContent {
//            val session = LocalSession.current
//            if (session == null) {
//                return@setContent
//            }
//            Subspace {
//                MainScreen(modelRepository)
//            }
//        }

        sceneCoreSession.mainPanelEntity.setHidden(true)
//
        createHeadLockedPanelUi()

        createModels()
//        lifecycleScope.launch {
//            viewModel.isModelsVisible.collect {
//                Log.d("TAG", "onCreate: collect collect collect collect")
//            }
//        }
//        lifecycleScope.launch {
//            viewModel.isDialogVisible.collect {
//                Log.d("TAG", "onCreate: collect collect collect collect")
//                dialogPanel.setHidden(it)
//            }
//        }
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
            Toolbar(
                {viewModel.showDialog()},
                {viewModel.showModels()},
                {},
                modelRepository = modelRepository
            )
        }
        val headLockedPanel = createPanelUi(
            session = sceneCoreSession,
            view = headLockedPanelView,
            surfaceDimensionsPx = Dimensions(340f, 120f),
            dimensions = Dimensions(2f, 7f),
            panelName = "headLockedPanel",
            pose = userForward
        )
        headLockedPanelView.postOnAnimation {
            updateHeadLockedPose(headLockedPanelView, headLockedPanel)
        }

//        createHeadLockedDialogUi(headLockedPanel)
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
                        })
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