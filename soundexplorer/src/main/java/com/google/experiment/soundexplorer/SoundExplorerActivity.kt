@file:Suppress("SpellCheckingInspection")

package com.google.experiment.soundexplorer

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.concurrent.futures.await
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.xr.compose.spatial.Subspace
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Dimensions
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.scenecore.PanelEntity
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.ui.ActionScreen
import com.google.experiment.soundexplorer.vm.SoundExplorerViewModel
import kotlinx.coroutines.launch
import kotlin.getValue


class SoundExplorerActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel : SoundExplorerViewModel by viewModels()
    private val sceneCoreSession by lazy { Session.create(this) }
    private var userForward: Pose by mutableStateOf(Pose(Vector3(0f, 0.00f, -1.0f)))
    private lateinit var headLockedPanelView: View
    private lateinit var headLockedPanel: PanelEntity


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started.")

        sceneCoreSession.mainPanelEntity.setHidden(true)

        createHeadLockedUi(this, sceneCoreSession)
    }

    private fun createHeadLockedUi(activity: Activity, session: Session) {
        headLockedPanelView = ComposeView(activity).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                ActionScreen()
            }
        }
        headLockedPanelView.postOnAnimation(this::updateHeadLockedPose)
        headLockedPanelView.setViewTreeLifecycleOwner(activity as LifecycleOwner)
        headLockedPanelView.setViewTreeViewModelStoreOwner(activity as ViewModelStoreOwner)
        headLockedPanelView.setViewTreeSavedStateRegistryOwner(activity as SavedStateRegistryOwner)

        val newUserForwardPose = Pose(
            translation = userForward.translation + viewModel.slidersValues.value.unaryMinus(),
            rotation = userForward.rotation
        )
        headLockedPanel =
            PanelEntity.create(
                session = session,
                view = headLockedPanelView,
                surfaceDimensionsPx = Dimensions(1500f, 1500f),
                dimensions = Dimensions(2f, 7f),
                name = "headLockedPanel",
                pose = newUserForwardPose
            )
        headLockedPanel.setParent(session.activitySpace)


        lifecycleScope.launch {

            // region static01
            val static01 = GltfModel.create(sceneCoreSession, "glb2/01_static.glb").await()
            // create the gltf entity using the gltf file from the previous snippet
            val static01Entity = GltfModelEntity.create(sceneCoreSession, static01)
            static01Entity.setParent(headLockedPanel)
            static01Entity.setPose(Pose(
                translation = Vector3.Left * 0.1f,
                rotation = static01Entity.getPose().rotation
            ))
            // Setting an Interactable Component
            val interactable = InteractableComponent.create(session, mainExecutor) { ie ->
                when (ie.action) {
                    InputEvent.ACTION_HOVER_ENTER -> {
                        static01Entity.setScale(1.3f)
                    }
                    InputEvent.ACTION_HOVER_EXIT -> {
                        static01Entity.setScale(1f)
                    }
                    InputEvent.ACTION_DOWN -> {

                        launch {
                            // region static01
                            val static01Anim = GltfModel.create(sceneCoreSession, "glb2/01_animated.glb").await()
                            // create the gltf entity using the gltf file from the previous snippet
                            val static01AnimEntity = GltfModelEntity.create(sceneCoreSession, static01Anim)
                            static01Entity.setParent(session.activitySpace)
                            val transformedPose =
                                sceneCoreSession.activitySpace.transformPoseTo(
                                    static01Entity.getPose(),
                                    sceneCoreSession.activitySpace,
                                )
                            val newPosition = transformedPose.translation + transformedPose.backward
                            static01AnimEntity.setPose(Pose(
                                translation = newPosition,
                                rotation = static01Entity.getPose().rotation
                            ))
                            // Setting an Interactable Component
                            val interactable01Anim = InteractableComponent.create(session, mainExecutor) { ie ->
                                when (ie.action) {
                                    InputEvent.ACTION_DOWN -> {
                                        static01AnimEntity.startAnimation(loop = false)
                                    }
                                }
                            }
                            static01AnimEntity.addComponent(interactable01Anim)
                        }

                        static01Entity.setHidden(true)
                    }
                }
            }
            static01Entity.addComponent(interactable)
            // endregion


            // region static02
            val static02 = GltfModel.create(sceneCoreSession, "glb2/02_static.glb").await()
            // create the gltf entity using the gltf file from the previous snippet
            val static02Entity = GltfModelEntity.create(sceneCoreSession, static02)

            static02Entity.setParent(headLockedPanel)
            static02Entity.setPose(Pose(
                translation = Vector3.Right * 0.1f,
                rotation = static02Entity.getPose().rotation
            ))
            // Setting an Interactable Component
            val interactable02 = InteractableComponent.create(session, mainExecutor) { ie ->
                when (ie.action) {
                    InputEvent.ACTION_HOVER_ENTER -> {
                        static02Entity.setScale(1.3f)
                    }
                    InputEvent.ACTION_HOVER_EXIT -> {
                        static02Entity.setScale(1f)
                    }
                }
            }
            static02Entity.addComponent(interactable02)
            // endregion

            // region static05
            val static05 = GltfModel.create(sceneCoreSession, "glb2/05_static.glb").await()
            // create the gltf entity using the gltf file from the previous snippet
            val static05Entity = GltfModelEntity.create(sceneCoreSession, static05)

            static05Entity.setParent(headLockedPanel)
            static05Entity.setPose(Pose(
                translation = Vector3.Right * 0.3f,
                rotation = static05Entity.getPose().rotation
            ))
            // Setting an Interactable Component
            val interactable05 = InteractableComponent.create(session, mainExecutor) { ie ->
                when (ie.action) {
                    InputEvent.ACTION_HOVER_ENTER -> {
                        static05Entity.setScale(1.3f)
                    }
                    InputEvent.ACTION_HOVER_EXIT -> {
                        static05Entity.setScale(1f)
                    }
                }
            }
            static05Entity.addComponent(interactable05)
            // endregion

            // region static08
            val static08 = GltfModel.create(sceneCoreSession, "glb2/08_static.glb").await()
            // create the gltf entity using the gltf file from the previous snippet
            val static08Entity = GltfModelEntity.create(sceneCoreSession, static08)

            static08Entity.setParent(headLockedPanel)
            static08Entity.setPose(Pose(
                translation = Vector3.Left * 0.1f + Vector3.Down * 0.1f,
                rotation = static08Entity.getPose().rotation
            ))
            // Setting an Interactable Component
            val interactable08 = InteractableComponent.create(session, mainExecutor) { ie ->
                when (ie.action) {
                    InputEvent.ACTION_HOVER_ENTER -> {
                        static08Entity.setScale(1.3f)
                    }
                    InputEvent.ACTION_HOVER_EXIT -> {
                        static08Entity.setScale(1f)
                    }
                }
            }
            static08Entity.addComponent(interactable08)
            // endregion


            // region static10
            val static10 = GltfModel.create(sceneCoreSession, "glb2/10_static.glb").await()
            // create the gltf entity using the gltf file from the previous snippet
            val static10Entity = GltfModelEntity.create(sceneCoreSession, static10)

            static10Entity.setParent(headLockedPanel)
            static10Entity.setPose(Pose(
                translation = Vector3.Right * 0.1f + Vector3.Down * 0.1f,
                rotation = static10Entity.getPose().rotation
            ))
            // Setting an Interactable Component
            val interactable10 = InteractableComponent.create(session, mainExecutor) { ie ->
                when (ie.action) {
                    InputEvent.ACTION_HOVER_ENTER -> {
                        static10Entity.setScale(1.3f)
                    }
                    InputEvent.ACTION_HOVER_EXIT -> {
                        static10Entity.setScale(1f)
                    }
                }
            }
            static10Entity.addComponent(interactable10)
            // endregion

            // region static11
            val static11 = GltfModel.create(sceneCoreSession, "glb2/11_static.glb").await()
            // create the gltf entity using the gltf file from the previous snippet
            val static11Entity = GltfModelEntity.create(sceneCoreSession, static11)

            static11Entity.setParent(headLockedPanel)
            static11Entity.setPose(Pose(
                translation = Vector3.Right * 0.3f + Vector3.Down * 0.1f,
                rotation = static11Entity.getPose().rotation
            ))
            // Setting an Interactable Component
            val interactable11 = InteractableComponent.create(session, mainExecutor) { ie ->
                when (ie.action) {
                    InputEvent.ACTION_HOVER_ENTER -> {
                        static11Entity.setScale(1.3f)
                    }
                    InputEvent.ACTION_HOVER_EXIT -> {
                        static11Entity.setScale(1f)
                    }
                }
            }
            static11Entity.addComponent(interactable11)
            // endregion

            // region static16
            val static16 = GltfModel.create(sceneCoreSession, "glb2/16_static.glb").await()
            // create the gltf entity using the gltf file from the previous snippet
            val static16Entity = GltfModelEntity.create(sceneCoreSession, static16)

            static16Entity.setParent(headLockedPanel)
            static16Entity.setPose(Pose(
                translation = Vector3.Left * 0.1f + Vector3.Down * 0.2f,
                rotation = static16Entity.getPose().rotation
            ))
            // Setting an Interactable Component
            val interactable16 = InteractableComponent.create(session, mainExecutor) { ie ->
                when (ie.action) {
                    InputEvent.ACTION_HOVER_ENTER -> {
                        static16Entity.setScale(1.3f)
                    }
                    InputEvent.ACTION_HOVER_EXIT -> {
                        static16Entity.setScale(1f)
                    }
                }
            }
            static16Entity.addComponent(interactable16)
            // endregion

            // region static18
            val static18 = GltfModel.create(sceneCoreSession, "glb2/18_static.glb").await()
            // create the gltf entity using the gltf file from the previous snippet
            val static18Entity = GltfModelEntity.create(sceneCoreSession, static18)

            static18Entity.setParent(headLockedPanel)
            static18Entity.setPose(Pose(
                translation = Vector3.Right * 0.1f + Vector3.Down * 0.2f,
                rotation = static18Entity.getPose().rotation
            ))
            // Setting an Interactable Component
            val interactable18 = InteractableComponent.create(session, mainExecutor) { ie ->
                when (ie.action) {
                    InputEvent.ACTION_HOVER_ENTER -> {
                        static18Entity.setScale(1.3f)
                    }
                    InputEvent.ACTION_HOVER_EXIT -> {
                        static18Entity.setScale(1f)
                    }
                }
            }
            static18Entity.addComponent(interactable18)
            // endregion

            viewModel.addShapeMenuOpen.collect { isVisible ->
                static01Entity.setHidden(!isVisible)
                static02Entity.setHidden(!isVisible)
                static05Entity.setHidden(!isVisible)
                static08Entity.setHidden(!isVisible)
                static10Entity.setHidden(!isVisible)
                static11Entity.setHidden(!isVisible)
                static16Entity.setHidden(!isVisible)
                static18Entity.setHidden(!isVisible)
            }
        }
    }

    private fun updateHeadLockedPose() {
        sceneCoreSession.spatialUser.head?.let { projectionSource ->

            val newUserForwardPose = Pose(
                translation = userForward.translation + viewModel.slidersValues.value.unaryMinus(),
                rotation = userForward.rotation
            )

            projectionSource.transformPoseTo(newUserForwardPose, sceneCoreSession.activitySpace).let {
                this.headLockedPanel.setPose(it)
            }
        }
        headLockedPanelView.postOnAnimation(this::updateHeadLockedPose)
    }

}