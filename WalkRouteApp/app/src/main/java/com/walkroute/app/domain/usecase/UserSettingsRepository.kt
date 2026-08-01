package com.walkroute.app.domain.usecase

import com.walkroute.app.data.local.dao.UserSettingsDao
import com.walkroute.app.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepository @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) {
    
    val userSettings: Flow<UserSettingsEntity?> = userSettingsDao.getUserSettings()
    
    suspend fun saveUserSettings(settings: UserSettingsEntity): Long {
        return userSettingsDao.insertUserSettings(settings)
    }
    
    suspend fun updateUserSettings(settings: UserSettingsEntity) {
        userSettingsDao.updateUserSettings(settings)
    }
    
    suspend fun deleteUserSettings() {
        userSettingsDao.deleteUserSettings()
    }
}
