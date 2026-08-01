package com.walkroute.app.di

import android.content.Context
import com.walkroute.app.ui.screens.home.AndroidLocationTracker
import com.walkroute.app.ui.screens.home.LocationTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {
    
    @Binds
    @Singleton
    abstract fun bindLocationTracker(
        androidLocationTracker: AndroidLocationTracker
    ): LocationTracker
}
