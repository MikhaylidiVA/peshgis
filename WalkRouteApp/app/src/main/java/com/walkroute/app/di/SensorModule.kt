package com.walkroute.app.di

import android.content.Context
import android.hardware.SensorManager
import com.walkroute.app.data.repository.AndroidStepDetector
import com.walkroute.app.data.repository.StepDetector
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SensorModule {
    
    @Binds
    @Singleton
    abstract fun bindStepDetector(
        androidStepDetector: AndroidStepDetector
    ): StepDetector
    
    companion object {
        @Provides
        @Singleton
        fun provideSensorManager(
            @ApplicationContext context: Context
        ): SensorManager {
            return context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }
    }
}
