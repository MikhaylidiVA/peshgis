package com.walkroute.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.walkroute.app.data.local.dao.UserSettingsDao
import com.walkroute.app.data.local.dao.WalkSessionDao
import com.walkroute.app.data.local.entity.UserSettingsEntity
import com.walkroute.app.data.local.entity.WalkSessionEntity

@Database(
    entities = [
        UserSettingsEntity::class,
        WalkSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun walkSessionDao(): WalkSessionDao
    
    companion object {
        const val DATABASE_NAME = "walkroute_database"
    }
}
