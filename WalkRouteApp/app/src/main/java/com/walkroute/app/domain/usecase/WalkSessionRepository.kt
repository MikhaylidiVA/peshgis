package com.walkroute.app.domain.usecase

import com.walkroute.app.data.local.dao.WalkSessionDao
import com.walkroute.app.data.local.entity.WalkSessionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalkSessionRepository @Inject constructor(
    private val walkSessionDao: WalkSessionDao
) {
    val allWalkSessions: Flow<List<WalkSessionEntity>> = walkSessionDao.getAllWalkSessions()
    
    val completedWalkSessions: Flow<List<WalkSessionEntity>> = walkSessionDao.getCompletedWalkSessions()
    
    suspend fun getWalkSessionById(id: Long): WalkSessionEntity? {
        return walkSessionDao.getWalkSessionById(id)
    }
    
    suspend fun insertWalkSession(session: WalkSessionEntity): Long {
        return walkSessionDao.insertWalkSession(session)
    }
    
    suspend fun updateWalkSession(session: WalkSessionEntity) {
        walkSessionDao.updateWalkSession(session)
    }
    
    suspend fun deleteWalkSession(session: WalkSessionEntity) {
        walkSessionDao.deleteWalkSession(session)
    }
    
    suspend fun deleteWalkSessionById(id: Long) {
        walkSessionDao.deleteWalkSessionById(id)
    }
    
    suspend fun deleteAllWalkSessions() {
        walkSessionDao.deleteAllWalkSessions()
    }
    
    fun getTotalWalksCount(): Flow<Int> = walkSessionDao.getTotalWalksCount()
    
    fun getTotalStepsSum(): Flow<Int?> = walkSessionDao.getTotalStepsSum()
    
    fun getTotalDistanceSum(): Flow<Int?> = walkSessionDao.getTotalDistanceSum()
}
