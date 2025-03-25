package com.google.experiment.soundexplorer.di

import android.content.Context
import androidx.activity.ComponentActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import androidx.xr.scenecore.Session as SceneCoreSession
import androidx.xr.runtime.Session as ARCoreSession


@Module
@InstallIn(ActivityComponent::class)
object SessionModule {

    @Provides
    fun provideSceneCoreSession(@ActivityContext context: Context): SceneCoreSession =
        SceneCoreSession.create(activity = (context as ComponentActivity))

}