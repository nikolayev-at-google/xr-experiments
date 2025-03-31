@file:Suppress("SpellCheckingInspection")

package com.google.experiment.soundexplorer

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Dimensions
import androidx.xr.scenecore.MovableComponent
import androidx.xr.scenecore.PanelEntity
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.ui.ActionScreen
import com.google.experiment.soundexplorer.ui.SoundExplorerMainScreen
import kotlin.getValue


class SoundExplorerActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

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

        headLockedPanel =
            PanelEntity.create(
                session = session,
                view = headLockedPanelView,
                surfaceDimensionsPx = Dimensions(1200f, 500f),
                dimensions = Dimensions(500f, 500f),
                name = "headLockedPanel"
            )
        headLockedPanel.setParent(session.activitySpace)
    }

    private fun updateHeadLockedPose() {
        sceneCoreSession.spatialUser.head?.let { projectionSource ->
            projectionSource.transformPoseTo(userForward, sceneCoreSession.activitySpace).let {
                this.headLockedPanel.setPose(it)
            }
        }
        headLockedPanelView.postOnAnimation(this::updateHeadLockedPose)
    }

}