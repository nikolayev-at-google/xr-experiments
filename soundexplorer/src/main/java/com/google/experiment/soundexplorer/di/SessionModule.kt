package com.google.experiment.soundexplorer.di

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import androidx.xr.scenecore.Session as SceneCoreSession


@Module
@InstallIn(ActivityComponent::class)
object SessionModule {

    @Provides
    @ActivityScoped
    fun provideSceneCoreSession(activity: Activity): SceneCoreSession =
        SceneCoreSession.create(activity = (activity as ComponentActivity))

}