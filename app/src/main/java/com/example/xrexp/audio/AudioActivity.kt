package com.example.xrexp.audio

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.xr.compose.spatial.Subspace
import com.example.xrexp.ui.theme.XRExpTheme

class AudioActivity : ComponentActivity() {

    companion object {
        private const val TAG = "AudioActivity"
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Audio activity created")

        enableEdgeToEdge()

        setContent {
            XRExpTheme {
                SpatialAudioApp()
            }
        }
    }
}
