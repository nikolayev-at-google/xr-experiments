package com.example.xrexp.arcore

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.xr.arcore.Plane
import androidx.xr.arcore.Trackable
import androidx.xr.arcore.perceptionState
import androidx.xr.runtime.Session
import androidx.xr.runtime.SessionCreatePermissionsNotGranted
import androidx.xr.runtime.SessionCreateSuccess
import androidx.xr.runtime.SessionResumePermissionsNotGranted
import androidx.xr.runtime.SessionResumeSuccess
import kotlinx.coroutines.launch


class ExpArCoreActivity : ComponentActivity() {

    companion object {
        private val TAG = "ExpArCoreActivity"
    }

    lateinit var arCoreSession: Session


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSession()
    }

    override fun onResume() {
        super.onResume()
        if (!this::arCoreSession.isInitialized) {
            return
        }
        when (val result = arCoreSession.resume()) {
            is SessionResumeSuccess -> {
                // TODO - working...
            }
            is SessionResumePermissionsNotGranted -> {
                Log.e(TAG, "Attempted to resume while SessionResumePermissionsNotGranted")
            }
            else -> {
                Log.e(TAG, "Attempted to resume while session is null.")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!this::arCoreSession.isInitialized) {
            return
        }
        // TODO - working...
        arCoreSession.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!this::arCoreSession.isInitialized) {
            return
        }
        arCoreSession.destroy()
    }

    private fun setupSession() {
        val result = Session.create(this)
        Log.d(TAG, "result: $result")
        when (result) {
            is SessionCreateSuccess -> {
                arCoreSession = result.session
                // TODO - working...
                setContent { ExpArCoreWindow() }
            }
            is SessionCreatePermissionsNotGranted -> {
                Log.e(TAG, "Attempted to setup session returned: SessionResumePermissionsNotGranted")
            }
            else -> {
                Log.e(TAG, "Attempted to setup session is null.")
            }
        }
    }

    @Composable
    fun ExpArCoreWindow() {
        val state by arCoreSession.state.collectAsStateWithLifecycle()
        val perceptionState = state.perceptionState

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    Plane.subscribe(arCoreSession).collect { planes ->
                        Log.d(TAG, "ExpArCoreWindow: ")
                    }
                }
            }
            Text(
                modifier = Modifier.padding(16.dp),
                text = "CoreState: ${state.timeMark}",
                fontSize = 18.sp,
            )
            if (perceptionState != null) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "perceptionState.trackables:\n${perceptionState.trackables.toList()}",
                    fontSize = 18.sp
                )
                TrackablesList(perceptionState.trackables.toList())
            } else {
                Text("PerceptionState is null.")
            }
        }
    }

    @Composable
    fun TrackablesList(trackables: List<Trackable<Trackable.State>>) {
        LazyColumn {
            items(trackables) { trackable ->
                TrackableCard(trackable)
            }
        }
    }

    @Composable
    fun TrackableCard(trackable: Trackable<Trackable.State>) {
        val state = trackable.state.collectAsStateWithLifecycle()
        OutlinedCard(
            colors = CardDefaults.cardColors(),
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Trackable ID: ${trackable}")
                Text(text = "Tracking State: ${state.value.trackingState}")
                if (trackable is Plane) {
                    Text("Plane Type: ${trackable.type}")
                    PlaneStateInfo(state.value as Plane.State)
                }
            }
        }
    }

    @Composable
    fun PlaneStateInfo(state: Plane.State) {
        Text(text = "Plane Label: ${state.label}", color = convertPlaneLabelToColor(state.label))
        Text(text = "Plane Center Pose: ${state.centerPose}")
        Text(text = "Plane Extents: ${state.extents}")
        Text(text = "Subsumed by Plane: ${state.subsumedBy}")
        Text(text = "Plane Vertices: ${state.vertices}")
    }

    private fun convertPlaneLabelToColor(label: Plane.Label): Color =
        when (label) {
            Plane.Label.Wall -> Color.Green
            Plane.Label.Floor -> Color.Blue
            Plane.Label.Ceiling -> Color.Yellow
            Plane.Label.Table -> Color.Magenta
            else -> Color.Red
        }
}