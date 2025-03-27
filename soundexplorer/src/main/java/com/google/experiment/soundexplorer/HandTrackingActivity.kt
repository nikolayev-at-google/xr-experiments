package com.google.experiment.soundexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.xr.runtime.Session
import androidx.xr.runtime.SessionCreateSuccess

class HandTrackingActivity : ComponentActivity() {

    private var session: Session? = null
    private lateinit var viewModel: HandTrackingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create session
        val sessionResult = Session.create(this)
        if (sessionResult is SessionCreateSuccess) {
            session = sessionResult.session
            session?.configure()

            // Create view model
            viewModel = HandTrackingViewModel()

            setContent {
                MaterialTheme {
                    HandTrackingDebugScreen(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        session?.resume()
        // Start tracking
        viewModel.startTracking(session!!)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopTracking()
        session?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        session?.destroy()
    }
}