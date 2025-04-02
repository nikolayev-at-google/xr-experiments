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
import androidx.xr.scenecore.AnchorEntity
import androidx.xr.scenecore.ContentlessEntity
import androidx.xr.scenecore.Dimensions
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.scenecore.PanelEntity
import androidx.xr.scenecore.PlaneSemantic
import androidx.xr.scenecore.PlaneType
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
                surfaceDimensionsPx = Dimensions(1500f, 500f),
                dimensions = Dimensions(2f, 7f),
                name = "headLockedPanel",
                pose = newUserForwardPose
            )
        headLockedPanel.setParent(session.activitySpace)

        val contentlessEntity = ContentlessEntity.create(session, "")
        contentlessEntity.setParent(headLockedPanel)


        lifecycleScope.launch {

            // region static01
            createGridItem(
                session = sceneCoreSession,
                parent = contentlessEntity,
                inMenuGlbFileName = "glb2/01_static.glb",
                onSceneGlbFileName = "glb2/01_animated.glb",
                translationInGrid = Vector3.Left * 0.1f,
                inMenuOnLoaded = {},
                inMenuOnHoverEnter = {
                    it.setScale(1.3f)
                    Log.d(TAG, "inMenuOnHoverEnter: $it")
                },
                inMenuOnHoverExit = {
                    it.setScale(1f)
                    Log.d(TAG, "inMenuOnHoverExit: $it")
                },
                inMenuOnClick = {
                    it.setHidden(true)
                    Log.d(TAG, "inMenuOnClick: $it")
                },
                onSceneOnLoaded = { onSceneModelEntity ->

                    Log.d(TAG, "onSceneOnLoaded: ${onSceneModelEntity.getPose()}")
                },
                onSceneOnHoverEnter = {},
                onSceneOnHoverExit = {},
                onSceneOnClick = {
                    it.startAnimation(loop = false)
                }
            )
            // endregion


            // region static02
            createGridItem(
                session = sceneCoreSession,
                parent = contentlessEntity,
                inMenuGlbFileName = "glb2/02_static.glb",
                onSceneGlbFileName = "glb2/02_static.glb",
                translationInGrid = Vector3.Right * 0.1f,
                inMenuOnLoaded = {},
                inMenuOnHoverEnter = {
                    it.setScale(1.3f)
                },
                inMenuOnHoverExit = {
                    it.setScale(1f)
                },
                inMenuOnClick = {
                    it.setHidden(true)
                },
                onSceneOnLoaded = {},
                onSceneOnHoverEnter = {},
                onSceneOnHoverExit = {},
                onSceneOnClick = {
                    it.startAnimation(loop = false)
                }
            )
            // endregion

            // region static05
            createGridItem(
                session = sceneCoreSession,
                parent = contentlessEntity,
                inMenuGlbFileName = "glb2/05_static.glb",
                onSceneGlbFileName = "glb2/05_static.glb",
                translationInGrid = Vector3.Right * 0.3f,
                inMenuOnLoaded = {},
                inMenuOnHoverEnter = {
                    it.setScale(1.3f)
                },
                inMenuOnHoverExit = {
                    it.setScale(1f)
                },
                inMenuOnClick = {
                    it.setHidden(true)
                },
                onSceneOnLoaded = {},
                onSceneOnHoverEnter = {},
                onSceneOnHoverExit = {},
                onSceneOnClick = {
                    it.startAnimation(loop = false)
                }
            )
            // endregion

            // region static08
            createGridItem(
                session = sceneCoreSession,
                parent = contentlessEntity,
                inMenuGlbFileName = "glb2/08_static.glb",
                onSceneGlbFileName = "glb2/08_static.glb",
                translationInGrid = Vector3.Left * 0.1f + Vector3.Down * 0.1f,
                inMenuOnLoaded = {},
                inMenuOnHoverEnter = {
                    it.setScale(1.3f)
                },
                inMenuOnHoverExit = {
                    it.setScale(1f)
                },
                inMenuOnClick = {
                    it.setHidden(true)
                },
                onSceneOnLoaded = {},
                onSceneOnHoverEnter = {},
                onSceneOnHoverExit = {},
                onSceneOnClick = {
                    it.startAnimation(loop = false)
                }
            )
            // endregion


            // region static10
            createGridItem(
                session = sceneCoreSession,
                parent = contentlessEntity,
                inMenuGlbFileName = "glb2/10_static.glb",
                onSceneGlbFileName = "glb2/10_static.glb",
                translationInGrid = Vector3.Right * 0.1f + Vector3.Down * 0.1f,
                inMenuOnLoaded = {},
                inMenuOnHoverEnter = {
                    it.setScale(1.3f)
                },
                inMenuOnHoverExit = {
                    it.setScale(1f)
                },
                inMenuOnClick = {
                    it.setHidden(true)
                },
                onSceneOnLoaded = {},
                onSceneOnHoverEnter = {},
                onSceneOnHoverExit = {},
                onSceneOnClick = {
                    it.startAnimation(loop = false)
                }
            )
            // endregion

            // region static11
            createGridItem(
                session = sceneCoreSession,
                parent = contentlessEntity,
                inMenuGlbFileName = "glb2/11_static.glb",
                onSceneGlbFileName = "glb2/11_static.glb",
                translationInGrid = Vector3.Right * 0.3f + Vector3.Down * 0.1f,
                inMenuOnLoaded = {},
                inMenuOnHoverEnter = {
                    it.setScale(1.3f)
                },
                inMenuOnHoverExit = {
                    it.setScale(1f)
                },
                inMenuOnClick = {
                    it.setHidden(true)
                },
                onSceneOnLoaded = {},
                onSceneOnHoverEnter = {},
                onSceneOnHoverExit = {},
                onSceneOnClick = {
                    it.startAnimation(loop = false)
                }
            )
            // endregion

            // region static16
            createGridItem(
                session = sceneCoreSession,
                parent = contentlessEntity,
                inMenuGlbFileName = "glb2/16_static.glb",
                onSceneGlbFileName = "glb2/16_static.glb",
                translationInGrid = Vector3.Left * 0.1f + Vector3.Down * 0.2f,
                inMenuOnLoaded = {},
                inMenuOnHoverEnter = {
                    it.setScale(1.3f)
                },
                inMenuOnHoverExit = {
                    it.setScale(1f)
                },
                inMenuOnClick = {
                    it.setHidden(true)
                },
                onSceneOnLoaded = {},
                onSceneOnHoverEnter = {},
                onSceneOnHoverExit = {},
                onSceneOnClick = {
                    it.startAnimation(loop = false)
                }
            )
            // endregion

            // region static18
            createGridItem(
                session = sceneCoreSession,
                parent = contentlessEntity,
                inMenuGlbFileName = "glb2/18_static.glb",
                onSceneGlbFileName = "glb2/18_static.glb",
                translationInGrid = Vector3.Right * 0.1f + Vector3.Down * 0.2f,
                inMenuOnLoaded = {},
                inMenuOnHoverEnter = {
                    it.setScale(1.3f)
                },
                inMenuOnHoverExit = {
                    it.setScale(1f)
                },
                inMenuOnClick = {
                    it.setHidden(true)
                },
                onSceneOnLoaded = {},
                onSceneOnHoverEnter = {},
                onSceneOnHoverExit = {},
                onSceneOnClick = {
                    it.startAnimation(loop = false)
                }
            )
            // endregion

            viewModel.addShapeMenuOpen.collect { isVisible ->
                contentlessEntity.setHidden(!isVisible)
            }
        }
    }

    private fun createGridItem(
        session : Session,
        parent : Entity,
        inMenuGlbFileName : String,
        onSceneGlbFileName : String,
        translationInGrid : Vector3,
        inMenuOnLoaded : (gltfModelEntity : GltfModelEntity) -> Unit,
        inMenuOnHoverEnter : (gltfModelEntity : GltfModelEntity) -> Unit,
        inMenuOnHoverExit : (gltfModelEntity : GltfModelEntity) -> Unit,
        inMenuOnClick : (gltfModelEntity : GltfModelEntity) -> Unit,
        onSceneOnLoaded : (gltfModelEntity : GltfModelEntity) -> Unit,
        onSceneOnHoverEnter : (gltfModelEntity : GltfModelEntity) -> Unit,
        onSceneOnHoverExit : (gltfModelEntity : GltfModelEntity) -> Unit,
        onSceneOnClick : (gltfModelEntity : GltfModelEntity) -> Unit
    ) {
        lifecycleScope.launch {
            val inMenuModel = GltfModel.create(session, inMenuGlbFileName).await()
            val inMenuModelEntity = GltfModelEntity.create(session, inMenuModel)
            inMenuModelEntity.setParent(parent)
            inMenuModelEntity.setPose(
                Pose(
                    translation = translationInGrid,
                    rotation = inMenuModelEntity.getPose().rotation
                )
            )
            inMenuOnLoaded(inMenuModelEntity)
            // Setting an Interactable Component
            val interactable = InteractableComponent.create(session, mainExecutor) { ie ->
                when (ie.action) {
                    InputEvent.ACTION_HOVER_ENTER -> {
                        inMenuOnHoverEnter(inMenuModelEntity)
                    }
                    InputEvent.ACTION_HOVER_EXIT -> {
                        inMenuOnHoverExit(inMenuModelEntity)
                    }
                    InputEvent.ACTION_DOWN -> {
                        inMenuOnClick(inMenuModelEntity)
                        lifecycleScope.launch {

//                            val anchor =
//                                AnchorEntity.create(
//                                    session = session,
//                                    bounds = Dimensions(1f, 1f),
//                                    PlaneType.ANY,
//                                    PlaneSemantic.ANY
//                                )

                            val onSceneModel = GltfModel.create(session, onSceneGlbFileName).await()
                            val onSceneModelEntity = GltfModelEntity.create(session, onSceneModel)
//                            val transformedPose = session.activitySpace.transformPoseTo(
//                                    onSceneModelEntity.getPose(),
//                                    session.activitySpace,
//                                )
//                            val newPosition = transformedPose.translation + transformedPose.backward
//                            onSceneModelEntity.setPose(Pose(
//                                translation = newPosition,
//                                rotation = inMenuModelEntity.getPose().rotation
//                            ))
                            onSceneModelEntity.setPose(Pose.Identity.translate(Vector3.Up))
//                            onSceneModelEntity.setParent(session.activitySpace)

//                            anchor.addChild(onSceneModelEntity)

                            onSceneOnLoaded(onSceneModelEntity)
                            // Setting an Interactable Component
                            onSceneModelEntity.addComponent(
                                InteractableComponent.create(session, mainExecutor) { ie ->
                                    when (ie.action) {
                                        InputEvent.ACTION_DOWN -> {
                                            onSceneOnClick(onSceneModelEntity)
                                        }
                                        InputEvent.ACTION_HOVER_ENTER -> {
                                            onSceneOnHoverEnter(onSceneModelEntity)
                                        }
                                        InputEvent.ACTION_HOVER_EXIT -> {
                                            onSceneOnHoverExit(onSceneModelEntity)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            inMenuModelEntity.addComponent(interactable)
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