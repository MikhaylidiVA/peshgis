package com.walkroute.app.data.local.dao

import androidx.room.*
import com.walkroute.app.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    
    @Query("SELECT * FROM user_settings LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettingsEntity): Long
    
    @Update
    suspend fun updateUserSettings(settings: UserSettingsEntity)
    
    @Query("DELETE FROM user_settings")
    suspend fun deleteUserSettings()
}
