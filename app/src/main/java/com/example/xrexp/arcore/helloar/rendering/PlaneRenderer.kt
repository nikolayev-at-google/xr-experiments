package com.example.xrexp.arcore.helloar.rendering

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.xr.arcore.Plane
import androidx.xr.arcore.TrackingState
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector2
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Dimensions
import androidx.xr.scenecore.PanelEntity
import androidx.xr.scenecore.PixelDimensions
import androidx.xr.scenecore.Session as JxrCoreSession
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Class that keeps track of planes rendered as GLTF models in a SceneCore session. */
internal class PlaneRenderer(
    val session: Session,
    val renderSession: JxrCoreSession,
    val coroutineScope: CoroutineScope,
) {

    private val _renderedPlanes: MutableStateFlow<List<PlaneModel>> =
        MutableStateFlow(mutableListOf<PlaneModel>())
    internal val renderedPlanes: StateFlow<Collection<PlaneModel>> = _renderedPlanes.asStateFlow()

    private lateinit var updateJob: CompletableJob

    internal fun startRendering() {
        updateJob =
            SupervisorJob(
                coroutineScope.launch { Plane.subscribe(session).collect { updatePlaneModels(it) } }
            )
    }

    internal fun stopRendering() {
        updateJob.complete()
        _renderedPlanes.value = emptyList<PlaneModel>()
    }

    private fun updatePlaneModels(planes: Collection<Plane>) {
        val planesToRender = _renderedPlanes.value.toMutableList()
        // Create renderers for new planes.
        for (plane in planes) {
            if (_renderedPlanes.value.none { it.id == plane.hashCode() }) {
                addPlaneModel(plane, planesToRender)
            }
        }
        // Stop rendering dropped planes.
        for (renderedPlane in _renderedPlanes.value) {
            if (planes.none { it.hashCode() == renderedPlane.id }) {
                removePlaneModel(renderedPlane, planesToRender)
            }
        }
        // Emit to notify collectors that collection has been updated.
        _renderedPlanes.value = planesToRender
    }

    @SuppressLint("RestrictedApi")
    private fun addPlaneModel(plane: Plane, planesToRender: MutableList<PlaneModel>) {
        val view = createPanelDebugViewUsingCompose(plane, renderSession.activity)
        val entity = createPlanePanelEntity(plane, view)
        // The counter starts at max to trigger the resize on the first update loop since emulators
        // only
        // update their static planes once.
        var counter = PANEL_RESIZE_UPDATE_COUNT
        // Make the render job a child of the update job so it completes when the parent completes.
        val renderJob =
            coroutineScope.launch(updateJob) {
                plane.state.collect { state ->
                    if (state.trackingState == TrackingState.Tracking) {
                        if (state.label == Plane.Label.Unknown) {
                            entity.setHidden(true)
                        } else {
                            entity.setHidden(false)
                            counter++
                            entity.setPose(
                                renderSession.perceptionSpace
                                    .transformPoseTo(state.centerPose, renderSession.activitySpace)
                                    // Planes are X-Y while Panels are X-Z, so we need to rotate the
                                    // X-axis by -90
                                    // degrees to align them.
                                    .compose(PANEL_TO_PLANE_ROTATION)
                            )

                            updateViewText(view, plane, state)
                            if (counter > PANEL_RESIZE_UPDATE_COUNT) {
                                val panelExtentsInPixels = convertMetersToPixels(state.extents)
                                entity.setPixelDimensions(
                                    PixelDimensions(
                                        width = panelExtentsInPixels.x.toInt(),
                                        height = panelExtentsInPixels.y.toInt(),
                                    )
                                )
                                counter = 0
                            }
                        }
                    } else if (state.trackingState == TrackingState.Stopped) {
                        entity.setHidden(true)
                    }
                }
            }

        planesToRender.add(PlaneModel(plane.hashCode(), plane.type, plane.state, entity, renderJob))
    }

    private fun createPlanePanelEntity(plane: Plane, view: View): PanelEntity {
        return PanelEntity.create(
            renderSession,
            view,
            Dimensions(320f, 320f),
            Dimensions(1f, 1f, 1f),
            plane.hashCode().toString(),
            plane.state.value.centerPose,
        )
    }

    private fun createPanelDebugViewUsingCompose(plane: Plane, activity: Activity): View {
        val view = TextView(activity.applicationContext)
        view.text = "Plane: ${plane.hashCode()}"
        view.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
        view.setBackgroundColor(Color.WHITE)
        return view
    }

    private fun updateViewText(view: View, plane: Plane, state: Plane.State) {
        val textView = view as TextView
        textView.setBackgroundColor(convertPlaneLabelToColor(state.label))
        textView.text = "Plane: ${plane.hashCode()}"
    }

    private fun convertPlaneLabelToColor(label: Plane.Label): Int =
        when (label) {
            Plane.Label.Wall -> Color.GREEN
            Plane.Label.Floor -> Color.BLUE
            Plane.Label.Ceiling -> Color.YELLOW
            Plane.Label.Table -> Color.MAGENTA
            // Planes with Unknown Label are currently not rendered.
            else -> Color.RED
        }

    private fun convertMetersToPixels(input: Vector2): Vector2 = input * PX_PER_METER

    private fun removePlaneModel(planeModel: PlaneModel, planesToRender: MutableList<PlaneModel>) {
        planeModel.renderJob?.cancel()
        planeModel.entity.dispose()
        planesToRender.remove(planeModel)
    }

    private companion object {
        private val PX_PER_METER = Resources.getSystem().displayMetrics.density * 1111.11f
        private val PANEL_TO_PLANE_ROTATION =
            Pose(Vector3(), Quaternion.fromEulerAngles(-90f, 0f, 0f))
        private const val PANEL_RESIZE_UPDATE_COUNT = 50
    }
}
