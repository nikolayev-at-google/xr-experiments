package com.example.xrexp.arcore.helloar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.xrexp.arcore.common.BackToMainActivityButton
import com.example.xrexp.arcore.common.SessionLifecycleHelper
import com.example.xrexp.arcore.common.TrackablesList
import com.example.xrexp.arcore.helloar.rendering.AnchorRenderer
import com.example.xrexp.arcore.helloar.rendering.PlaneRenderer
import androidx.xr.arcore.perceptionState
import androidx.xr.runtime.Session
import androidx.xr.scenecore.Session as JxrCoreSession

/** Sample that demonstrates fundamental ARCore for Android XR usage. */
class HelloArActivity : ComponentActivity() {

    private lateinit var session: Session
    private lateinit var sessionHelper: SessionLifecycleHelper

    private lateinit var jxrCoreSession: JxrCoreSession

    private var planeRenderer: PlaneRenderer? = null
    private var anchorRenderer: AnchorRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create session and renderers.
        sessionHelper =
            SessionLifecycleHelper(
                onCreateCallback = {
                    session = it
                    jxrCoreSession = JxrCoreSession.create(this)
                    planeRenderer = PlaneRenderer(session, jxrCoreSession, lifecycleScope)
                    anchorRenderer =
                        AnchorRenderer(
                            this,
                            planeRenderer!!,
                            session,
                            jxrCoreSession,
                            lifecycleScope
                        )
                    setContent { HelloWorld(session) }
                },
                onResumeCallback = {
                    planeRenderer?.startRendering()
                    anchorRenderer?.startRendering()
                },
                beforePauseCallback = {
                    planeRenderer?.stopRendering()
                    anchorRenderer?.stopRendering()
                },
            )
        lifecycle.addObserver(sessionHelper)
    }
}

@Composable
fun HelloWorld(session: Session) {
    val state by session.state.collectAsStateWithLifecycle()
    val perceptionState = state.perceptionState

    Column(modifier = Modifier.background(color = Color.White)) {
        BackToMainActivityButton()
        Text(text = "CoreState: ${state.timeMark}")
        if (perceptionState != null) {
            TrackablesList(perceptionState.trackables.toList())
        } else {
            Text("PerceptionState is null.")
        }
    }
}
