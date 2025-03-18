package com.example.xrexp.arcore.handtracking

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.xr.arcore.Hand
import com.example.xrexp.arcore.common.BackToMainActivityButton
import com.example.xrexp.arcore.common.SessionLifecycleHelper
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Dimensions
import androidx.xr.scenecore.PanelEntity
import kotlin.collections.iterator
import androidx.xr.scenecore.Session as JxrCoreSession

/** Sample that demonstrates fundamental ARCore for Android XR usage. */
class HandTrackingActivity : ComponentActivity() {

    private lateinit var session: Session
    private lateinit var sessionHelper: SessionLifecycleHelper

    private lateinit var jxrCoreSession: JxrCoreSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create session and renderers.
        sessionHelper =
            SessionLifecycleHelper(
                onCreateCallback = {
                    session = it
                    jxrCoreSession = JxrCoreSession.create(this)
                    setContent { MainPanel(session) }
                    createHandPanel(session, isLeftHand = true)
                    createHandPanel(session, isLeftHand = false)
                }
            )
        lifecycle.addObserver(sessionHelper)
    }

    private fun createHandPanel(session: Session, isLeftHand: Boolean) {
        val composeView = ComposeView(this)
        configureComposeView(composeView, this)
        val hand = if (isLeftHand) Hand.left(session) else Hand.right(session)
        val title = if (isLeftHand) "Left Hand" else "Right Hand"
        val position = if (isLeftHand) Vector3(-0.6f, 0f, 0.1f) else Vector3(0.6f, 0f, 0.1f)
        val rotation = if (isLeftHand) Quaternion(0f, 0f, 0f, 1f) else Quaternion(0f, 0f, 0f, 1f)
        val panelEntity =
            PanelEntity.create(
                jxrCoreSession,
                composeView,
                Dimensions(1000f, 700f),
                Dimensions(1f, 1f, 1f),
                title,
                Pose(position, rotation),
            )
        panelEntity.setParent(jxrCoreSession.activitySpace)
        composeView.setContent { HandTrackingPanel(hand!!, isLeftHand) }
    }

    private fun configureComposeView(composeView: ComposeView, activity: Activity) {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setViewTreeLifecycleOwner(activity as LifecycleOwner)
        composeView.setViewTreeViewModelStoreOwner(activity as ViewModelStoreOwner)
        composeView.setViewTreeSavedStateRegistryOwner(activity as SavedStateRegistryOwner)
    }
}

@Composable
fun MainPanel(session: Session) {
    val state by session.state.collectAsStateWithLifecycle()

    val leftHand = Hand.left(session)
    val rightHand = Hand.right(session)

    Column(
        modifier =
            Modifier.background(color = Color.White)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 20.dp)
    ) {
        BackToMainActivityButton()
        Text(text = "CoreState: ${state.timeMark.toString()}")
        if (leftHand == null || rightHand == null) {
            Text("Hand module is not supported.")
        } else {
            Text("Hand module is supported.")
        }
    }
}

@Composable
private fun HandTrackingPanel(hand: Hand, isLeftHand: Boolean) {
    val handState by hand.state.collectAsStateWithLifecycle()
    val name = if (isLeftHand) "Left Hand" else "Right Hand"
    Column(
        modifier =
            Modifier.background(color = Color.White)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 30.dp)
    ) {
        Text("${name} isActive: ${handState.isActive}")
        for ((jointType, pose) in handState.handJoints) {
            Text("${name} joint ${jointType}: ${pose.translation}")
        }
    }
}
