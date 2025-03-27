package com.google.experiment.soundexplorer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.scenecore.Session
import com.google.experiment.soundexplorer.ui.SoundExplorerMainScreen
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // TODO: refactor
    private val scenecoreSession by lazy { Session.create(this) }


    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scenecoreSession.activitySpace.addComponent(
            InteractableComponent.create(scenecoreSession, mainExecutor) { event ->

                Log.d(TAG, "timestamp:${event.timestamp}, " +
                        "action:${event.action}, " +
                        "origin:${event.origin}, " +
                        "source:${event.source}, " +
                        "hitInfo:${event.hitInfo}, " +
                        "direction:${event.direction}, " +
                        "pointerType:${event.pointerType}, " +
                        "secondaryHitInfo:${event.secondaryHitInfo} "
                )

//                when (event.action) {
//                    InputEvent.ACTION_DOWN -> {
//                        Log.d(TAG, "InputEvent.ACTION_DOWN")
//                    }
//                    InputEvent.ACTION_UP -> {
//                        Log.d(TAG, "InputEvent.ACTION_UP")
//                    }
//                    InputEvent.ACTION_MOVE -> {
//                        Log.d(TAG, "InputEvent.ACTION_MOVE")
//                    }
//                    InputEvent.ACTION_CANCEL -> {
//                        Log.d(TAG, "InputEvent.ACTION_CANCEL")
//                    }
//                    InputEvent.ACTION_HOVER_MOVE -> {
//                        Log.d(TAG, "InputEvent.ACTION_HOVER_MOVE")
//                    }
//                    InputEvent.ACTION_HOVER_ENTER -> {
//                        Log.d(TAG, "InputEvent.ACTION_HOVER_ENTER")
//                    }
//                    InputEvent.ACTION_HOVER_EXIT -> {
//                        Log.d(TAG, "InputEvent.ACTION_HOVER_EXIT")
//                    }
//                    else -> {
//                        Log.d(TAG, "InputEvent.OTHER: ${event.action} event:[$event]")
//                    }
//                }
            }
        )

        Log.d(TAG, "onCreate")
        setContent {
            SoundExplorerMainScreen()
        }
    }


}