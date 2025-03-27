package com.example.xrexp.arcore.asl2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.xr.runtime.Session
import com.example.xrexp.arcore.common.SessionLifecycleHelper
import com.example.xrexp.ui.theme.XRExpTheme


class ASL2Activity : ComponentActivity() {

    private val viewModel: ASL2ViewModel by viewModels()
    private lateinit var session: Session
    private lateinit var sessionHelper: SessionLifecycleHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create session and renderers.
        sessionHelper =
            SessionLifecycleHelper(
                onCreateCallback = {
                    session = it
                    setContent {
                        XRExpTheme {
                            Surface(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // TODO: ASL Detection Screen
                                ASL2DetectorScreen(viewModel)
                            }
                        }
                    }
                },
                onResumeCallback = {
                    viewModel.startTracking(session)
                },
                beforePauseCallback = {
                    viewModel.stopTracking()
                }
            )
        lifecycle.addObserver(sessionHelper)

    }

}