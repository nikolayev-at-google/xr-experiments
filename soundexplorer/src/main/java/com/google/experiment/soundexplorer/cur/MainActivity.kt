package com.google.experiment.soundexplorer.cur

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.spatial.Subspace
import com.google.experiment.soundexplorer.core.GlbModelRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var modelRepository : GlbModelRepository
    private val viewModel : MainViewModel by viewModels()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val session = LocalSession.current
            if (session == null) {
                return@setContent
            }
            Subspace {
                MainScreen(modelRepository)
            }
        }

    }

}