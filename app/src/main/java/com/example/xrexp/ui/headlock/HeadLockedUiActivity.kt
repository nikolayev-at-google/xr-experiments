package com.example.xrexp.ui.headlock


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.ActivityPose
import androidx.xr.scenecore.Dimensions
import androidx.xr.scenecore.MovableComponent
import androidx.xr.scenecore.PanelEntity
import androidx.xr.scenecore.Session
import com.example.xrexp.R

class HeadLockedUiActivity : ComponentActivity() {

    private val mSession by lazy { Session.create(this) }
    private var mUserForward: Pose by mutableStateOf(Pose(Vector3(0f, 0.00f, -1.0f)))
    private lateinit var mHeadLockedPanel: PanelEntity
    private lateinit var mHeadLockedPanelView: View
    private var mProjectionSource: ActivityPose? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the main panel size and make the main panel movable.
        mSession.mainPanelEntity.setSize(Dimensions(width = 1500f, height = 1100f))
        val movableComponent =
            MovableComponent.create(mSession, systemMovable = true, scaleInZ = false)
        mSession.mainPanelEntity.addComponent(movableComponent)
        mSession.mainPanelEntity.setHidden(true)

        // Create the image panel.
        @SuppressLint("InflateParams")
        this.mHeadLockedPanelView = layoutInflater.inflate(R.layout.image, null, false)
        this.mHeadLockedPanelView.postOnAnimation(this::updateHeadLockedPose)
        this.mHeadLockedPanel =
            PanelEntity.create(
                session = mSession,
                view = mHeadLockedPanelView,
                surfaceDimensionsPx = Dimensions(360f, 180f),
                dimensions = Dimensions(360f, 180f),
                name = "headLockedPanel",
                pose = Pose(Vector3(0f, 0f, 0f)),
            )
        this.mHeadLockedPanel.setParent(mSession.activitySpace)
    }

    private fun updateHeadLockedPose() {
        mProjectionSource = mSession.spatialUser.head
        if (this.mProjectionSource != null) {
            // Since the panel is parented by the activitySpace, we need to inverse its scale
            // so that the panel stays at a fixed size in the view even when ActivitySpace scales.
            this.mHeadLockedPanel.setScale(0.5f / mSession.activitySpace.getScale())
            this.mProjectionSource?.transformPoseTo(mUserForward, mSession.activitySpace)?.let {
                this.mHeadLockedPanel.setPose(it)
            }
        }
        mHeadLockedPanelView.postOnAnimation(this::updateHeadLockedPose)
    }

}
