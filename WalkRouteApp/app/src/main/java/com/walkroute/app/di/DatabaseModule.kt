package com.walkroute.app.di

import android.content.Context
import androidx.room.Room
import com.walkroute.app.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideUserSettingsDao(database: AppDatabase) = database.userSettingsDao()
    
    @Provides
    @Singleton
    fun provideWalkSessionDao(database: AppDatabase) = database.walkSessionDao()
}
