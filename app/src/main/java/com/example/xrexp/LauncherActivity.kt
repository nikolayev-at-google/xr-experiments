package com.example.xrexp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.xr.compose.platform.LocalHasXrSpatialFeature
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.scenecore.Session
import com.example.xrexp.environment.EnvironmentController
import com.example.xrexp.ui.ExpActivityInfo
import com.example.xrexp.ui.NavigationManager
import com.example.xrexp.ui.RequestFullSpaceButton
import com.example.xrexp.ui.RequestHomeSpaceButton
import com.example.xrexp.ui.SetPassthroughButton
import com.example.xrexp.ui.SetVirtualEnvironmentButton
import com.example.xrexp.ui.theme.LocalSpacing
import com.example.xrexp.ui.theme.XRExpTheme

class LauncherActivity : ComponentActivity() {

    companion object {
        const val TAG = "LauncherActivity"
    }

    private var activityInfoToLaunch: ExpActivityInfo? = null
    private var scenecoreSession : Session? = null

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            if (isGranted) {
                // Permission is granted
                Log.i(TAG, "Permission $permissionName is granted")
            } else {
                // Permission is denied
                Log.e(TAG, "Permission $permissionName is denied")
            }

            val allPermissionsGranted = permissions.all { it.value }
            if (!allPermissionsGranted) {
                Toast.makeText(
                    this,
                    "Required permissions were not granted, try again. ",
                    Toast.LENGTH_LONG,
                ).show()
            } else {
                NavigationManager.start(this, activityInfoToLaunch!!, scenecoreSession)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            XRExpTheme {
                Primary()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun XRTopAppBar() {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                // Only show the mode toggle if the device supports spatial UI
                if (LocalHasXrSpatialFeature.current && !LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    // If we aren't able to access the session, these buttons wouldn't work and shouldn't be shown
                    val activity = LocalActivity.current
                    val isJXRSessionAvailable = LocalSession.current != null
                    if (activity is ComponentActivity && isJXRSessionAvailable) {
                        val environmentController = remember(activity) {
                            val session = Session.create(activity)
                            EnvironmentController(session, activity.lifecycleScope)
                        }
                        RequestFullSpaceButton { environmentController.requestFullSpaceMode() }
                    }
                }
            }
        )
    }

    @Composable
    fun Primary() {
        Scaffold(
            topBar = { XRTopAppBar() }
        ) { innerPadding ->

            val modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()

            Box(Modifier.padding(innerPadding)) {
                ActivityListScreen(
                    modifier = modifier
                )
                if (LocalHasXrSpatialFeature.current && LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    LocalActivity.current?.let { activity ->
                        val environmentController = remember(activity) {
                            val session = Session.create(activity)
                            EnvironmentController(session, (activity as ComponentActivity).lifecycleScope)
                        }
                        // load the model early so it's in memory for when we need it
                        val environmentModelName = "env/green_hills_ktx2_mipmap.glb"
                        environmentController.loadModelAsset(environmentModelName)

                        val showSecondOrbiter = remember { mutableStateOf(false) }

                        Orbiter(
                            position = OrbiterEdge.Vertical.End,
                            alignment = Alignment.Top,
                            offset = LocalSpacing.current.xxxxl
                        ) {
                            Surface(modifier = Modifier.clip(CircleShape)) {
                                Column {
                                    RequestHomeSpaceButton { environmentController.requestHomeSpaceMode() }
                                    SetVirtualEnvironmentButton(
                                        modifier = Modifier
                                            .padding(LocalSpacing.current.m)
                                            .background(
                                                if (showSecondOrbiter.value)
                                                    MaterialTheme.colorScheme.inversePrimary
                                                else
                                                    MaterialTheme.colorScheme.onSecondary,
                                                CircleShape
                                            )
                                    ) {
                                        showSecondOrbiter.value = !showSecondOrbiter.value
                                    }
                                }
                            }
                        }

                        if (showSecondOrbiter.value) {
                            Orbiter(
                                position = OrbiterEdge.Vertical.End,
                                alignment = Alignment.Top,
                                offset = LocalSpacing.current.xxxxl + LocalSpacing.current.xxxxl
                            ) {
                                Surface(modifier = Modifier.clip(CircleShape)) {
                                    Column {
                                        SetPassthroughButton(
                                            iconResId = R.drawable.visibility_on,
                                            stringResId = R.string.enable_passthrough
                                        ) { environmentController.requestPassthrough(1f) }
                                        SetPassthroughButton(
                                            iconResId = R.drawable.visibility_off,
                                            stringResId = R.string.disable_passthrough
                                        ) { environmentController.requestPassthrough(0f) }
                                        SetVirtualEnvironmentButton(
                                            iconResId = R.drawable.ic_download,
                                            stringResId = R.string.replace_virtual_environment
                                        ) { environmentController.requestCustomEnvironment(environmentModelName) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ActivityListScreen(modifier : Modifier) {
        val activities = NavigationManager.getActivities()
        val session = LocalSession.current
        LazyColumn(
            modifier = modifier
        ) {
            items(activities.size) {
                val expActivityInfo = activities[it]
                ActivityItem(expActivityInfo, onClick = {
                    requestPermissionsIfNeeded(expActivityInfo, session)
                })
            }
        }
    }

    @Composable
    fun ActivityItem(activity: ExpActivityInfo, onClick: () -> Unit) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = activity.activityClass.name)
                Text(text = activity.description, color = Color.Gray)
            }
        }
    }

    private fun requestPermissionsIfNeeded(activityInfo: ExpActivityInfo, session : Session?) {
        scenecoreSession = session
        activityInfoToLaunch = activityInfo
        if (activityInfo.permissionsToRequest.isNotEmpty()) {
            // Check if permissions are already granted
            val permissionsNotGranted = activityInfo.permissionsToRequest.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (permissionsNotGranted.isNotEmpty()) {
                Log.i(TAG, "Request permissions if not granted. Launch request permissions...")
                requestMultiplePermissions.launch(permissionsNotGranted.toTypedArray())
            } else {
                NavigationManager.start(this, activityInfo, session)
            }
        } else {
            NavigationManager.start(this, activityInfo, session)
        }
    }
}
