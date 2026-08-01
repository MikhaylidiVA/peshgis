package com.walkroute.app.di

import android.content.Context
import com.walkroute.app.data.repository.RouteGenerator
import com.walkroute.app.ui.screens.navigation.TextToSpeechManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {
    
    @Provides
    @Singleton
    fun provideRouteGenerator(): RouteGenerator {
        return RouteGenerator()
    }
    
    @Provides
    @Singleton
    fun provideTextToSpeechManager(
        @ApplicationContext context: Context
    ): TextToSpeechManager {
        return TextToSpeechManager(context)
    }
}
