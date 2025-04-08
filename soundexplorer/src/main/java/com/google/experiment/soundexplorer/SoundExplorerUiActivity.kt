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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.subspace.layout.height
import com.google.experiment.soundexplorer.core.GlbModel
import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.vm.SoundExplorerUiViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


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

        setContent { Subspace { MainMenu() } }
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
        SpatialRow(SubspaceModifier.testTag("ShapeGrid")) {
            val columnPanelModifier = SubspaceModifier.size(150.dp).padding(20.dp)
            SpatialColumn {
                AppPanel(
                    modifier = columnPanelModifier.testTag("02_static"),
                    glbFileName = "glb2/02_static.glb",
                    glbModel = GlbModel.GlbModel01Anim
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("05_static"),
                    glbFileName = "glb2/05_static.glb",
                    glbModel = GlbModel.GlbModel02Anim
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("08_static"),
                    glbFileName = "glb2/08_static.glb",
                    glbModel = GlbModel.GlbModel03Anim
                )
            }
            SpatialColumn {
                AppPanel(
                    modifier = columnPanelModifier.testTag("10_static"),
                    glbFileName = "glb2/10_static.glb",
                    glbModel = GlbModel.GlbModel04Anim
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("11_static"),
                    glbFileName = "glb2/11_static.glb",
                    glbModel = GlbModel.GlbModel05Anim
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("16_static"),
                    glbFileName = "glb2/16_static.glb",
                    glbModel = GlbModel.GlbModel06Anim
                )
            }
            SpatialColumn {
                AppPanel(
                    modifier = columnPanelModifier.testTag("18_static"),
                    glbFileName = "glb2/18_static.glb",
                    glbModel = GlbModel.GlbModel07Anim
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("01_animated"),
                    glbFileName = "glb2/01_animated.glb",
                    glbModel = GlbModel.GlbModel08Anim
                )
                AppPanel(
                    modifier = columnPanelModifier.testTag("01_animated"),
                    glbFileName = "glb2/01_animated.glb",
                    glbModel = GlbModel.GlbModel09Anim
                )
            }
        }
    }

    @SubspaceComposable
    @Composable
    fun AppPanel(
        modifier: SubspaceModifier = SubspaceModifier,
        glbFileName: String = "glb2/01_animated.glb",
        glbModel : GlbModel = GlbModel.GlbModel01
    ) {
        SpatialPanel(modifier = modifier.movable()) {
            PanelContent(glbFileName = glbFileName, glbModel = glbModel)
        }
    }

    @UiComposable
    @Composable
    fun PanelContent(
        glbFileName: String,
        glbModel : GlbModel = GlbModel.GlbModel01
    ) {

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
                val gltfModelEntity = GltfModelEntity.create(session, it).apply {
                    addComponent(
                        InteractableComponent.create(session, mainExecutor) { ie ->
                        when (ie.action) {
                            InputEvent.ACTION_DOWN -> {
                                startAnimation(loop = false)
                            }
                            InputEvent.ACTION_HOVER_ENTER -> {
                                isOrbiterVisible = true
                                setScale(1.3f)
                            }
                            InputEvent.ACTION_HOVER_EXIT -> {
                                isOrbiterVisible = false
                                setScale(1f)
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
//            TODO: load model from cach here!!!!
//            shape = GltfModel.create(session, glbFileName).await()
            Log.d(TAG, "executionTime[${glbModel.assetName}]: ")
            // calculate time to execute commad to load model
            val startTime = System.currentTimeMillis()
            shape = modelRepository.getOrLoadModel(glbModel).getOrNull() as GltfModel?
            val endTime = System.currentTimeMillis()
            val executionTime = endTime - startTime

            Log.d(TAG, "executionTime[${glbModel.assetName}]: $executionTime")
        }

        Box(
//            modifier = Modifier.background(Color.LightGray).fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (gltfEntity != null) {
                Subspace {
                    Volume {
                        gltfEntity.setParent(it)
                    }
                }
            }
            if (isOrbiterVisible) {
                Orbiter(position = OrbiterEdge.Bottom, offset = 64.dp) {
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