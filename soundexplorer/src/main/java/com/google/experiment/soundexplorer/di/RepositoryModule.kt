package com.google.experiment.soundexplorer.di

import com.google.experiment.soundexplorer.core.GlbModelRepository
import com.google.experiment.soundexplorer.core.GlbModelRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // TODO: Check if SingletonComponent is correct, maybe ActivityComponent because of session
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindModelRepository(impl: GlbModelRepositoryImpl): GlbModelRepository
}