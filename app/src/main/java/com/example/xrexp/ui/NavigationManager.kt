package com.example.xrexp.ui

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.util.Log
import androidx.xr.scenecore.Session
import androidx.xr.scenecore.setFullSpaceMode
import com.example.xrexp.Main3DActivity
import com.example.xrexp.Main3DAnimActivity
import com.example.xrexp.MainActivity
import com.example.xrexp.MainEnvironmentActivity
import com.example.xrexp.MainVideoActivity
import com.example.xrexp.arcore.asl.ASLDetectorActivity
import com.example.xrexp.arcore.ExpArCoreActivity
import com.example.xrexp.arcore.ExpHandsActivity
import com.example.xrexp.arcore.asl2.ASL2Activity
import com.example.xrexp.audio.AudioActivity
import com.example.xrexp.m3.M3MainActivity

object NavigationManager {

    private const val TAG = "NavigationManager"

    private val activityRegistry = listOf(
        ExpActivityInfo(
            activityClass = ASL2Activity::class.java,
            description = "American Sign Language detection.",
            isFullSpace = true
        ),
        ExpActivityInfo(
            activityClass = MainActivity::class.java,
            description = "The main entry point of the app"
        ),
        ExpActivityInfo(
            activityClass = ASLDetectorActivity::class.java,
            description = "American Sign Language detection. Activity to recognize ASL signs A, B, C, D, and E."
        ),
        ExpActivityInfo(
            activityClass = AudioActivity::class.java,
            description = "Activity to test spatial audio features",
            isFullSpace = true
        ),
        ExpActivityInfo(
            activityClass = ExpHandsActivity::class.java,
            description = "Hands tracking test activity, ThumbsUP experiments",
            isFullSpace = true,
            permissionsToRequest = arrayOf(
                "android.permission.SCENE_UNDERSTANDING",
                "android.permission.HAND_TRACKING",
            )
        ),
        ExpActivityInfo(
            activityClass = ExpArCoreActivity::class.java,
            description = "Custom ArCore playground",
            isFullSpace = true,
            permissionsToRequest = arrayOf(
                "android.permission.SCENE_UNDERSTANDING",
                "android.permission.HAND_TRACKING",
//            "android.permission.SCENE_UNDERSTANDING_COARSE",
//            "android.permission.SCENE_UNDERSTANDING_FINE"
            )
        ),
        ExpActivityInfo(
            activityClass = Main3DActivity::class.java,
            description = "Basic 3D model viewer",
            isFullSpace = true
        ),
        ExpActivityInfo(
            activityClass = Main3DAnimActivity::class.java,
            description = "Basic 3D model viewer with animation switch",
            isFullSpace = true
        ),
        ExpActivityInfo(
            activityClass = MainVideoActivity::class.java,
            description = "Basic video viewer"
        ),
        ExpActivityInfo(
            activityClass = M3MainActivity::class.java,
            description = "Material3 example with adaptive layouts"
        ),
        ExpActivityInfo(
            activityClass = com.example.xrexp.arcore.MainActivity::class.java,
            description = "ARCore test application",
            isFullSpace = true
        ),
        ExpActivityInfo(
            activityClass = MainEnvironmentActivity::class.java,
            description = "Testing environments properties, skybox, passthrough...",
            isFullSpace = true
        )
    )

    fun getActivities(): List<ExpActivityInfo> = activityRegistry

    fun start(context: Context, info : ExpActivityInfo, session : Session?) {

        val intent = Intent(context, info.activityClass)

        if (info.isFullSpace) {
            Log.i(TAG, "Starting new activity(${info.activityClass.name}) in Full space")
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            val bundle = Bundle()
            val startActivityBundle = session?.setFullSpaceMode(bundle) ?: bundle
            context.startActivity(intent, startActivityBundle)
        } else {
            Log.i(TAG, "Starting new activity(${info.activityClass.name}) in Home space")
            context.startActivity(intent)
        }
    }
}


